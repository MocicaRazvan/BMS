"use client";
import {
  createContext,
  ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { isDeepEqual } from "@/lib/utils";
import { LRUCache } from "lru-cache";

const maxLRUCacheItems = process.env.NEXT_PUBLIC_MAX_LRU_CACHE_ITEMS
  ? parseInt(process.env.NEXT_PUBLIC_MAX_LRU_CACHE_ITEMS, 10)
  : 250;
const maxLRUCacheSize = process.env.NEXT_PUBLIC_MAX_LRU_CACHE_SIZE
  ? parseInt(process.env.NEXT_PUBLIC_MAX_LRU_CACHE_SIZE, 10)
  : 10000;
const ttlLRUCache = process.env.NEXT_PUBLIC_TTL_LRU_CACHE
  ? parseInt(process.env.NEXT_PUBLIC_TTL_LRU_CACHE, 10) * 1000
  : 1000 * 60 * 5; //5 mins

type ClientCacheListener = () => void;

export class ClientCacheInstance {
  private static instance: ClientCacheInstance;
  private readonly cache: LRUCache<string, unknown[][]>;

  private readonly listeners: Map<string, Set<ClientCacheListener>>;
  // private flatCache = new Map<string, unknown[]>();
  private readonly EMPTY_ARRAY = [];

  private constructor() {
    this.listeners = new Map();
    // this.flatCache = new Map();
    this.cache = new LRUCache({
      max: maxLRUCacheItems,
      maxSize: maxLRUCacheSize,
      sizeCalculation: (v, _) => v.flat().length || 1,
      ttl: ttlLRUCache,
      allowStale: true,
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
          this.emitChange(k);
        }
      },
      onInsert: (value, key, reason) => {
        // console.log("Cache Inserted:", key, reason);
        // this.flatCache.set(key, value ? value.flat() : this.EMPTY_ARRAY);
        this.emitChange(key);
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
      return this.EMPTY_ARRAY;
    }
    return cachedValue as T[][];
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
    // new reference to trigger listeners
    this.cache.set(key, [...cachedValue]);
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

  public hasStale(key: string): boolean {
    return (
      this.cache.peek(key, {
        allowStale: true,
      }) !== undefined
    );
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
    return () => {
      // console.log(
      //   "Cache Unsubscribing from cache key:",
      //   key,
      //   this.cache.calculatedSize,
      // );
      if (!set) {
        return;
      }
      set.delete(listener);
      if (set.size === 0) {
        this.listeners.delete(key);
      }
    };
  }
}

export const CacheContext = createContext<ClientCacheInstance | null>(null);

interface Props {
  children: ReactNode;
}

export const CacheProvider = ({ children }: Props) => {
  const cacheInstance = useMemo(() => ClientCacheInstance.getInstance(), []);

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
    () => cacheInstance.hasStale(cacheKey),
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

export type CacheIsEqual<T> = (a: T[][], b: T[][]) => boolean;
//https://github.com/facebook/react/issues/27670
// why no useSyncExternalStoreWithSelector
export function useFlattenCachedValue<T>(
  cacheKey: string,
  isEqual: CacheIsEqual<T> = Object.is,
) {
  const { cacheInstance, subscribeToCache, ...rest } = useCache<T>(cacheKey);
  const lastFlatRef = useRef<T[][]>([]);
  const isEqualRef = useRef<CacheIsEqual<T>>(isEqual);
  const getSnapshot = useCallback(
    () => cacheInstance.getRawOrEmpty<T>(cacheKey),
    [cacheKey, cacheInstance],
  );

  const handler = useCallback(() => {
    const next = getSnapshot();
    if (!isEqualRef.current(lastFlatRef.current, next)) {
      lastFlatRef.current = next;
      setValue(next);
    }
  }, [getSnapshot]);

  const [value, setValue] = useState<T[][]>(() => {
    const initial = getSnapshot();
    lastFlatRef.current = initial;
    return initial;
  });

  useEffect(() => {
    const next = getSnapshot();
    if (!isEqualRef.current(lastFlatRef.current, next)) {
      lastFlatRef.current = next;
      setValue(next);
    }

    return subscribeToCache(handler);
  }, [getSnapshot, handler, subscribeToCache]);

  const flat = useMemo(() => value.flat(), [value]);
  return {
    value: flat,
    originalValue: value,
    ...rest,
  };
}
