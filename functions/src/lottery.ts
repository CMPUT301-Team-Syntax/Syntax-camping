import {onDocumentCreated} from "firebase-functions/v2/firestore";
import {onTaskDispatched} from "firebase-functions/v2/tasks";
import {getFunctions} from "firebase-admin/functions";
import {getFirestore, FieldValue} from "firebase-admin/firestore";

const db = getFirestore();
const MAX_TASK_WINDOW_MS = 700 * 60 * 60 * 1000; // 700 hours (~29.1 days, under Cloud Tasks' 720-hour limit)

interface LotteryEntry {
    userId: string;
    joinedTimestamp: number;
}

export const scheduleLotteryDraw = onDocumentCreated(
    {
        document: "lotteries/{lotteryId}",
        region: "northamerica-northeast1",
    },
    async (event) => {
        const snapshot = event.data;
        if (!snapshot) {
            console.log("No lottery document found.");
            return;
        }

        const lottery = snapshot.data();
        const lotteryId = event.params.lotteryId;
        const endTimeLottery = lottery.endTimeLottery;

        if (typeof endTimeLottery !== "number" || !Number.isFinite(endTimeLottery)) {
            throw new Error("Invalid lottery endTime.");
        }

        const now = Date.now();

        // 1. Schedule lottery draw (clamped between 1s and 700h from now)
        const safeLotteryTime = Math.min(
            Math.max(endTimeLottery, now + 1000),
            now + MAX_TASK_WINDOW_MS
        );
        const queue = getFunctions().taskQueue(
            "projects/syntaxcamping/locations/northamerica-northeast1/functions/runLotteryDraw"
        );

        await queue.enqueue(
            {lotteryId},
            {
                scheduleTime: new Date(safeLotteryTime),
                dispatchDeadlineSeconds: 60 * 5,
            }
        );

        // 2. Schedule booking close (clamped between 2s and 700h from now)
        const endTimeBooking = lottery.endTimeBooking;
        if (typeof endTimeBooking !== "number" || !Number.isFinite(endTimeBooking)) {
            throw new Error("Invalid booking endTime.");
        }

        const safeBookingTime = Math.min(
            Math.max(endTimeBooking, now + 2000),
            now + MAX_TASK_WINDOW_MS
        );
        const closingQueue = getFunctions().taskQueue(
            "projects/syntaxcamping/locations/northamerica-northeast1/functions/closeLotteryBooking"
        );

        await closingQueue.enqueue(
            {lotteryId},
            {
                scheduleTime: new Date(safeBookingTime),
                dispatchDeadlineSeconds: 60 * 5,
            }
        );
    }
);

