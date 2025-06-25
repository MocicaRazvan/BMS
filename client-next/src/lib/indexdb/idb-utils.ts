"use client";
import { IDBPDatabase, openDB } from "idb";

const DB_NAME = "BMS_DB";
const STORE_NAME = "BMS_STORAGE";
const TIMESTAMP_INDEX_EMAIL_INDEX = "by-email-timestamp";
const DB_VERSION = process.env.INDEXEDB_VERSION
  ? parseInt(process.env.INDEXEDB_VERSION, 10)
  : 1;

type CachePayload = {
  value: unknown[][];
  timestamp: number;
};
async function deriveKey(email: string): Promise<CryptoKey> {
  const encoder = new TextEncoder();
  const keyMaterial = await crypto.subtle.importKey(
    "raw",
    encoder.encode(email),
    "PBKDF2",
    false,
    ["deriveKey"],
  );
  return crypto.subtle.deriveKey(
    {
      name: "PBKDF2",
      salt: encoder.encode("fixed-app-salt"),
      iterations: 1000,
      hash: "SHA-256",
    },
    keyMaterial,
    { name: "AES-GCM", length: 256 },
    false,
    ["encrypt", "decrypt"],
  );
}

const derivedKeys = new Map<string, CryptoKey>();

export async function getDerivedKey(email: string): Promise<CryptoKey> {
  if (derivedKeys.has(email)) {
    return derivedKeys.get(email)!;
  }
  const key = await deriveKey(email);
  derivedKeys.set(email, key);
  return key;
}

type EncryptedCachePayload = {
  value: number[];
  iv: number[];
  timestamp: number;
  userEmail: string;
};
const textEncoder = new TextEncoder();
const textDecoder = new TextDecoder();

async function encryptPayload(
  data: unknown,
  key: CryptoKey,
): Promise<{ encrypted: Uint8Array; iv: Uint8Array }> {
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const encoded = textEncoder.encode(JSON.stringify(data));
  const encrypted = await crypto.subtle.encrypt(
    { name: "AES-GCM", iv },
    key,
    encoded,
  );
  return { encrypted: new Uint8Array(encrypted), iv };
}

async function decryptPayload(
  encrypted: Uint8Array,
  iv: Uint8Array,
  key: CryptoKey,
): Promise<unknown | null> {
  try {
    const decrypted = await crypto.subtle.decrypt(
      { name: "AES-GCM", iv },
      key,
      encrypted,
    );
    return JSON.parse(textDecoder.decode(decrypted));
  } catch (err) {
    console.warn("Decryption failed, possible tampering or stale key:", err);
    return null;
  }
}

let dbPromise: Promise<IDBPDatabase> | null = null;

async function getDB(): Promise<IDBPDatabase> {
  if (!dbPromise) {
    dbPromise = openDB(DB_NAME, DB_VERSION, {
      upgrade(db) {
        if (!db.objectStoreNames.contains(STORE_NAME)) {
          const store = db.createObjectStore(STORE_NAME);
          store.createIndex(
            TIMESTAMP_INDEX_EMAIL_INDEX,
            ["userEmail", "timestamp"],
            {
              unique: false,
            },
          );
        }
      },
    });
  }
  return dbPromise;
}
export async function saveToIndexedDB(
  key: string,
  value: unknown[][],
  userEmail: string,
): Promise<void> {
  const db = await getDB();
  const cryptoKey = await getDerivedKey(userEmail);
  const { encrypted, iv } = await encryptPayload(value, cryptoKey);
  const payload: EncryptedCachePayload = {
    value: Array.from(encrypted),
    iv: Array.from(iv),
    timestamp: Date.now(),
    userEmail,
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
  userEmail: string,
): Promise<Record<string, unknown[][]>> {
  const cryptoKey = await getDerivedKey(userEmail);
  const db = await getDB();
  const entries: Record<string, EncryptedCachePayload> = {};
  const tx = db.transaction(STORE_NAME, "readwrite");
  const store = tx.objectStore(STORE_NAME);
  const index = store.index(TIMESTAMP_INDEX_EMAIL_INDEX);

  const range = IDBKeyRange.bound(
    [userEmail, afterTimestamp + 1],
    [userEmail, Infinity],
  );
  let cursor = await index.openCursor(range);
  while (cursor) {
    const key = cursor.primaryKey as string;
    entries[key] = cursor.value as EncryptedCachePayload;

    cursor = await cursor.continue();
  }

  const staleRange = IDBKeyRange.bound(
    [userEmail, 0],
    [userEmail, afterTimestamp],
  );
  let staleCursor = await index.openCursor(staleRange);
  while (staleCursor) {
    await staleCursor.delete();
    staleCursor = await staleCursor.continue();
  }

  await tx.done;
  return await Promise.all(
    Object.entries(entries).map(async ([key, payload]) => {
      const decryptedValue = await decryptPayload(
        new Uint8Array(payload.value),
        new Uint8Array(payload.iv),
        cryptoKey,
      );
      return [key, decryptedValue] as [string, unknown[][] | null];
    }),
  ).then((entries) =>
    entries.reduce<Record<string, unknown[][]>>((acc, [key, value]) => {
      if (value !== null) {
        acc[key] = value;
      }
      return acc;
    }, {}),
  );
}

export async function dumpToCache(
  data: Iterable<[string, unknown[][]]>,
  userEmail: string,
) {
  const cryptoKey = await getDerivedKey(userEmail);
  const timestamp = Date.now();
  const encryptedData: [string, EncryptedCachePayload][] = await Promise.all(
    Array.from(data).map(async ([key, value]) => {
      const { encrypted, iv } = await encryptPayload(value, cryptoKey);
      const data: EncryptedCachePayload = {
        value: Array.from(encrypted),
        iv: Array.from(iv),
        timestamp,
        userEmail,
      };
      return [key, data];
    }),
  );
  const db = await getDB();
  const tx = db.transaction(STORE_NAME, "readwrite", { durability: "relaxed" });
  const store = tx.objectStore(STORE_NAME);

  for (const [key, payload] of encryptedData) {
    store.put(payload, key);
  }

  await tx.done;
}
