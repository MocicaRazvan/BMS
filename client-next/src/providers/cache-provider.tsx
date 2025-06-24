"use client";
import {
  createContext,
  ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
} from "react";
import { isDeepEqual } from "@/lib/utils";
import { LRUCache } from "lru-cache";
import { Subscription, useSubscription } from "@/lib/fetchers/use-subscription";
import { useEvent } from "react-use";
import {
  DumpCacheIncomingMessage,
  IdbMessageResponse,
  IdbMessageType,
  LoadCacheIncomingMessage,
} from "@/lib/indexdb/idb-worker-types";
import { throttle } from "lodash-es";

const maxLRUCacheItems = process.env.NEXT_PUBLIC_MAX_LRU_CACHE_ITEMS
  ? parseInt(process.env.NEXT_PUBLIC_MAX_LRU_CACHE_ITEMS, 10)
  : 250;
const maxLRUCacheSize = process.env.NEXT_PUBLIC_MAX_LRU_CACHE_SIZE
  ? parseInt(process.env.NEXT_PUBLIC_MAX_LRU_CACHE_SIZE, 10)
  : 5000;
const ttlLRUCache = process.env.NEXT_PUBLIC_TTL_LRU_CACHE
  ? parseInt(process.env.NEXT_PUBLIC_TTL_LRU_CACHE, 10) * 1000
  : 1000 * 60 * 5; //5 mins

function countElements(arr: unknown[]): number {
  let count = 0;
  for (const el of arr) {
    if (!Array.isArray(el)) {
      count += 1;
    } else {
      count += countElements(el);
    }
  }
  return count;
}

type ClientCacheListener = () => void;

export class ClientCacheInstance {
  private static instance: ClientCacheInstance;
  private readonly cache: LRUCache<string, unknown[][]>;

  private readonly listeners: Map<string, Set<ClientCacheListener>>;
  private readonly reactSafeStore: Map<string, unknown[]>;
  public static readonly EMPTY_ARRAY = [];

  private constructor() {
    this.listeners = new Map();
    this.reactSafeStore = new Map();
    this.cache = new LRUCache({
      max: maxLRUCacheItems,
      maxSize: maxLRUCacheSize,
      sizeCalculation: (v, _) => countElements(v) || 1,
      ttl: ttlLRUCache,
      allowStale: false,
      updateAgeOnGet: true,
      updateAgeOnHas: false,
      // dispose: (v, k, r) => {
      //   if (r !== "set") {
      //     // this.flatCache.delete(k);
      //     this.emitChange(k);
      //   }
      // },
      disposeAfter: (v, k, r) => {
        if (r === "delete") {
          // console.log("Cache Disposed delete:", k, r, v);
          // this.flatCache.delete(k);
          this.reactSafeStore.delete(k);
          this.emitChange(k);
        }
        // deleteFromIndexedDB(k).catch((err) => {
        //   console.error("Error deleting from IndexedDB:", err);
        // });
      },
      onInsert: (value, key, reason) => {
        // console.log("Cache Inserted:", key, reason);
        // this.flatCache.set(key, value ? value.flat() : this.EMPTY_ARRAY);
        this.reactSafeStore.set(key, value.flat());
        this.emitChange(key);
        // saveToIndexedDBBatched(key, value);
      },
    });
  }

  static getInstance(): ClientCacheInstance {
    if (!ClientCacheInstance.instance) {
      ClientCacheInstance.instance = new ClientCacheInstance();
    }
    return ClientCacheInstance.instance;
  }

  public get<T>(key: string): T[] | undefined {
    const cachedValue = this.cache.get(key);
    if (!cachedValue) {
      return undefined;
    }
    return cachedValue.flat() as T[];
  }

  public getRaw<T>(key: string) {
    const cachedValue = this.cache.get(key);
    if (!cachedValue) {
      return undefined;
    }
    return cachedValue as T[][];
  }

  public getRawOrEmpty<T>(key: string): T[][] {
    const cachedValue = this.cache.peek(key, {
      allowStale: true,
    });
    if (!cachedValue) {
      return ClientCacheInstance.EMPTY_ARRAY as T[][];
    }
    return cachedValue as T[][];
  }

  public getReactSafeStore<T>(key: string): T[] {
    const value = this.reactSafeStore.get(key);
    if (!value) {
      return ClientCacheInstance.EMPTY_ARRAY;
    }
    return value as T[];
  }

  public hasReactSafeStore(key: string): boolean {
    return this.reactSafeStore.has(key);
  }

  public set<T>(key: string, value: T[][]): void {
    this.cache.set(key, value);
  }

  public replaceBatch<T>(key: string, value: T[], batchIndex: number): void {
    let cachedValue = this.cache.get(key);
    if (!cachedValue) {
      cachedValue = [];
    }
    if (batchIndex >= cachedValue.length) {
      cachedValue.length = batchIndex + 1;
    }
    cachedValue[batchIndex] = value;
    // info we flat in react so no need to spread
    // new reference to trigger listeners
    // this.cache.set(key, [...cachedValue]);
    this.cache.set(key, cachedValue);
  }

