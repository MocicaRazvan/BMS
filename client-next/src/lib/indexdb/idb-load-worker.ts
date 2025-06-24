"use client";

import {
  clearIndexedDB,
  dumpToCache,
  loadCacheFromIndexedDB,
} from "@/lib/indexdb/idb-utils";
import {
  IdbIncomingMessage,
  IdbMessageType,
} from "@/lib/indexdb/idb-worker-types";

addEventListener("message", async (event: MessageEvent<IdbIncomingMessage>) => {
  if (!event.data) {
    return;
  }
  try {
    switch (event.data.type) {
      case "loadCache":
        const entries = await loadCacheFromIndexedDB(event.data.afterTimestamp);
        postMessage({
          status: "success",
          entries,
          type: IdbMessageType.LOAD_CACHE,
        });
        break;
      case "dumpCache":
        await dumpToCache(event.data.payload);
        postMessage({
          status: "success",
          type: IdbMessageType.DUMP_CACHE,
        });
        break;
      case "clearCache":
        await clearIndexedDB();
        postMessage({
          status: "success",
          type: IdbMessageType.CLEAR_CACHE,
        });
    }
  } catch (error) {
    postMessage({
      status: "error",
      error,
    });
  }
});
