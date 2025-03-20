"use client";
import { BaseError } from "@/types/responses";

import { fetchStream, FetchStreamProps } from "@/lib/fetchers/fetchStream";
export type FetchStreamBatchedProps<T> = Omit<
  FetchStreamProps<T>,
  "successCallback" | "successArrayCallback"
>;

type FetchStreamResponse<T, E extends BaseError> = Awaited<
  ReturnType<typeof fetchStream<T, E>>
>;

type QueueEntry<T, E extends BaseError> =
  | { type: "batch"; data: T[]; batchIndex: number }
  | {
      type: "response";
      response: FetchStreamResponse<T, E>;
    };

type PendingResolveValue<T, E extends BaseError> =
  | {
      data: T[];
      batchIndex: number;
    }
  | {
      response: Omit<FetchStreamResponse<T, E>, "error"> & {
        error: E | null;
      };
    };

const EntryError = new Error("Entry is not defined it should never happen");

export function fetchStreamAsyncIterator<T, E extends BaseError = BaseError>(
  options: FetchStreamBatchedProps<T>,
): AsyncIterableIterator<
  | { data: T[]; batchIndex: number }
  | { response: Awaited<ReturnType<typeof fetchStream<T, E>>> }
> & { abort: () => void } {
  const queue: QueueEntry<T, E>[] = [];
  let pendingResolve:
    | ((value: IteratorResult<PendingResolveValue<T, E>>) => void)
    | null = null;
  let pendingReject: ((reason?: unknown) => void) | null = null;
  let error: E | null = null;
  let isDone = false;
  const abortController = options.aboveController || new AbortController();

  function handleResolve() {
    if (pendingResolve && queue.length > 0) {
      const entry = queue.shift();
      if (!entry) {
        throw EntryError;
      }
      pendingResolve({
        value:
          entry.type === "batch"
            ? { data: entry.data, batchIndex: entry.batchIndex }
            : { response: entry.response },
        done: false,
      });
      pendingResolve = null;
      pendingReject = null;
    }
  }

  const fetchPromise = fetchStream<T, E>({
    ...options,
    aboveController: abortController,
    successArrayCallback: (data, batchIndex) => {
      queue.push({ type: "batch", data, batchIndex });
      handleResolve();
    },
    errorCallback: (err) => {
      error = err as E;
      options.errorCallback?.(error);
      if (pendingReject) {
        pendingReject(err);
        pendingResolve = null;
        pendingReject = null;
      }
    },
  });

  fetchPromise
    .then((result) => {
      queue.push({ type: "response", response: result });
      isDone = true;
      handleResolve();
    })
    .catch((err) => {
      error = err;
      if (pendingReject) {
        pendingReject(err);
        pendingResolve = null;
        pendingReject = null;
      }
    });

  function callAbortController() {
    if (!abortController.signal.aborted) {
      abortController.abort();
      (abortController as any)?.customAbort?.();
    }
  }

  return {
    [Symbol.asyncIterator]() {
      return this;
    },

    async next() {
      if (error) return Promise.reject(error);
      if (queue.length > 0) {
        const entry = queue.shift();
        if (!entry) {
          throw EntryError;
        }
        return entry.type === "batch"
          ? {
              value: { data: entry.data, batchIndex: entry.batchIndex },
              done: false,
            }
          : { value: { response: entry.response }, done: false };
      }
      if (isDone) {
        return { value: undefined, done: true };
      }
      return new Promise((resolve, reject) => {
        pendingResolve = resolve;
        pendingReject = reject;
      });
    },

    async return() {
      callAbortController();
      return { value: undefined, done: true };
    },

    abort() {
      try {
        console.log("Aborting fetchStreamAsyncIterator for path", options.path);
        callAbortController();
        options.onAbort?.();
      } catch (e) {
        console.log("Error aborting fetchStreamAsyncIterator", e);
      }
    },
  };
}
