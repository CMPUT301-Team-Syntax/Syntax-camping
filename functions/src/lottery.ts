import {onDocumentCreated} from "firebase-functions/v2/firestore";
import {onTaskDispatched} from "firebase-functions/v2/tasks";
import {getFunctions} from "firebase-admin/functions";
import {getFirestore} from "firebase-admin/firestore";
const db = getFirestore();
interface LotteryEntry {
    userId: string;
    joinedTimestamp: number;
    }


export const scheduleLotteryDraw = onDocumentCreated(
    "lotteries/{lotteryId}",//makes sure document created is of lotteries type
    async (event) => {

        const snapshot = event.data;//get the data of the new lottery

        if (!snapshot) {
            console.log("No lottery document found.");
            return;
        }

        const lottery = snapshot.data();

        const lotteryId = event.params.lotteryId;
        const endTimeLottery = lottery.endTimeLottery;//gets lottery endtime
        if (typeof endTimeLottery !== "number" || !Number.isFinite(endTimeLottery)) {
             throw new Error("Invalid lottery endTime.");
        }

        const scheduleTime = new Date(endTimeLottery);//converts kotlin long to java date

        const queue = getFunctions().taskQueue(
            'projects/syntaxcamping/locations/northamerica-northeast1/functions/runLotteryDraw')
        console.log("In here?");
        console.log(scheduleTime)


        //
        await queue.enqueue(//Schedules this operation for when the time specified(endtime) while await makes sure the draw happens before firestores triggers finish
            {lotteryId},
            {
                scheduleTime: scheduleTime,
                dispatchDeadlineSeconds: 60 * 5,
            }
        );
    const closingQueue = getFunctions().taskQueue(
        "projects/syntaxcamping/locations/northamerica-northeast1/functions/closeLotteryBooking");
        const endTimeBooking = lottery.endTimeBooking;
        await closingQueue.enqueue(
            {lotteryId},
            {
                    scheduleTime:new Date(endTimeBooking),
                    dispatchDeadlineSeconds: 60*5
                    }
                );




    }
);
export const runLotteryDraw = onTaskDispatched(
{
    //TODO This will actually draw winners, once winners has been added

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
    const lotteryInfo = lotteryCurrent.data()
    //below gets the most current object representation of the entries list, so who is enrolled (basically gets the list as a whole)
    const entriesCurrent = await lotteryReference
        .collection("entries")
        .get();
    const entries: LotteryEntry[] = entriesCurrent.docs.map(doc => {
        return {
            userId: doc.id,
            joinedTimestamp: doc.data().joinedTimestamp};
            });
    const drawnIndexes = new Set<number>();// tracks which indexed entrants have already been drawn(prevents randomly drawing same user twice)
    const numberOfWinners = lotteryInfo?.numberOfWinners;
    const amountOfWinners = Math.min(numberOfWinners,entries.length);
    const selectionTimestamp = Date.now();
    const bookingEndDate = lotteryInfo?.endTimeBooking;
    const responseDeadline = bookingEndDate


    if (lotteryInfo?.status !== "initialAccepting"){
        console.log("lottery has already been drawn")//We cna use this open status later, as if we need to redraw a couple names cause people reject or whatever, we can change status to like new draw or smth
        return
    }
    while(winners.length< amountOfWinners){
        const randomIndex = Math.floor(Math.random() * entries.length);
        if (!drawnIndexes.has(randomIndex)){
            drawnIndexes.add(randomIndex);
            winners.push(entries[randomIndex]);
        }
    }

    await db.runTransaction(async (transaction) => {

        //Since time can pass between last time we got lottery info, and we want to ensure we are working with up to date stuff, we will reget certain values
        const currentLottery = await transaction.get(lotteryReference);
            const currentInfo = currentLottery.data();
        if (!currentLottery.exists ||
             currentInfo?.status !== "initialAccepting"){
            console.log("Lottery has already been drawn.");
            return;
        }

        // Saves the winners to the firebase
        for (const winner of winners) {
            const winnerReference = lotteryReference
                .collection("winners")
                .doc(winner.userId);
            transaction.set(winnerReference, {
                userId: winner.userId,
                status: "pending",// winners have status of pending until they accept their reservation/confirm reservation
                selectionTimestamp: selectionTimestamp,
                responseDeadline: responseDeadline

            });

        }

        // Move everyone who wasn't selected to the waitlist

        for (let i = 0; i < entries.length; i++) {
            if (!drawnIndexes.has(i)) {
                const entrant = entries[i];
                const waitlistReference = lotteryReference
                    .collection("waitlist")
                    .doc(entrant.userId);
                //moves everyone from entries list to waitlist with a waiting status
                transaction.set(waitlistReference, {
                    userId: entrant.userId,
                    status: "waiting",
                    joinedTimestamp: entrant.joinedTimestamp
                });

            }

    }

    // Move the lottery into the booking stage

    transaction.update(lotteryReference, {

        status: "booking",

        drawCompletedAt: selectionTimestamp

    });
});
// We will add the winner-selection logic here later.
}
);