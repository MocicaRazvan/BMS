"use client";
import { BaseError } from "@/types/responses";
import { FetchStreamBatchedProps } from "@/lib/fetchers/fetchStreamBatchedFromCallback";
import TTLCache from "@isaacs/ttlcache";
import { fetchStreamAsyncGenerator } from "@/lib/fetchers/fetchStreamAsyncGenerator";

const DEDUPLICATION_FACTOR = 1.05 as const;

const globalMemoizedIterators = new TTLCache<
  string,
  (() => AsyncGenerator<any>) | Promise<() => AsyncGenerator<any>>
>({
  ttl: DEDUPLICATION_FACTOR * 1000,
  updateAgeOnGet: true,
});

async function memoizeAsyncIterator<T>(
  key: string,
  asyncIteratorFn: () => AsyncIterable<T>,
): Promise<() => AsyncGenerator<T>> {
  const existingIterator = globalMemoizedIterators.get(key);
  if (existingIterator) {
    if (typeof existingIterator === "function") {
      // console.log(`memoizeAsyncIterator cache hit function for key: ${key}`);
      return existingIterator;
    } else {
      // console.log(
      //   `memoizeAsyncIterator cache hit promise for key: ${key}, waiting for it to resolve`,
      // );
      return await existingIterator;
    }
  }

  //dummy initialization to avoid TS error
  let resolveFactory: (genFn: () => AsyncGenerator<T>) => void = (_) => {};
  let rejectFactory: (err: any) => void = (_) => {};
  const factoryPromise = new Promise<() => AsyncGenerator<T>>(
    (resolve, reject) => {
      resolveFactory = resolve;
      rejectFactory = reject;
    },
  );
  globalMemoizedIterators.set(key, factoryPromise);

  (async () => {
    try {
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
            // End of iteration, we send undefined in fetchStreamAsyncGenerator in the end
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
            // console.log(
            //   `memoizeAsyncIterator Aborting generator for key: ${key},`,
            // );
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

      // ready state
      globalMemoizedIterators.set(key, generatorWithAbort);

      resolveFactory(generatorWithAbort);
    } catch (err) {
      globalMemoizedIterators.delete(key);
      rejectFactory(err);
    }
  })();

  // console.log("memoizeAsyncIterator cache miss", key);
  return factoryPromise;
}

export const deduplicateFetchStream = async <
  T,
  E extends BaseError = BaseError,
>(
  params: FetchStreamBatchedProps<T> & {
    dedupKey: string;
  },
) => {
  const asyncIteratorFn = () =>
    fetchStreamAsyncGenerator<T, E>({
      ...params,
      onAbort: () => {
        params.onAbort?.();
        globalMemoizedIterators.delete(params.dedupKey);
      },
      errorCallback: (err) => {
        params.errorCallback?.(err);
        globalMemoizedIterators.delete(params.dedupKey);
      },
    });

  return (await memoizeAsyncIterator(params.dedupKey, asyncIteratorFn))();
};