export const runLotteryDraw = onTaskDispatched(
    {
        region: "northamerica-northeast1",
        retryConfig: {
            maxAttempts: 3,
            minBackoffSeconds: 60,
        },
    },
    async (request) => {
        const lotteryId = request.data.lotteryId;
        const lotteryReference = db.collection("lotteries").doc(lotteryId);

        const lotteryCurrent = await lotteryReference.get();
        if (!lotteryCurrent.exists) {
            console.log("Lottery no longer exists.");
            return;
        }
        const lotteryInfo = lotteryCurrent.data();

        if (lotteryInfo?.status !== "initialAccepting") {
            console.log("lottery has already been drawn");
            return;
        }

        // If this task woke up early because endTimeLottery was > 700h away, re-enqueue for the next window
        const now = Date.now();
        if (typeof lotteryInfo?.endTimeLottery === "number" && now < lotteryInfo.endTimeLottery) {
            const nextLotteryTime = Math.min(lotteryInfo.endTimeLottery, now + MAX_TASK_WINDOW_MS);
            const queue = getFunctions().taskQueue(
                "projects/syntaxcamping/locations/northamerica-northeast1/functions/runLotteryDraw"
            );
            await queue.enqueue(
                {lotteryId},
                {
                    scheduleTime: new Date(nextLotteryTime),
                    dispatchDeadlineSeconds: 60 * 5,
                }
            );
            return;
        }

        const winners: LotteryEntry[] = [];
        const entriesCurrent = await lotteryReference
            .collection("entries")
            .get();

        const entries: LotteryEntry[] = entriesCurrent.docs.map((doc) => ({
            userId: doc.id,
            joinedTimestamp: doc.data().joinedTimestamp,
        }));

        const drawnIndexes = new Set<number>();
        const numberOfWinners = lotteryInfo?.numberOfWinners ?? 0;
        const amountOfWinners = Math.min(numberOfWinners, entries.length);
        const selectionTimestamp = Date.now();
        const responseDeadline = lotteryInfo?.endTimeBooking;

        while (winners.length < amountOfWinners) {
            const randomIndex = Math.floor(Math.random() * entries.length);
            if (!drawnIndexes.has(randomIndex)) {
                drawnIndexes.add(randomIndex);
                winners.push(entries[randomIndex]);
            }
        }

        await db.runTransaction(async (transaction) => {
            const currentLottery = await transaction.get(lotteryReference);
            const currentInfo = currentLottery.data();

            if (!currentLottery.exists || currentInfo?.status !== "initialAccepting") {
                console.log("Lottery has already been drawn.");
                return;
            }

            // Prepare winner confirmation refs and entrantData refs before writes
            const winnerRecords = winners.map((winner) => {
                const confirmationRef = db.collection("pendingConfirmations").doc();
                const winnerReference = lotteryReference
                    .collection("winners")
                    .doc(winner.userId);
                const entrantDataReference = db
                    .collection("entrantData")
                    .doc(winner.userId);

                return {
                    winner,
                    confirmationId: confirmationRef.id,
                    confirmationRef,
                    winnerReference,
                    entrantDataReference,
                };
            });

            // Read existing entrantData docs in the transaction so transaction rules stay happy
            const entrantSnaps = await Promise.all(
                winnerRecords.map((rec) => transaction.get(rec.entrantDataReference))
            );

            for (let i = 0; i < winnerRecords.length; i++) {
                const {
                    winner,
                    confirmationId,
                    confirmationRef,
                    winnerReference,
                    entrantDataReference,
                } = winnerRecords[i];
                const entrantSnap = entrantSnaps[i];

                // 1. Create pendingConfirmations/{confirmationId}
                transaction.set(confirmationRef, {
                    confirmationId: confirmationId,
                    lotteryId: lotteryId,
                    userId: winner.userId,
                    status: "pending",
                    selectionTimestamp: selectionTimestamp,
                    responseDeadline: responseDeadline,
                });

                // 2. Write lotteries/{lotteryId}/winners/{userId}
                transaction.set(winnerReference, {
                    userId: winner.userId,
                    confirmationId: confirmationId,
                    status: "pending",
                    selectionTimestamp: selectionTimestamp,
                    responseDeadline: responseDeadline,
                });

                // 3. Store confirmationId in entrantData/{userId}.entrantPendingEntryIds
                if (entrantSnap.exists) {
                    transaction.update(entrantDataReference, {
                        entrantPendingEntryIds: FieldValue.arrayUnion(confirmationId),
                    });
                } else {
                    transaction.set(entrantDataReference, {
                        entrantLotteryEntryIds: [],
                        entrantPendingEntryIds: [confirmationId],
                        entrantReservationIds: [],
                    });
                }
            }

            // Move everyone who wasn't selected to the waitlist
            for (let i = 0; i < entries.length; i++) {
                if (!drawnIndexes.has(i)) {
                    const entrant = entries[i];
                    const waitlistReference = lotteryReference
                        .collection("waitlist")
                        .doc(entrant.userId);

                    transaction.set(waitlistReference, {
                        userId: entrant.userId,
                        status: "waiting",
                        joinedTimestamp: entrant.joinedTimestamp,
                        addedByDraw: true,
                    });
                }
            }

            // Move the lottery into the booking stage
            transaction.update(lotteryReference, {
                status: "booking",
                drawCompletedAt: selectionTimestamp,
            });
        });
    }
);

export const closeLotteryBooking = onTaskDispatched(
    {
        region: "northamerica-northeast1",
        retryConfig: {
            maxAttempts: 3,
            minBackoffSeconds: 60,
        },
    },
    async (request) => {
        const lotteryId = request.data.lotteryId;
        const lotteryReference = db.collection("lotteries").doc(lotteryId);

        const lotteryCurrent = await lotteryReference.get();
        if (!lotteryCurrent.exists) {
            console.log("Lottery no longer exists.");
            return;
        }
        const lotteryInfo = lotteryCurrent.data();

        // If this task woke up early because endTimeBooking was > 700h away, re-enqueue for the next window
        const now = Date.now();
        if (typeof lotteryInfo?.endTimeBooking === "number" && now < lotteryInfo.endTimeBooking) {
            const nextBookingTime = Math.min(lotteryInfo.endTimeBooking, now + MAX_TASK_WINDOW_MS);
            const closingQueue = getFunctions().taskQueue(
                "projects/syntaxcamping/locations/northamerica-northeast1/functions/closeLotteryBooking"
            );
            await closingQueue.enqueue(
                {lotteryId},
                {
                    scheduleTime: new Date(nextBookingTime),
                    dispatchDeadlineSeconds: 60 * 5,
                }
            );
            return;
        }

        // Keep your existing closeLotteryBooking logic below this line
    }
);