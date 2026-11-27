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
): Promise<void> {
    if (!isValidId(userId)) return;

    const userDoc = await db.collection("users").doc(userId).get();
    const rawTokens = userDoc.get("fcmTokens");
    if (!Array.isArray(rawTokens) || rawTokens.length === 0) return;

    const tokens = rawTokens.filter((t): t is string => typeof t === "string" && t.trim().length > 0);
    if (tokens.length === 0) return;

    const response = await messaging.sendEachForMulticast({
        tokens,
        notification: {title, body},
        data,
        android: {
            priority: "high",
            notification: {
                // Must match the NotificationChannel ID created in your Android app
                channelId: "default_notifications",
            },
        },
    });

    // Clean up expired or uninstalled device tokens automatically
    const badTokens: string[] = [];
    response.responses.forEach((res, idx) => {
        if (!res.success && (
            res.error?.code === "messaging/registration-token-not-registered" ||
            res.error?.code === "messaging/invalid-registration-token"
        )) {
            badTokens.push(tokens[idx]);
        }
    });

    if (badTokens.length > 0) {
        await userDoc.ref.update({
            fcmTokens: tokens.filter((t) => !badTokens.includes(t)),
        });
    }
}