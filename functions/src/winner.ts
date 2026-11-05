import{onDocumentUpdated} from "firebase-functions/v2/firestore";
import{onDocumentCreated} from "firebase-functions/v2/firestore";
import { getFirestore } from "firebase-admin/firestore";
const db = getFirestore();
    async function selectReplacement(
    lotteryId: string,
    rejectedWinnerId?: string
    ) {
    const lotteryReference = db.collection("lotteries").doc(lotteryId);

    await db.runTransaction(async (transaction) => {

    // Get lottery information
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

    //  If this was triggered by a rejection, check it hasn't already been processed.
    let rejectedWinnerReference = null;

    if (rejectedWinnerId) {
        rejectedWinnerReference = lotteryReference
            .collection("winners")
            .doc(rejectedWinnerId);

        const rejectedWinner = await transaction.get(
            rejectedWinnerReference
        );

        if (
            rejectedWinner.data()?.status !== "rejected" ||
            rejectedWinner.data()?.replacementProcessed === true
        ) {
            return;
        }
    }

    // Retrieve existing winners
    const winnersCurrent = await transaction.get(
    lotteryReference.collection("winners")
    );

    // Count confirmed and pending winners
    const validWinners = winnersCurrent.docs.filter(document => {
    const status = document.data().status;
    return status === "confirmed" || status === "pending";
    }).length;

    // 4. Calculate missing winner positions
    const missingSlots = numberOfWinners - validWinners;

    if (missingSlots <= 0) {
        console.log("All winner positions are filled.");

        if (rejectedWinnerReference) {
            transaction.update(rejectedWinnerReference, {
                replacementProcessed: true
            });
        }

        return;
    }

    // 5. Retrieve everyone currently on the waitlist
    const waitlistCurrent = await transaction.get(
        lotteryReference.collection("waitlist")
            .where("status", "==", "waiting")
    );

    if (waitlistCurrent.empty) {
        console.log("No one remains on the waitlist.");

        if (rejectedWinnerReference) {
            transaction.update(rejectedWinnerReference, {
                 replacementProcessed: true
            });
        }

        return;
    }

    // Determine how many people we can select
    const replacementsToDraw = Math.min(
        missingSlots,
        waitlistCurrent.docs.length
    );

    // Make a copy so we can remove selected people
    const availablePeople = [...waitlistCurrent.docs];

    //Randomly select replacements
    for (let i = 0; i < replacementsToDraw; i++) {

        const randomIndex = Math.floor(
            Math.random() * availablePeople.length
        );

        const selectedPerson = availablePeople.splice(
            randomIndex, 1
        )[0];

        const winnerReference = lotteryReference
            .collection("winners")
            .doc(selectedPerson.id);

        transaction.set(winnerReference, {
            userId: selectedPerson.id,
            status: "pending",
            selectionTimestamp: Date.now(),
            responseDeadline: lotteryInfo.endTimeBooking
        });

        // Remove selected person from waitlist
        transaction.delete(selectedPerson.ref);
    }

    // Mark the rejection as processed
    if (rejectedWinnerReference) {
        transaction.update(rejectedWinnerReference, {
            replacementProcessed: true
        });
    }

    });
}
export const onWaitlistEntryCreated = onDocumentCreated(
    "lotteries/{lotteryId}/waitlist/{userId}",
    async (event) => {

        const lotteryId = event.params.lotteryId;
        const waitlistEntry = event.data?.data();

        // Make sure this is a valid waitlist entry
        if (!waitlistEntry || waitlistEntry.status !== "waiting") {
            return;
        }



        // Check whether a winner position needs filling
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

            // When a winner's status changes from pending to rejected

            if (
                before.status === "pending" &&
                after.status === "rejected"
            ) {
                const lotteryId = event.params.lotteryId;
                const winnerId = event.params.winnerId;
                await selectReplacement(lotteryId,winnerId);

            }

        }

    );

