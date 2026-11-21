import { onDocumentUpdated, onDocumentCreated } from "firebase-functions/v2/firestore";
import { getFirestore } from "firebase-admin/firestore";

const db = getFirestore();

async function selectReplacement(
    lotteryId: string,
    rejectedWinnerId?: string
) {
    const lotteryReference = db.collection("lotteries").doc(lotteryId);

    await db.runTransaction(async (transaction) => {
        // 1. Get lottery information
        const lotteryCurrent = await transaction.get(lotteryReference);
        const lotteryInfo = lotteryCurrent.data();

        // Only replace winners during booking
        if (!lotteryCurrent.exists || lotteryInfo?.status !== "booking") {
            return;
        }

        const numberOfWinners = lotteryInfo.numberOfWinners;
        if (typeof numberOfWinners !== "number") {
            return;
        }

        // 2. If triggered by a rejection/cancellation, make sure it hasn't already been processed
        let rejectedWinnerReference = null;

        if (rejectedWinnerId) {
            rejectedWinnerReference = lotteryReference
                .collection("winners")
                .doc(rejectedWinnerId);

            const rejectedWinner = await transaction.get(rejectedWinnerReference);
            const rejectedWinnerData = rejectedWinner.data();
            const isRejectedStatus =
                rejectedWinnerData?.status === "rejected" ||
                rejectedWinnerData?.status === "cancelled";

            if (!isRejectedStatus || rejectedWinnerData?.replacementProcessed === true) {
                return;
            }
        }

        // 3. Retrieve existing winners
        const winnersCurrent = await transaction.get(
            lotteryReference.collection("winners")
        );

        // Count confirmed and pending winners
        const validWinners = winnersCurrent.docs.filter((document) => {
            const status = document.data().status;
            return status === "confirmed" || status === "pending";
        }).length;

        // 4. Calculate missing winner positions
        const missingSlots = numberOfWinners - validWinners;

        if (missingSlots <= 0) {
            console.log("All winner positions are filled.");

            if (rejectedWinnerReference) {
                transaction.update(rejectedWinnerReference, {
                    replacementProcessed: true,
                });
            }

            return;
        }

        // 5. Retrieve everyone currently on the waitlist
        const waitlistCurrent = await transaction.get(
            lotteryReference
                .collection("waitlist")
                .where("status", "==", "waiting")
        );

        if (waitlistCurrent.empty) {
            console.log("No one remains on the waitlist.");

            if (rejectedWinnerReference) {
                transaction.update(rejectedWinnerReference, {
                    replacementProcessed: true,
                });
            }

            return;
        }

        // 6. Determine how many replacements to draw
        const replacementsToDraw = Math.min(
            missingSlots,
            waitlistCurrent.docs.length
        );

        const availablePeople = [...waitlistCurrent.docs];

        for (let i = 0; i < replacementsToDraw; i++) {
            const randomIndex = Math.floor(Math.random() * availablePeople.length);
            const selectedPerson = availablePeople.splice(randomIndex, 1)[0];

            const winnerReference = lotteryReference
                .collection("winners")
                .doc(selectedPerson.id);

            transaction.set(winnerReference, {
                userId: selectedPerson.id,
                status: "pending",
                selectionTimestamp: Date.now(),
                responseDeadline: lotteryInfo.endTimeBooking,
            });

            const pendingConfirmReference = db
                .collection("pendingConfirmations")
                .doc(`${lotteryId}_${selectedPerson.id}`);

            transaction.set(pendingConfirmReference, {
                lotteryId: lotteryId,
                userId: selectedPerson.id,
            });

            transaction.delete(selectedPerson.ref);
        }

        if (rejectedWinnerReference) {
            transaction.update(rejectedWinnerReference, {
                replacementProcessed: true,
            });
        }
    });
}

export const onWaitlistEntryCreated = onDocumentCreated(
    "lotteries/{lotteryId}/waitlist/{userId}",
    async (event) => {
        const lotteryId = event.params.lotteryId;
        const waitlistEntry = event.data?.data();

        if (!waitlistEntry || waitlistEntry.status !== "waiting") {
            return;
        }

        await selectReplacement(lotteryId);
    }
);

export const handleWinnerStatusChange = onDocumentUpdated(
    "lotteries/{lotteryId}/winners/{winnerId}",
    async (event) => {
        const before = event.data?.before.data();
        const after = event.data?.after.data();
        if (!before || !after) {
            return;
        }

        const rejectedStatuses = new Set(["rejected", "cancelled"]);
        const wasActive = before.status === "pending" || before.status === "confirmed";
        const isNowRejected = rejectedStatuses.has(after.status);

        if (wasActive && isNowRejected) {
            const lotteryId = event.params.lotteryId;
            const winnerId = event.params.winnerId;
            await selectReplacement(lotteryId, winnerId);
        }
    }
);