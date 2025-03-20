"use client";
import { BaseError } from "@/types/responses";
import {
  fetchStreamAsyncIterator,
  FetchStreamBatchedProps,
} from "@/lib/fetchers/fetchStreamBatchedFromCallback";
import TTLCache from "@isaacs/ttlcache";

const globalMemoizedIterators = new TTLCache<string, () => AsyncGenerator<any>>(
  {
    ttl: 1.05 * 1000,
    updateAgeOnGet: true,
  },
);

const NotAsyncGeneratorError = new Error(
  "Async generator is not defined it should never happen",
);

function memoizeAsyncIterator<T>(
  key: string,
  asyncIteratorFn: () => AsyncIterable<T>,
): () => AsyncGenerator<T> {
  if (!globalMemoizedIterators.has(key)) {
    const cache: T[] = [];
    const resolvers: ((value: T | undefined) => void)[] = [];
    const iterator = asyncIteratorFn();
    let aborted = false;

    const sharedIterator = async function* (): AsyncGenerator<T> {
      let index = 0;

      while (index < cache.length) {
        yield cache[index++];
      }

      while (!aborted) {
        if (index < cache.length) {
          yield cache[index++];
        } else {
          const promise = new Promise<T | undefined>((resolve) =>
            resolvers.push(resolve),
          );
          const value = await promise;
          // End of iteration, we send undefined in fetchStreamAsyncIterator in the end
          if (value === undefined) break;
          yield value;
          index++;
        }
      }
    };

    const generatorWithAbort = () => {
      const generator = sharedIterator();
      return Object.assign(generator, {
        abort: () => {
          aborted = true;
          if ("abort" in iterator && typeof iterator.abort === "function") {
            iterator.abort();
          }
          console.log(
            `memoizeAsyncIterator Aborting generator for key: ${key},`,
          );
          // globalMemoizedIterators.delete(key);
          while (resolvers.length > 0) {
            resolvers.shift()?.(undefined);
          }
        },
      });
    };

    (async () => {
      for await (const value of iterator) {
        cache.push(value);
        while (resolvers.length > 0) {
          resolvers.shift()?.(value);
        }
      }
      while (resolvers.length > 0) {
        resolvers.shift()?.(undefined);
      }
    })();
    console.log("memoizeAsyncIterator cache miss", key);
    globalMemoizedIterators.set(key, generatorWithAbort);
  }
  const cachedIterator = globalMemoizedIterators.get(key);
  if (!cachedIterator) throw NotAsyncGeneratorError;
  return cachedIterator;
}

export const deduplicateFetchStream = <T, E extends BaseError = BaseError>(
  params: FetchStreamBatchedProps<T> & {
    dedupKey: string;
  },
) => {
  const asyncIteratorFn = () =>
    fetchStreamAsyncIterator<T, E>({
      ...params,
      onAbort: () => {
        params.onAbort?.();
        // globalMemoizedIterators.delete(params.dedupKey);
      },
      errorCallback: (_) => {
        globalMemoizedIterators.delete(params.dedupKey);
      },
    });

  return memoizeAsyncIterator(params.dedupKey, asyncIteratorFn)();
};

//todo pt mine cand ma voi mai uita, sper sa nu
// Yes, that's correct. In JavaScript, the garbage collector
// ensures that the cache array persists as long as there are references to it through closure scope.
// When the memoizeAsyncIterator function creates a new iterator:
// The cache array is declared in the function's local scope
// The sharedIterator generator function, the IIFE,
// and generatorWithAbort all maintain references to this array via closure
// As long as the iterator returned by generatorWithAbort exists
// in the globalMemoizedIterators TTLCache (or is being used by a consumer),
// the entire closure - including the cache array - is kept in memory
//  Only when all references to the iterator are gone (when the key expires from the
//  TTLCache after 1.05 seconds and no code is actively using the iterator)
//  will the garbage collector be able to clean up the cache array.
//  This closure-based caching pattern is memory-efficient because each unique dedupKey
//  gets its own isolated cache that's automatically cleaned up when the iterator is no longer needed.
