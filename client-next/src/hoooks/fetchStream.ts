"use client";
import ndjsonStream from "can-ndjson-stream";
import { BaseError } from "@/types/responses";
import { AcceptHeader } from "@/types/fetch-utils";
import { NEXT_CSRF_HEADER, NEXT_CSRF_HEADER_TOKEN } from "@/lib/constants";
import { getCsrfToken } from "next-auth/react";

const mutatingActions = ["POST", "PUT", "DELETE", "PATCH"];

export interface FetchStreamProps<T> {
  path: string;
  method?: "GET" | "POST" | "PUT" | "DELETE" | "HEAD" | "PATCH";
  body?: object | null;
  customHeaders?: HeadersInit;
  queryParams?: Record<string, string>;
  arrayQueryParam?: Record<string, string[]>;
  token?: string;
  cache?: RequestCache;
  aboveController?: AbortController;
  successCallback?: (data: T) => void;
  successArrayCallback?: (data: T[]) => void;
  errorCallback?: (error: BaseError) => void;
  acceptHeader?: AcceptHeader;
  batchSize?: number;
  csrf?: string;
}

export async function fetchStream<
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
  successCallback,
  errorCallback,
  successArrayCallback,
  acceptHeader = "application/x-ndjson",
  batchSize = 6,
  csrf,
}: FetchStreamProps<T>) {
  let batchBuffer: T[] = [];
  let messages: T[] = [];
  let error: E | null = null;
  let isFinished = false;

  const abortController = aboveController || new AbortController();

  const headers = new Headers(customHeaders);
  headers.set("Accept", acceptHeader);

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  if (body !== null && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  if (csrf) {
    headers.set(NEXT_CSRF_HEADER_TOKEN, csrf);
  }
  if (mutatingActions.includes(method)) {
    if (csrf) {
      const tokenHashDelimiter = csrf.indexOf("|") !== -1 ? "|" : "%7C";

      const rawToken = csrf.split(tokenHashDelimiter)[0];
      headers.set(NEXT_CSRF_HEADER, rawToken);
    } else {
      const rawToken = await getCsrfToken();
      if (rawToken) {
        headers.set(NEXT_CSRF_HEADER, rawToken);
      } else {
        window.location.reload();
      }
    }
  }

  const querySearch = new URLSearchParams(queryParams).toString();

  const arrayQueryStrings = Object.entries(arrayQueryParam)
    .map(([key, values]) => {
      return values
        .map(
          (value) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`,
        )
        .join("&");
    })
    .join("&");

  const combinedQuery = [querySearch, arrayQueryStrings]
    .filter((part) => part)
    .join("&");

  const fetchOptions: RequestInit = {
    method,
    headers,
    signal: abortController.signal,
    cache,
    credentials: "include",
  };

  if (body !== null && method !== "GET" && method !== "HEAD") {
    fetchOptions.body = JSON.stringify(body);
  }

  const url = combinedQuery ? `${path}?${combinedQuery}` : path;

  const handleBatchUpdate = () => {
    messages.push(...batchBuffer);
    successArrayCallback?.(batchBuffer);
    batchBuffer = [];
  };

  try {
    messages = [];
    batchBuffer = [];
    const res = await fetch(
      `${process.env.NEXT_PUBLIC_SPRING_CLIENT}${url}`,
      fetchOptions,
    );
    const stream = ndjsonStream<T, E>(res.body);
    const reader = stream.getReader();

    const read = async (): Promise<void> => {
      const { done, value } = await reader.read();
      if (done) {
        isFinished = true;
        if (batchBuffer.length > 0) {
          console.log("handleBatchUpdateDone", path, batchBuffer.length);
          handleBatchUpdate();
        }
        return;
      }
      if (!res.ok) {
        error = value as E;
        errorCallback?.(value as E);
      } else {
        batchBuffer.push(value as T);
        // messages = [...messages, value as T];
        if (batchBuffer.length === batchSize) {
          console.log("handleBatchUpdateSize", path, batchBuffer.length);
          handleBatchUpdate();
        }
        successCallback?.(value as T);
      }
      await read();
    };

    await read();
  } catch (err) {
    // check if error is AbortError
    if (err && err instanceof DOMException && err?.name === "AbortError") {
      isFinished = false;
    } else {
      error = err as E;
      isFinished = true;
    }
  }

  const cleanUp = () => {
    if (!abortController?.signal?.aborted) {
      batchBuffer = [];
      abortController?.abort();
    }
  };

  return { messages, error, isFinished, cleanUp };
}
