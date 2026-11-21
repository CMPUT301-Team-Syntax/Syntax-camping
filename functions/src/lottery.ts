import {onDocumentCreated} from "firebase-functions/v2/firestore";
import {onTaskDispatched} from "firebase-functions/v2/tasks";
import {getFunctions} from "firebase-admin/functions";
import {getFirestore, FieldValue} from "firebase-admin/firestore";

const db = getFirestore();

interface LotteryEntry {
    userId: string;
    joinedTimestamp: number;
}

export const scheduleLotteryDraw = onDocumentCreated(
    "lotteries/{lotteryId}",
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

        const scheduleTime = new Date(endTimeLottery);
        const queue = getFunctions().taskQueue(
            "projects/syntaxcamping/locations/northamerica-northeast1/functions/runLotteryDraw"
        );

        await queue.enqueue(
            {lotteryId},
            {
                scheduleTime: scheduleTime,
                dispatchDeadlineSeconds: 60 * 5,
            }
        );

        const closingQueue = getFunctions().taskQueue(
            "projects/syntaxcamping/locations/northamerica-northeast1/functions/closeLotteryBooking"
        );
        const endTimeBooking = lottery.endTimeBooking;

        await closingQueue.enqueue(
            {lotteryId},
            {
                scheduleTime: new Date(endTimeBooking),
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
        const winners: LotteryEntry[] = [];

        const lotteryCurrent = await lotteryReference.get();
        const lotteryInfo = lotteryCurrent.data();

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

        if (lotteryInfo?.status !== "initialAccepting") {
            console.log("lottery has already been drawn");
            return;
        }

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