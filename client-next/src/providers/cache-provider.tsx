"use client";
import {
  createContext,
  ReactNode,
  useCallback,
  useContext,
  useMemo,
} from "react";
import { isDeepEqual } from "@/lib/utils";
import { LRUCache } from "lru-cache";

class ClientCacheInstance {
  private static instance: ClientCacheInstance;
  private readonly cache: LRUCache<string, unknown[][]>;

  private constructor() {
    this.cache = new LRUCache({
      max: 250,
      maxSize: 10000,
      sizeCalculation: (v, _) => v.flat().length || 1,
      ttl: 1000 * 60 * 5,
      allowStale: false,
      updateAgeOnGet: false,
      updateAgeOnHas: false,
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

    this.cache.set(key, cachedValue);
  }

  public remove(key: string): void {
    this.cache.delete(key);
  }

  public removeArray(keys: Iterable<string>): void {
    for (const key of keys) {
      this.cache.delete(key);
    }
  }

  public has(key: string): boolean {
    return this.cache.has(key);
  }

  public isBatchTheSame<T>(
    key: string,
    value: T[],
    batchIndex: number,
    batchSize: number,
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

export const useCache = <T,>(cacheKey: string) => {
  const clientCache = useContext(CacheContext);
  if (!clientCache) {
    throw new Error(
      "useCache must be used within a CacheProvider. Wrap a parent component in CacheProvider to fix this error.",
    );
  }

  const getFromCache = useCallback(() => {
    const cachedValue = clientCache.get<T>(cacheKey);
    return cachedValue ? [...cachedValue] : undefined;
  }, [cacheKey]);

  const setInCache = useCallback(
    (data: T[][]) => {
      clientCache.set(cacheKey, data);
      return clientCache.get<T>(cacheKey);
    },
    [cacheKey],
  );

  const removeFromCache = useCallback(() => {
    clientCache.remove(cacheKey);
    return undefined;
  }, [cacheKey]);

  const replaceBatchInCache = useCallback(
    (data: T[], batchIndex: number) => {
      clientCache.replaceBatch(cacheKey, data, batchIndex);
    },
    [cacheKey],
  );

  const replaceBatchInCacheWithCallback = useCallback(
    (data: T[], batchIndex: number, callback: (data: T[]) => void) => {
      clientCache.replaceBatch(cacheKey, data, batchIndex);
      callback(data);
    },
    [cacheKey],
  );

  const isCacheKeyNotEmpty = useCallback(() => {
    return clientCache.get<T>(cacheKey) !== undefined;
  }, [cacheKey]);

  const isSameBatchInCache = useCallback(
    (value: T[], batchIndex: number, batchSize: number) =>
      clientCache.isBatchTheSame(cacheKey, value, batchIndex, batchSize),
    [cacheKey],
  );

  const cacheData = useMemo(() => getFromCache(), [getFromCache]);

  const replaceBatchInForAnyKey = useCallback(
    (data: T[], batchIndex: number, key: string) => {
      clientCache.replaceBatch(key, data, batchIndex);
    },
    [],
  );

  return {
    getFromCache,
    setInCache,
    removeFromCache,
    replaceBatchInCache,
    isCacheKeyNotEmpty,
    isSameBatchInCache,
    replaceBatchInCacheWithCallback,
    cacheData,
    replaceBatchInForAnyKey,
  };
};

export const useCacheInvalidator = () => {
  const clientCache = useContext(CacheContext);
  if (!clientCache) {
    throw new Error(
      "useCacheInvalidator must be used within a CacheProvider. Wrap a parent component in CacheProvider to fix this error.",
    );
  }

  const removeFromCache = useCallback((key: string) => {
    clientCache.remove(key);
  }, []);

  const removeArrayFromCache = useCallback((keys: Iterable<string>) => {
    clientCache.removeArray(keys);
  }, []);

  return {
    removeFromCache,
    removeArrayFromCache,
  };
};
