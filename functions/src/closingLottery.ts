import { onTaskDispatched } from "firebase-functions/v2/tasks";
import { getFirestore } from "firebase-admin/firestore";

const db = getFirestore();
export const closeLotteryBooking = onTaskDispatched(
    async (event) => {
        const lotteryId = event.data.lotteryId;

        const lotteryReference = db.collection("lotteries")
        .doc(lotteryId);

        await db.runTransaction(async (transaction) => {

            const lotteryCurrent = await transaction.get(lotteryReference);
            const lotteryInfo = lotteryCurrent.data();

            if (
                !lotteryCurrent.exists ||
                lotteryInfo?.status !== "booking"
            ) {
                console.log("Lottery is not in the booking phase.");
                return;
            }

            transaction.update(lotteryReference, {
                status: "complete"
            });

            console.log("Lottery booking has closed:", lotteryId);
        });
    }
);