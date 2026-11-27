const {initializeApp} = require("firebase-admin/app");
const assert = require("node:assert/strict");
const {test} = require("node:test");

initializeApp({projectId: "demo-syntax-camping"});
const {getAdminNotificationLogs} = require("../lib/notificationAudit.js");

const data = {scope: "campsite", id: "river"};

test("unauthenticated clients cannot read audit logs", async () => {
    await assert.rejects(getAdminNotificationLogs.run({data}), {code: "unauthenticated"});
});

test("a signed-in user needs a boolean administrator claim", async () => {
    for (const claim of [undefined, false, "true"]) {
        const auth = {uid: "entrant", token: {admin: claim}};
        await assert.rejects(getAdminNotificationLogs.run({data, auth}), {code: "permission-denied"});
    }
});

test("invalid queries are rejected before any Firestore access", async () => {
    const auth = {uid: "staff", token: {admin: true}};
    for (const invalid of [
        {scope: "global", id: "river"},
        {scope: "owner", id: "wrong/path"},
        {...data, cursor: {id: "record", occurredAt: "yesterday"}},
    ]) {
        await assert.rejects(getAdminNotificationLogs.run({data: invalid, auth}), {code: "invalid-argument"});
    }
});

test("a registered notification appears once in the right campsite and owner logs", {
    skip: !process.env.FIRESTORE_EMULATOR_HOST,
}, async () => {
    const {randomUUID} = require("node:crypto");
    const {getFirestore} = require("firebase-admin/firestore");
    const {logLotteryRegistration} = require("../lib/notificationAudit.js");
    const firestore = getFirestore();
    const ownerId = randomUUID();
    const campsiteId = randomUUID();
    const otherId = randomUUID();
    const lotteryId = randomUUID();
    const lottery = firestore.collection("lotteries").doc(lotteryId);

    await Promise.all([
        firestore.collection("users").doc(ownerId).set({displayName: "Ada"}),
        firestore.collection("campsites").doc(campsiteId).set({name: "River Camp", ownerId, lotteryId}),
        firestore.collection("campsites").doc(otherId).set({name: "Pines Camp", ownerId, lotteryId}),
        lottery.set({name: "Summer draw", ownerId}),
    ]);
    const entry = lottery.collection("entries").doc("entrant");
    await entry.set({notificationCampsiteId: campsiteId});
    const event = {id: randomUUID(), params: {lotteryId, userId: "entrant"}, data: await entry.get()};
    await logLotteryRegistration.run(event);
    await logLotteryRegistration.run(event);

    const auth = {uid: "staff", token: {admin: true}};
    const load = (scope, id) => getAdminNotificationLogs.run({data: {scope, id}, auth});
    const [campsite, owner, unrelated] = await Promise.all([
        load("campsite", campsiteId), load("owner", ownerId), load("campsite", otherId),
    ]);
    assert.equal(campsite.name, "River Camp");
    assert.equal(owner.name, "Ada");
    assert.equal(campsite.rows.length, 1);
    assert.equal(unrelated.rows.length, 0);
    assert.deepEqual(owner.rows, campsite.rows);
    assert.equal(owner.rows[0].campsiteName, "River Camp");
    assert.ok(Number.isSafeInteger(owner.rows[0].occurredAt));
});
