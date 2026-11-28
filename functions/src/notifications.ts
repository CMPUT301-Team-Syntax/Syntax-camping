import {createHash} from "node:crypto";
import {getFirestore} from "firebase-admin/firestore";
import {getMessaging} from "firebase-admin/messaging";

const db = getFirestore();
const messaging = getMessaging();

function isValidId(value: unknown): value is string {
    return typeof value === "string" && value.trim().length > 0 &&
        Buffer.byteLength(value) <= 1500 && value !== "." && value !== ".." &&
        !value.includes("/") && !/^__.*__$/.test(value);
}

export async function sendPushToUser(
    userId: unknown,
    title: string,
    body: string,
    data?: Record<string, string>,
    eventKey?: string,
): Promise<void> {
    if (!isValidId(userId)) return;

    const userRef = db.collection("users").doc(userId);
    const notifId = eventKey
        ? createHash("sha256").update(JSON.stringify([userId, eventKey])).digest("hex")
        : db.collection("_").doc().id;

    // 1. Save a persistent copy to Firestore so the user's in-app inbox sees it even if the app was closed
    const storePromise = userRef.collection("notifications").doc(notifId).create({
        title,
        message: body,
        data: data ?? {},
        read: false,
        occurredAt: Date.now(),
    }).catch((error) => {
        if (error.code !== 6) throw error; // Ignore gRPC ALREADY_EXISTS on event retries
    });

    // 2. Look up the user's Android FCM device tokens and send the system tray push
    const userDoc = await userRef.get();
    const rawTokens = userDoc.get("fcmTokens");
    if (!Array.isArray(rawTokens) || rawTokens.length === 0) {
        await storePromise;
        return;
    }

    const tokens = rawTokens.filter((t): t is string => typeof t === "string" && t.trim().length > 0);
    if (tokens.length === 0) {
        await storePromise;
        return;
    }

    const response = await messaging.sendEachForMulticast({
        tokens,
        notification: {title, body},
        data: {
            ...data,
            notificationId: notifId,
        },
        android: {
            priority: "high",
            notification: {
                // Must match CHANNEL_ID in AppFirebaseMessagingService.kt
                channelId: "default_notifications",
            },
        },
    });

    // 3. Clean up expired or uninstalled device tokens automatically
    const badTokens: string[] = [];
    response.responses.forEach((res, idx) => {
        if (!res.success && (
            res.error?.code === "messaging/registration-token-not-registered" ||
            res.error?.code === "messaging/invalid-registration-token"
        )) {
            badTokens.push(tokens[idx]);
        }
    });

    const cleanupPromise = badTokens.length > 0
        ? userRef.update({fcmTokens: tokens.filter((t) => !badTokens.includes(t))})
        : Promise.resolve();

    await Promise.all([storePromise, cleanupPromise]);
}