  public handleBatchUpdate<T>(
    key: string,
    value: T[],
    batchIndex: number,
  ): void {
    if (this.isBatchTheSame(key, value, batchIndex)) {
      return;
    }
    this.replaceBatch(key, value, batchIndex);
  }

  public remove(key: string): void {
    this.cache.delete(key);
  }

  public removeArray(keys: Iterable<string>): void {
    for (const key of keys) {
      this.remove(key);
    }
  }

  public has(key: string): boolean {
    return this.cache.has(key);
  }

  public isBatchTheSame<T>(
    key: string,
    value: T[],
    batchIndex: number,
  ): boolean {
    const cachedValue = this.cache.get(key);
    if (!cachedValue || !cachedValue?.[batchIndex]) {
      return false;
    }

    if (value.length !== cachedValue[batchIndex].length) {
      return false;
    }

    return isDeepEqual(cachedValue[batchIndex], value);
  }

  public hasStale(key: string): boolean {
    return !!this.cache.peek(key, {
      allowStale: true,
    });
  }

  public getEntries() {
    return this.cache.entries();
  }

  public bulkInsertIfMissing(data: Record<string, unknown[][]>) {
    // console.log("Bulk inserting into cache:", data);
    Object.entries(data).forEach(([key, value]) => {
      if (!this.hasStale(key)) {
        this.cache.set(key, value);
      }
    });
  }

  public isCacheEmpty(): boolean {
    return this.cache.calculatedSize === 0;
  }

  public purgeStaleCache(): boolean {
    return this.cache.purgeStale();
  }

  private emitChange(key: string) {
    const set = this.listeners.get(key);
    if (set) {
      for (const listener of set) listener();
    }
  }
  public subscribe(key: string, listener: ClientCacheListener): () => void {
    let set = this.listeners.get(key);
    if (!set) {
      set = new Set();
      this.listeners.set(key, set);
    }
    set.add(listener);
    const cachedValue = this.cache.peek(key, {
      allowStale: true,
    });
    if (!this.reactSafeStore.has(key) && cachedValue) {
      // console.log("Cache Subscribing to cache key:", key);
      this.reactSafeStore.set(key, cachedValue.flat());
    }
    return () => {
      // console.log(
      //    "Cache Unsubscribing from cache key:",
      //   key,
      //   this.cache.calculatedSize,
      // );
      if (!set) {
        return;
      }
      set.delete(listener);
      if (set.size === 0) {
        this.listeners.delete(key);
        this.reactSafeStore.delete(key);
        // console.log("Cache Unsubscribing delete key:", key);
      }
    };
  }
}

export const CacheContext = createContext<ClientCacheInstance | null>(null);

interface Props {
  children: ReactNode;
}

