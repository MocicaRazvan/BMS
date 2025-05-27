"use client";

import { LRUCache } from "lru-cache";
import murmur from "murmurhash";
import { useEffect, useState } from "react";
import stringify from "safe-stable-stringify";

class ClientLRUStore {
  private static instance: ClientLRUStore;
  private readonly cache: LRUCache<number, any>;

  private constructor() {
    this.cache = new LRUCache({
      max: 1000,
      ttl: 1000 * 60 * 60,
      allowStale: true,
      updateAgeOnGet: true,
      updateAgeOnHas: false,
    });
  }

  static getInstance(): ClientLRUStore {
    if (!ClientLRUStore.instance) {
      ClientLRUStore.instance = new ClientLRUStore();
    }
    return ClientLRUStore.instance;
  }

  public get<T>(...args: any[]) {
    const cacheKey = this.generateCacheKey(...args);
    return this.cache.get(cacheKey) as T;
  }

  public set<T>(value: T, ...args: any[]) {
    const cacheKey = this.generateCacheKey(...args);
    this.cache.set(cacheKey, value);
  }

  private generateCacheKey(...args: any[]) {
    return murmur.v3(args.join(""));
  }

  public remove(...args: any[]) {
    this.cache.delete(this.generateCacheKey(...args));
  }
}

declare const globalThis: {
  clientLRUStore: ClientLRUStore | undefined;
} & typeof global;

export function getClientLRUStore(): ClientLRUStore {
  if (!globalThis.clientLRUStore) {
    globalThis.clientLRUStore = ClientLRUStore.getInstance();
  }
  if (!globalThis.clientLRUStore) {
    throw new Error("ClientLRUStore is not initialized");
  }
  return globalThis.clientLRUStore;
}
interface UseClientLRUStoreArgs<T> {
  setter: (args: any[]) => Promise<T>;
  args: any[];
}
const clientLRUStore = getClientLRUStore();
export const useClientLRUStore = <T>({
  setter,
  args,
}: UseClientLRUStoreArgs<T>) => {
  const stableArgs = stringify(args) || "__undefined";

  const [state, setState] = useState<T | null>(null);
  useEffect(() => {
    const cachedValue = clientLRUStore.get<T>(...args);
    if (cachedValue) {
      setState(cachedValue);
    } else {
      setter(args)
        .then((value) => {
          clientLRUStore.set(value, ...args);
          setState(value);
        })
        .catch((error) => {
          console.error("Error fetching data:", error);
          setState(null);
        });
    }
  }, [stableArgs]);

  return state;
};
