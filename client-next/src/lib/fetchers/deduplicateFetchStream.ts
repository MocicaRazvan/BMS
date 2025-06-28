"use client";
import { BaseError } from "@/types/responses";
import { FetchStreamBatchedProps } from "@/lib/fetchers/fetchStreamBatchedFromCallback";
import TTLCache from "@isaacs/ttlcache";
import { fetchStreamAsyncGenerator } from "@/lib/fetchers/fetchStreamAsyncGenerator";

const DEDUPLICATION_FACTOR = 1.05 as const;
type MemoEntry<T> =
  | {
      type: "pending";
      promise: Promise<() => AsyncGenerator<T>>;
      abort: () => void;
    }
  | {
      type: "ready";
      value: () => AsyncGenerator<T>;
      abort: () => void;
    };

const globalMemoizedIterators = new TTLCache<string, MemoEntry<any>>({
  ttl: DEDUPLICATION_FACTOR * 1000,
  updateAgeOnGet: true,
});

export const getSyncAbortForKey = (key: string) => {
  const existing = globalMemoizedIterators.get(key);
  if (!existing) {
    return () => {};
  }
  return existing.abort;
};

async function memoizeAsyncIterator<T>(
  key: string,
  asyncIteratorFn: () => AsyncIterable<T>,
): Promise<() => AsyncGenerator<T>> {
  const existing = globalMemoizedIterators.get(key);

  if (existing) {
    if (existing.type === "ready") {
      // console.log("memoizeAsyncIterator cache hit function", key);
      return existing.value;
    } else {
      // console.log("memoizeAsyncIterator cache hit promise", key);
      return await existing.promise;
    }
  }

  const cache: T[] = [];
  const resolvers: ((value: T | undefined) => void)[] = [];
  const iterator = asyncIteratorFn();
  let aborted = false;

  //dummy initialization to avoid TS error
  let resolveFactory: (genFn: () => AsyncGenerator<T>) => void = (_) => {};
  let rejectFactory: (err: any) => void = (_) => {};
  const factoryPromise = new Promise<() => AsyncGenerator<T>>(
    (resolve, reject) => {
      resolveFactory = resolve;
      rejectFactory = reject;
    },
  );

  const abortEarly = () => {
    if (aborted) return;
    aborted = true;

    globalMemoizedIterators.delete(key);

    while (resolvers.length > 0) {
      resolvers.shift()?.(undefined);
    }

    resolveFactory(() => {
      const noopGen = (async function* () {
        //
      })();
      return Object.assign(noopGen, {
        abort: () => {},
      });
    });
  };

  globalMemoizedIterators.set(key, {
    type: "pending",
    promise: factoryPromise,
    abort: abortEarly,
  });

  (async () => {
    try {
      const sharedIterator = async function* (): AsyncGenerator<T> {
        let index = 0;

        while (index < cache.length) {
          if (aborted) return;
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
            if (aborted) return;
            // End of iteration, we send undefined in fetchStreamAsyncGenerator in the end
            if (value === undefined) break;
            yield value;
            index++;
          }
        }
      };
      const finalAbort = () => {
        if (aborted) return;
        aborted = true;

        globalMemoizedIterators.delete(key);

        if ("abort" in iterator && typeof iterator.abort === "function") {
          iterator.abort();
        }
        // console.log(
        //   `memoizeAsyncIterator Aborting generator for key: ${key},`,
        // );

        while (resolvers.length > 0) {
          resolvers.shift()?.(undefined);
        }
      };

      const generatorWithAbort = () => {
        const generator = sharedIterator();
        return Object.assign(generator, {
          abort: finalAbort,
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

      if (!aborted) {
        globalMemoizedIterators.set(key, {
          type: "ready",
          value: generatorWithAbort,
          abort: finalAbort,
        });

        resolveFactory(generatorWithAbort);
      }
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