const EXPIRE_BEFORE_MS = 1000 * 60 * 30; // 30 minutes
const THROTTLE_WAIT_MS = 1000 * 10; // 10 seconds
export const CacheProvider = ({ children }: Props) => {
  const cacheInstance = useMemo(() => ClientCacheInstance.getInstance(), []);
  const workerRef = useRef<Worker>();

  const requestLoadCacheFromIdb = useMemo(() => {
    return throttle(() => {
      if (!workerRef.current) return;
      const message: LoadCacheIncomingMessage = {
        afterTimestamp: Date.now() - EXPIRE_BEFORE_MS,
        type: IdbMessageType.LOAD_CACHE,
      };
      workerRef.current?.postMessage(message);
    }, THROTTLE_WAIT_MS);
  }, []);

  const dumpHandler = useMemo(() => {
    return throttle(() => {
      if (!workerRef.current) return;
      const entries = cacheInstance.getEntries();
      const message: DumpCacheIncomingMessage = {
        type: IdbMessageType.DUMP_CACHE,
        payload: Array.from(entries),
      };
      if (entries) {
        workerRef.current.postMessage(message);
        // console.log("Dumping cache to IndexedDB:", entries);
        try {
        } catch (err) {
          console.error("Error dumping cache to IndexedDB:", err);
        }
      }
    }, THROTTLE_WAIT_MS);
  }, []);

  useEffect(() => {
    if (navigator.storage?.persist) {
      navigator.storage.persist().catch((err) => {
        console.warn("Could not request persistent storage:", err);
      });
    }

    workerRef.current = new Worker(
      new URL("../lib/indexdb/idb-load-worker.ts", import.meta.url),
    );
    workerRef.current.onmessage = (event: MessageEvent<IdbMessageResponse>) => {
      // console.log("Received message", event.data.type);
      if (
        event.data &&
        event.data.status === "success" &&
        event.data.type === IdbMessageType.LOAD_CACHE &&
        event.data.entries
      ) {
        cacheInstance.bulkInsertIfMissing(event.data.entries);
      }
    };

    let intervalId: NodeJS.Timeout | undefined;

    const idleCbId = requestIdleCallback(() => {
      requestLoadCacheFromIdb();
      intervalId = setInterval(() => {
        cacheInstance.purgeStaleCache();
      });
    });

    return () => {
      workerRef.current?.terminate();
      cancelIdleCallback(idleCbId);
      requestLoadCacheFromIdb.cancel();
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, []);

  const visibilityChangeHandler = useCallback(
    async (e: Event) => {
      if (document.visibilityState === "hidden") {
        dumpHandler();
      } else if (
        document.visibilityState === "visible" &&
        cacheInstance.isCacheEmpty()
      ) {
        // console.log("Cache is empty, loading from IndexedDB");
        requestLoadCacheFromIdb();
      }
    },
    [dumpHandler, requestLoadCacheFromIdb],
  );

  useEvent("beforeunload", dumpHandler);
  useEvent("pagehide", dumpHandler);
  useEvent("visibilitychange", visibilityChangeHandler);

  return (
    <CacheContext.Provider value={cacheInstance}>
      {children}
    </CacheContext.Provider>
  );
};

export const useCacheInstance = (): ClientCacheInstance => {
  const cache = useContext(CacheContext);
  if (!cache)
    throw new Error("useCacheInstance must be used within CacheProvider");
  return cache;
};

export const useCache = <T,>(cacheKey: string) => {
  const cacheInstance = useCacheInstance();

  const getFromCache = useCallback(() => {
    const cachedValue = cacheInstance.get<T>(cacheKey);
    return cachedValue ? [...cachedValue] : undefined;
  }, [cacheInstance, cacheKey]);

  const setInCache = useCallback(
    (data: T[][]) => {
      cacheInstance.set(cacheKey, data);
      return cacheInstance.get<T>(cacheKey);
    },
    [cacheInstance, cacheKey],
  );

  const removeFromCache = useCallback(() => {
    cacheInstance.remove(cacheKey);
    return undefined;
  }, [cacheInstance, cacheKey]);

  const replaceBatchInCache = useCallback(
    (data: T[], batchIndex: number) => {
      cacheInstance.replaceBatch(cacheKey, data, batchIndex);
    },
    [cacheInstance, cacheKey],
  );

  const replaceBatchInCacheWithCallback = useCallback(
    (data: T[], batchIndex: number, callback: (data: T[]) => void) => {
      cacheInstance.replaceBatch(cacheKey, data, batchIndex);
      callback(data);
    },
    [cacheInstance, cacheKey],
  );

  const isCacheKeyNotEmpty = useCallback(
    () => cacheInstance.has(cacheKey),
    [cacheInstance, cacheKey],
  );

  const isSameBatchInCache = useCallback(
    (value: T[], batchIndex: number) =>
      cacheInstance.isBatchTheSame(cacheKey, value, batchIndex),
    [cacheInstance, cacheKey],
  );

  const subscribeToCache = useCallback(
    (listener: ClientCacheListener): (() => void) => {
      return cacheInstance.subscribe(cacheKey, listener);
    },
    [cacheInstance, cacheKey],
  );

  const replaceBatchInForAnyKey = useCallback(
    (data: T[], batchIndex: number, key: string) => {
      cacheInstance.replaceBatch(key, data, batchIndex);
    },
    [cacheInstance],
  );

  const handleBatchUpdate = useCallback(
    (data: T[], batchIndex: number) => {
      cacheInstance.handleBatchUpdate(cacheKey, data, batchIndex);
    },
    [cacheInstance, cacheKey],
  );

  return {
    getFromCache,
    setInCache,
    removeFromCache,
    replaceBatchInCache,
    isCacheKeyNotEmpty,
    isSameBatchInCache,
    replaceBatchInCacheWithCallback,
    replaceBatchInForAnyKey,
    subscribeToCache,
    cacheInstance,
    handleBatchUpdate,
  };
};

export const useCacheInvalidator = () => {
  const cacheInstance = useCacheInstance();

  const removeFromCache = useCallback(
    (key: string) => {
      cacheInstance.remove(key);
    },
    [cacheInstance],
  );

  const removeArrayFromCache = useCallback(
    (keys: Iterable<string>) => {
      cacheInstance.removeArray(keys);
    },
    [cacheInstance],
  );

  const isKeyInCache = useCallback(
    (key: string) => cacheInstance.has(key),
    [cacheInstance],
  );

  return {
    removeFromCache,
    removeArrayFromCache,
    isKeyInCache,
  };
};

//https://github.com/facebook/react/issues/27670
// why no useSyncExternalStoreWithSelector
export function useFlattenCachedValue<T>(cacheKey: string) {
  const {
    cacheInstance,
    subscribeToCache,
    isCacheKeyNotEmpty: _,
    ...rest
  } = useCache<T>(cacheKey);
  const getSnapshot = useCallback(
    () => cacheInstance.getReactSafeStore<T>(cacheKey),
    [cacheKey, cacheInstance],
  );

  const subscription: Subscription<T[]> = useMemo(
    () => ({
      getCurrentValue: getSnapshot,
      subscribe: subscribeToCache,
    }),
    [getSnapshot, subscribeToCache],
  );

  const value = useSubscription<T[]>(subscription);
  const isCacheKeyNotEmpty = useCallback(
    () => cacheInstance.hasReactSafeStore(cacheKey),
    [cacheInstance, cacheKey],
  );

  return {
    value,
    ...rest,
    cacheInstance,
    isCacheKeyNotEmpty,
  };
}
