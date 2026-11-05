/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import {initializeApp} from "firebase-admin/app";
import {setGlobalOptions} from "firebase-functions";

initializeApp();



setGlobalOptions({maxInstances: 10,
    region: "northamerica-northeast1"});

export {
    scheduleLotteryDraw,
    runLotteryDraw} from "./lottery";
   export {
       handleWinnerStatusChange,
       onWaitlistEntryCreated
   } from "./winner";
export{
    closeLotteryBooking
    } from "./closingLottery";
//initialization stuff
//const db = getFirestore();
//const tasksClient = new CloudTasksClient();
//Everytime a lottery is created, runs the following




