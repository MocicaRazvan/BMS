"use client";
import ndjsonStream from "can-ndjson-stream";
import { BaseError, isBaseError } from "@/types/responses";
import fetchFactory from "@/lib/fetchers/fetchWithRetry";
import {
  fetchStream,
  FetchStreamProps,
  generateFinalArgs,
} from "@/lib/fetchers/fetchStream";

export type FetchStreamAsyncGenerator<T> = Omit<
  FetchStreamProps<T>,
  "successCallback" | "successArrayCallback"
>;
const EntryError = new Error("Entry is not defined; this should never happen");
type FetchStreamResponse<T, E extends BaseError> = Awaited<
  ReturnType<typeof fetchStream<T, E>>
>;
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

export function fetchStreamAsyncGenerator<
  T = unknown,
  E extends BaseError = BaseError,
>({
  path,
  method = "GET",
  body = null,
  customHeaders = {},
  queryParams = {},
  token = "",
  arrayQueryParam = {},
  cache = "default",
  aboveController,
  errorCallback,
  acceptHeader = "application/x-ndjson",
  batchSize: initialBatchSize = 6,
  csrf,
  updateOnEmpty = false,
  nextRequestConfig,
  onAbort,
  extraOptions,
}: FetchStreamAsyncGenerator<T>): AsyncIterableIterator<
  { data: T[]; batchIndex: number } | { response: FetchStreamResponse<T, E> }
> & { abort: () => void } {
  const queue: (
    | { type: "batch"; data: T[]; batchIndex: number }
    | {
        type: "response";
        response: FetchStreamResponse<T, E>;
      }
  )[] = [];

  let pendingResolve:
    | ((value: IteratorResult<PendingResolveValue<T, E>>) => void)
    | null = null;
  let pendingReject: ((reason?: unknown) => void) | null = null;

  let streamError: E | null = null;
  let isDone = false;
  let localAbortController: AbortController | undefined = undefined;

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
  function callAbortController() {
    if (localAbortController && !localAbortController.signal.aborted) {
      localAbortController.abort();
      (localAbortController as any)?.customAbort?.();
    }
  }

  (async () => {
    const { batchSize, abortController, fetchOptions, url } =
      await generateFinalArgs(
        initialBatchSize,
        aboveController,
        customHeaders,
        acceptHeader,
        token,
        body,
        csrf,
        method,
        queryParams,
        arrayQueryParam,
        cache,
        nextRequestConfig,
        extraOptions,
        path,
      );
    localAbortController = abortController;

    let batchBuffer: T[] = [];
    let batchIndex = 0;
    const messages: T[] = [];
    let isFinished = false;
    let finalResult: FetchStreamResponse<T, E>;
    try {
      const flushBatch = () => {
        // copy the batchBuffer to avoid mutation issues
        const toEmit = batchBuffer.slice();
        queue.push({ type: "batch", data: toEmit, batchIndex });
        if (batchBuffer.length > 0) {
          messages.push(...batchBuffer);
        }
        batchBuffer = [];
        batchIndex++;
        handleResolve();
      };

      const res = await fetchFactory(fetch)(
        `${process.env.NEXT_PUBLIC_SPRING_CLIENT}${url}`,
        fetchOptions,
      );
      const stream = ndjsonStream<T, E>(res.body);
      const reader = stream.getReader();
      while (true) {
        const { done, value } = await reader.read();
        if (done) {
          isFinished = true;
          flushBatch();
          break;
        }
        if (!res.ok) {
          console.error(
            `Error in fetchStreamAsyncGenerator: ${res.status} ${res.statusText}`,
          );
          throw value;
        } else {
          batchBuffer.push(value as T);
          if (batchBuffer.length === batchSize) {
            flushBatch();
          }
        }
      }

      finalResult = {
        messages,
        error: streamError,
        isFinished,
        cleanUp: () => {
          if (localAbortController && !localAbortController?.signal?.aborted) {
            onAbort?.();
            batchBuffer = [];
            batchIndex = 0;
            localAbortController?.abort();
            (localAbortController as any)?.customAbort?.();
          }
        },
      };
    } catch (err) {
      if (err && err instanceof DOMException && err?.name === "AbortError") {
        finalResult = {
          messages,
          error: null,
          isFinished: false,
          cleanUp: () => {},
        };
      } else if (isBaseError(err)) {
        streamError = err as E;
        finalResult = {
          messages,
          error: streamError,
          isFinished: true,
          cleanUp: () => {},
        };
        errorCallback?.(streamError);

        // ts-ignore-next-line
        if (pendingReject) {
          const castedReject = pendingReject as (reason?: unknown) => void;
          castedReject?.(streamError);
          pendingResolve = null;
          pendingReject = null;
        }
      } else {
        finalResult = {
          messages,
          error: null,
          isFinished: true,
          cleanUp: () => {},
        };
      }
    }
    queue.push({ type: "response", response: finalResult });
    isDone = true;
    handleResolve();
  })();

  return {
    [Symbol.asyncIterator]() {
      return this;
    },
    async next() {
      if (streamError) return Promise.reject(streamError);
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
        // console.log("Aborting fetchStreamAsyncIterator for path", path);
        callAbortController();
        onAbort?.();
      } catch (e) {
        console.log("Error aborting fetchStreamAsyncIterator", e);
      }
    },
  };
}
