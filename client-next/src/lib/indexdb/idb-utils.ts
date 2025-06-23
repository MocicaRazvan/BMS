"use client";
import { openDB, IDBPDatabase } from "idb";

const DB_NAME = "BMS_DB";
const STORE_NAME = "BMS_STORAGE";
const TIMESTAMP_INDEX = "by-timestamp";
const DB_VERSION = process.env.INDEXEDB_VERSION
  ? parseInt(process.env.INDEXEDB_VERSION, 10)
  : 1;

type CachePayload = {
  value: unknown[][];
  timestamp: number;
};
let dbPromise: Promise<IDBPDatabase> | null = null;

async function getDB(): Promise<IDBPDatabase> {
  if (!dbPromise) {
    dbPromise = openDB(DB_NAME, DB_VERSION, {
      upgrade(db) {
        if (!db.objectStoreNames.contains(STORE_NAME)) {
          const store = db.createObjectStore(STORE_NAME);
          store.createIndex(TIMESTAMP_INDEX, "timestamp");
        }
      },
    });
  }
  return dbPromise;
}
export async function saveToIndexedDB(
  key: string,
  value: unknown[][],
): Promise<void> {
  const db = await getDB();
  const payload: CachePayload = {
    value,
    timestamp: Date.now(),
  };
  await db.put(STORE_NAME, payload, key);
}
export async function deleteFromIndexedDB(key: string): Promise<void> {
  const db = await getDB();
  await db.delete(STORE_NAME, key);
}
export async function clearIndexedDB(): Promise<void> {
  const db = await getDB();
  await db.clear(STORE_NAME);
}
export async function loadCacheFromIndexedDB(
  afterTimestamp: number,
): Promise<Record<string, unknown[][]>> {
  const db = await getDB();
  const entries: Record<string, unknown[][]> = {};
  const tx = db.transaction(STORE_NAME, "readwrite");
  const store = tx.objectStore(STORE_NAME);
  const index = store.index(TIMESTAMP_INDEX);

  const range = IDBKeyRange.lowerBound(afterTimestamp + 1);
  let cursor = await index.openCursor(range);
  while (cursor) {
    const key = cursor.primaryKey as string;
    const { value } = cursor.value as CachePayload;
    entries[key] = value;
    cursor = await cursor.continue();
  }

  const staleRange = IDBKeyRange.upperBound(afterTimestamp);
  let staleCursor = await index.openCursor(staleRange);
  while (staleCursor) {
    await staleCursor.delete();
    staleCursor = await staleCursor.continue();
  }

  await tx.done;
  return entries;
}
const pendingWrites = new Map<string, unknown[][]>();
let flushTimeout: ReturnType<typeof setTimeout> | null = null;
async function flushPendingWrites(): Promise<void> {
  const db = await getDB();
  const tx = db.transaction(STORE_NAME, "readwrite");
  const store = tx.objectStore(STORE_NAME);

  for (const [key, value] of pendingWrites.entries()) {
    const payload: CachePayload = {
      value,
      timestamp: Date.now(),
    };
    store.put(payload, key);
  }

  pendingWrites.clear();
  await tx.done;
  flushTimeout = null;
}
function scheduleWriteFlush() {
  if (flushTimeout) return;
  flushTimeout = setTimeout(() => {
    void flushPendingWrites().catch(console.error);
  }, 50);
}
export function saveToIndexedDBBatched(key: string, value: unknown[][]) {
  try {
    pendingWrites.set(key, value);
    scheduleWriteFlush();
  } catch (error) {
    console.error("Error saving to IndexedDB:", error);
  }
}

export async function dumpToCache(data: Iterable<[string, unknown[][]]>) {
  const db = await getDB();
  const tx = db.transaction(STORE_NAME, "readwrite", {
    durability: "relaxed",
  });
  const store = tx.objectStore(STORE_NAME);
  const timestamp = Date.now();

  for (const [key, value] of data) {
    const payload: CachePayload = {
      value,
      timestamp,
    };
    store.put(payload, key);
  }

  await tx.done;
}
