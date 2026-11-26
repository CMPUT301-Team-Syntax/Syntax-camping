import {createHash} from "node:crypto";
import {
    FieldPath, getFirestore,
    type DocumentData, type Query,
} from "firebase-admin/firestore";
import {onDocumentCreated, onDocumentUpdated, onDocumentWritten} from "firebase-functions/v2/firestore";
import {HttpsError, onCall} from "firebase-functions/v2/https";

const db = getFirestore();
const pageSize = 50;
type Scope = "campsite" | "owner";

function logCollection(scope: Scope, id: string) {
    return db.collection("notificationAudit").doc(scope).collection(id);
}

function validId(value: unknown): value is string {
    return typeof value === "string" && value.trim().length > 0 &&
        Buffer.byteLength(value) <= 1500 && value !== "." && value !== ".." &&
        !value.includes("/") && !/^__.*__$/.test(value);
}

type AuditItem = {
    key: string;
    title: string;
    message: string;
};

async function saveLotteryAudit(
    lotteryId: string,
    lottery: DocumentData,
    item: AuditItem,
    occurredAt: number,
    selectedCampsiteId?: unknown,
) {
    const snapshots = new Map<string, DocumentData>();
    if (Array.isArray(lottery.campsites)) {
        for (const campsite of lottery.campsites) {
            if (validId(campsite?.documentId)) snapshots.set(campsite.documentId, campsite);
        }
    }

    const linked = await db.collection("campsites").where("lotteryId", "==", lotteryId).get();
    for (const campsite of linked.docs) snapshots.set(campsite.id, campsite.data());

    const targets = validId(selectedCampsiteId) && snapshots.has(selectedCampsiteId)
        ? [selectedCampsiteId] : [...snapshots.keys()];
    const writer = db.bulkWriter();
    const writes = [];
    for (const campsiteId of targets) {
        const campsite = snapshots.get(campsiteId);
        const campsiteName = typeof campsite?.name === "string" && campsite.name.trim()
            ? campsite.name.trim() : campsiteId;
        const ownerId = validId(campsite?.ownerId) ? campsite.ownerId : lottery.ownerId;
        const id = createHash("sha256").update(JSON.stringify([lotteryId, campsiteId, item.key])).digest("hex");
        const data = {campsiteId, campsiteName, title: item.title, message: item.message, occurredAt};
        writes.push(writer.create(logCollection("campsite", campsiteId).doc(id), data));
        if (validId(ownerId)) writes.push(writer.create(logCollection("owner", ownerId).doc(id), data));
    }

    // Firestore events can be delivered more than once. Keep the first record for each event.
    const completed = Promise.all(writes.map((write) => write.catch((error) => {
        if (error.code !== 6) throw error; // gRPC ALREADY_EXISTS
    })));
    await Promise.all([writer.close(), completed]);
}

function lotteryName(lottery: DocumentData): string {
    return typeof lottery.name === "string" && lottery.name.trim() ? lottery.name.trim() : "a lottery";
}

export const logLotteryRegistration = onDocumentCreated(
    {document: "lotteries/{lotteryId}/entries/{userId}", retry: true},
    async (event) => {
        if (!event.data) return;
        const lottery = (await event.data.ref.parent.parent!.get()).data();
        if (!lottery) return;
        await saveLotteryAudit(event.params.lotteryId, lottery, {
            key: event.id,
            title: "Joined lottery",
            message: "Your lottery entry was received. Check the lottery details for updates.",
        }, event.data.createTime.toMillis(), event.data.get("notificationCampsiteId"));
    },
);

