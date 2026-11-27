import {onSchedule} from "firebase-functions/v2/scheduler";
import {onDocumentWritten} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import {getFirestore} from "firebase-admin/firestore";

const db = getFirestore();

// Adjust to your campsite/app timezone (e.g., "America/Edmonton")
const APP_TIME_ZONE = "America/Edmonton";

export type ReservationStatus = "upcoming" | "current" | "past";

export interface DateBounds {
  earliest: Date;
  latest: Date;
  earliestKey: number; // YYYYMMDD numeric key for safe comparison
  latestKey: number;   // YYYYMMDD numeric key for safe comparison
}

/**
 * Parses a "ddMMyy" string (e.g., "241126") into year, month (1-12), day (1-31).
 * Returns null if invalid.
 */
function parseDdMmYy(dateStr: string): {year: number; month: number; day: number} | null {
  const match = /^(\d{2})\/?(\d{2})\/?(\d{2})$/.exec(dateStr.trim());
  if (!match) return null;

  const day = Number(match[1]);
  const month = Number(match[2]);
  const twoDigitYear = Number(match[3]);
  const year = 2000 + twoDigitYear;

  if (month < 1 || month > 12 || day < 1 || day > 31) return null;

  // Verify real calendar date (e.g., rejects 31/02/26)
  const check = new Date(Date.UTC(year, month - 1, day));
  if (
    check.getUTCFullYear() !== year ||
    check.getUTCMonth() !== month - 1 ||
    check.getUTCDate() !== day
  ) {
    return null;
  }

  return {year, month, day};
}

function toDateKey(year: number, month: number, day: number): number {
  return year * 10000 + month * 100 + day;
}

/**
 * Gets today's calendar date (YYYYMMDD) in APP_TIME_ZONE.
 */
export function getTodayKey(now: Date = new Date(), timeZone: string = APP_TIME_ZONE): number {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(now);

  const year = Number(parts.find((p) => p.type === "year")?.value);
  const month = Number(parts.find((p) => p.type === "month")?.value);
  const day = Number(parts.find((p) => p.type === "day")?.value);

  return toDateKey(year, month, day);
}

/**
 * Finds the earliest and latest dates from a list of "ddMMyy" strings.
 */
export function getEarliestAndLatestDates(datesReserved: unknown): DateBounds | null {
  if (!Array.isArray(datesReserved) || datesReserved.length === 0) {
    return null;
  }

  let minKey = Number.POSITIVE_INFINITY;
  let maxKey = Number.NEGATIVE_INFINITY;
  let earliestDate: Date | null = null;
  let latestDate: Date | null = null;

  for (const raw of datesReserved) {
    if (typeof raw !== "string") continue;
    const parsed = parseDdMmYy(raw);
    if (!parsed) {
      logger.warn("Skipping invalid ddMMyy date string:", raw);
      continue;
    }

    const key = toDateKey(parsed.year, parsed.month, parsed.day);
    const utcMidnight = new Date(Date.UTC(parsed.year, parsed.month - 1, parsed.day));

    if (key < minKey) {
      minKey = key;
      earliestDate = utcMidnight;
    }
    if (key > maxKey) {
      maxKey = key;
      latestDate = utcMidnight;
    }
  }

  if (!earliestDate || !latestDate) {
    return null;
  }

  return {
    earliest: earliestDate,
    latest: latestDate,
    earliestKey: minKey,
    latestKey: maxKey,
  };
}

/**
 * Computes the expected status based on today's calendar date.
 * - today < earliestKey  -> "upcoming"
 * - earliestKey <= today <= latestKey -> "current"
 * - today > latestKey   -> "past"
 */
export function computeReservationStatus(bounds: DateBounds, todayKey: number): ReservationStatus {
  if (todayKey < bounds.earliestKey) {
    return "upcoming";
  }
  if (todayKey > bounds.latestKey) {
    return "past";
  }
  return "current";
}

/**
 * Updates both /reservations/{id} and /campsites/{campsiteId}/reservations/{id}
 * when the computed status differs from the current status.
 */
async function applyStatusAndBounds(
  reservationId: string,
  campsiteId: string | undefined,
  datesReserved: unknown,
  currentStatus: string | undefined,
  todayKey: number
): Promise<void> {
  const bounds = getEarliestAndLatestDates(datesReserved);
  if (!bounds) return;

  const nextStatus = computeReservationStatus(bounds, todayKey);
  if (currentStatus === nextStatus) return;

  const updates: Record<string, unknown> = {
    status: nextStatus,
  };

  const batch = db.batch();
  const topLevelRef = db.collection("reservations").doc(reservationId);
  batch.set(topLevelRef, updates, {merge: true});

  if (campsiteId && campsiteId.trim().length > 0) {
    const subcollectionRef = db
      .collection("campsites")
      .doc(campsiteId)
      .collection("reservations")
      .doc(reservationId);
    batch.set(subcollectionRef, updates, {merge: true});
  }

  await batch.commit();
  logger.info(`Reservation ${reservationId} status updated: ${currentStatus} -> ${nextStatus}`);
}

/**
 * 1. Runs whenever a reservation is created or updated so its status is immediately accurate.
 */
export const syncReservationStatusOnWrite = onDocumentWritten(
  "reservations/{reservationId}",
  async (event) => {
    const after = event.data?.after;
    if (!after || !after.exists) return;

    const data = after.data();
    if (!data) return;

    const todayKey = getTodayKey();
    await applyStatusAndBounds(
      event.params.reservationId,
      data.campsiteId as string | undefined,
      data.datesReserved,
      data.status as string | undefined,
      todayKey
    );
  }
);

/**
 * 2. Scheduled daily sweep (00:05 in APP_TIME_ZONE) to transition
 *    "upcoming" -> "current" and "current" -> "past" as calendar days pass.
 */
export const updateReservationStatusesDaily = onSchedule(
  {
    schedule: "5 0 * * *",
    timeZone: APP_TIME_ZONE,
  },
  async () => {
    const todayKey = getTodayKey();
    logger.info(`Running daily reservation status update for dateKey=${todayKey}`);

    const snapshot = await db
      .collection("reservations")
      .where("status", "in", ["upcoming", "current"])
      .get();

    if (snapshot.empty) {
      logger.info("No upcoming or current reservations to evaluate.");
      return;
    }

    const docs = snapshot.docs;
    const chunkSize = 200;

    for (let i = 0; i < docs.length; i += chunkSize) {
      const chunk = docs.slice(i, i + chunkSize);
      const batch = db.batch();
      let batchHasWrites = false;

      for (const docSnap of chunk) {
        const data = docSnap.data();
        const bounds = getEarliestAndLatestDates(data.datesReserved);
        if (!bounds) continue;

        const nextStatus = computeReservationStatus(bounds, todayKey);
        if (data.status === nextStatus) continue;

        batch.set(docSnap.ref, {status: nextStatus}, {merge: true});
        batchHasWrites = true;

        const campsiteId = data.campsiteId as string | undefined;
        if (campsiteId && campsiteId.trim().length > 0) {
          const subRef = db
            .collection("campsites")
            .doc(campsiteId)
            .collection("reservations")
            .doc(docSnap.id);
          batch.set(subRef, {status: nextStatus}, {merge: true});
        }
      }

      if (batchHasWrites) {
        await batch.commit();
      }
    }

    logger.info("Finished daily reservation status update.");
  }
);