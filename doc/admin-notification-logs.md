# Admin notification logs

Issue #39 adds two entry points. In **Browse ALL Campsites**, open a campsite card menu and choose **View notifications**. In **Profiles**, open the owner's profile and choose **View notification logs**. The first view shows the selected campsite. The second groups results by campsite and includes records stored for that owner at the time of the event.

## Firebase setup

1. In Firebase Authentication, enable the Email/Password provider and create accounts for the authorized administrators. Set the boolean custom claim `admin: true` on those accounts with the Firebase Admin SDK. Users sign in using **Settings → Login as admin**. Sign out and back in after assigning the claim.
2. Protect the entire `notificationAudit` collection and its descendants from direct Firestore client access in the project's current Security Rules. The audit functions write with the Admin SDK and the callable checks the authenticated admin claim on every request. A broad allow rule can override an explicit deny, so exclude `notificationAudit` from any overlapping permissive match. Verify that neither a regular client nor an admin client can read or write this collection directly.
3. Build the Cloud Functions project and deploy `getAdminNotificationLogs`, `logLotteryRegistration`, `logLotteryWaitlist`, `logLotteryWinner`, and `logLotteryOutcome` from the team's configured Firebase deployment workspace in `northamerica-northeast1`. Confirm the Android app points at that same project.

Run a lottery with test accounts after deployment and check both entry points. Verify the callable rejects unauthenticated accounts and signed-in accounts without the admin claim. The logs cover lottery registrations, direct waitlist registrations, winner selections, draw waitlist outcomes and lottery completion. They record events from deployment forward. Earlier notifications stored on individual devices are outside the audit history. Chat messages use a separate flow.