export const logLotteryWaitlist = onDocumentCreated(
    {document: "lotteries/{lotteryId}/waitlist/{userId}", retry: true},
    async (event) => {
        if (!event.data) return;
        const direct = event.data.get("directWaitlistRegistration") === true;
        const drawn = event.data.get("addedByDraw") === true;
        if (!direct && !drawn) return;

        const lottery = (await event.data.ref.parent.parent!.get()).data();
        if (!lottery) return;
        await saveLotteryAudit(event.params.lotteryId, lottery, {
            key: event.id,
            title: drawn ? "You're on the waitlist" : "Joined waitlist",
            message: drawn
                ? `You weren't picked in ${lotteryName(lottery)}. You're on the waitlist and will be offered a spot if one opens up.`
                : "You’ve been added to the waitlist. Check the lottery details for updates.",
        }, event.data.createTime.toMillis(), event.data.get("notificationCampsiteId"));
    },
);

export const logLotteryWinner = onDocumentWritten(
    {document: "lotteries/{lotteryId}/winners/{userId}", retry: true},
    async (event) => {
        const after = event.data?.after;
        if (!after?.exists || after.get("status") !== "pending") return;
        const before = event.data?.before;
        if (before?.exists && before.get("status") === "pending" &&
            before.get("selectionTimestamp") === after.get("selectionTimestamp")) return;

        const lottery = (await after.ref.parent.parent!.get()).data();
        if (!lottery) return;
        await saveLotteryAudit(event.params.lotteryId, lottery, {
            key: event.id,
            title: "You won the lottery!",
            message: `You were picked in ${lotteryName(lottery)}. Open the lottery to choose a campsite and your dates.`,
        }, after.updateTime!.toMillis());
    },
);

export const logLotteryOutcome = onDocumentUpdated(
    {document: "lotteries/{lotteryId}", retry: true},
    async (event) => {
        if (!event.data) return;
        const {before, after} = event.data;
        const lottery = after.data();
        if (before.get("status") === lottery.status) return;

        const drawn = lottery.status === "booking" && typeof lottery.drawCompletedAt === "number";
        if (!drawn && lottery.status !== "complete") return;

        await saveLotteryAudit(event.params.lotteryId, lottery, {
            key: event.id,
            title: drawn ? "Lottery drawn" : "Lottery ended",
            message: drawn
                ? `The lottery for ${lotteryName(lottery)} was drawn.`
                : `The lottery for ${lotteryName(lottery)} has ended.`,
        }, after.updateTime.toMillis());
    },
);

export const getAdminNotificationLogs = onCall(async (request) => {
    if (!request.auth) throw new HttpsError("unauthenticated", "Sign in to review notification logs.");
    if (request.auth.token.admin !== true) {
        throw new HttpsError("permission-denied", "An administrator account is required.");
    }

    const {scope, id, cursor} = request.data ?? {};
    if ((scope !== "campsite" && scope !== "owner") || !validId(id)) {
        throw new HttpsError("invalid-argument", "Choose a campsite or owner.");
    }

    let query: Query = logCollection(scope, id)
        .orderBy("occurredAt", "desc").orderBy(FieldPath.documentId(), "desc");
    if (cursor != null) {
        if (!Number.isSafeInteger(cursor.occurredAt) || !validId(cursor.id)) {
            throw new HttpsError("invalid-argument", "Invalid notification log cursor.");
        }
        query = query.startAfter(cursor.occurredAt, cursor.id);
    }
    const source = scope === "campsite" ? "campsites" : "users";
    const labelField = scope === "campsite" ? "name" : "displayName";
    const [snapshot, parent] = await Promise.all([
        query.limit(pageSize + 1).get(),
        db.collection(source).doc(id).get(),
    ]);
    const rows = snapshot.docs.slice(0, pageSize).map((entry) => ({
        id: entry.id,
        campsiteId: entry.get("campsiteId"),
        campsiteName: entry.get("campsiteName"),
        title: entry.get("title"),
        message: entry.get("message"),
        occurredAt: entry.get("occurredAt"),
    }));
    const last = rows[rows.length - 1];
    const name = parent.get(labelField);
    const fallback = scope === "campsite" ? rows[0]?.campsiteName ?? id : id;
    return {
        name: typeof name === "string" && name.trim() ? name.trim() : fallback,
        rows,
        next: snapshot.size > pageSize ? {id: last.id, occurredAt: last.occurredAt} : null,
    };
});
