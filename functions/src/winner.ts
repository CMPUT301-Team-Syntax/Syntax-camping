import{onDocumentUpdated} from "firebase-functions/v2/firestore";
import { getFirestore } from "firebase-admin/firestore";
const db = getFirestore();
async function selectReplacement(lotteryId: string,rejectedWinnerId:string) {
    const lotteryReference = db.collection("lotteries").doc(lotteryId);
    await db.runTransaction(async (transaction) =>{
        // Get the lottery information
        const lotteryCurrent = await transaction.get(lotteryReference)
        const lotteryInfo = lotteryCurrent.data();
        //Checks to see if the lottery booking phase has ended, adn if it does, does nto draw a replacement
        if (!lotteryCurrent.exists || lotteryInfo?.status !== "booking") {
            console.log("Lottery is no longer accepting replacements.");
            return;
        }
        const rejectedWinnerReference = lotteryReference
            .collection("winners")
            .doc(rejectedWinnerId);
        const rejectedWinner = await transaction.get(rejectedWinnerReference);
        if (rejectedWinner.data()?.status !== "rejected" ||
            rejectedWinner.data()?.replacementProcessed ===true){
            return;
            }

        // Get everyone currently on the waitlist
        const waitlistCurrent = await transaction.get(
            lotteryReference.collection("waitlist")
                .where("status", "==", "waiting")
                );

        if (waitlistCurrent.empty) {
            console.log("No one remains on the waitlist.");
            return;
        }
        // Randomly select someone
        const randomIndex = Math.floor(Math.random() * waitlistCurrent.docs.length);
        const selectedPerson = waitlistCurrent.docs[randomIndex];
        const winnerReference = lotteryReference
            .collection("winners")
            .doc(selectedPerson.id);
        transaction.set(winnerReference, {
            userId: selectedPerson.Id,
            status: "pending",
            selectionTimestamp: Date.now(),
            responseDeadline: lotteryInfo.bookingEndDate
            });
        transaction.update(rejectedWinnerReference, {
            replacementProcessed: true});
        transaction.delete(selectedPerson.ref);
    }
}



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