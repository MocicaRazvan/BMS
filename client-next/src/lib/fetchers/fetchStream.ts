"use client";
import ndjsonStream from "can-ndjson-stream";
import { BaseError } from "@/types/responses";
import { AcceptHeader } from "@/types/fetch-utils";
import { NEXT_CSRF_HEADER, NEXT_CSRF_HEADER_TOKEN } from "@/types/constants";
import { getCsrfToken } from "next-auth/react";
import fetchFactory from "@/lib/fetchers/fetchWithRetry";

export const mutatingActions = ["POST", "PUT", "DELETE", "PATCH"];

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
  successArrayCallback?: (data: T[], batchIndex: number) => void;
  errorCallback?: (error: BaseError) => void;
  acceptHeader?: AcceptHeader;
  batchSize?: number;
  csrf?: string;
  updateOnEmpty?: boolean;
  nextRequestConfig?: NextFetchRequestConfig;
  onAbort?: () => void;
  extraOptions?: RequestInit;
}

export async function generateFinalArgs(
  initialBatchSize: number,
  aboveController: AbortController | undefined,
  customHeaders: HeadersInit,
  acceptHeader: AcceptHeader,
  token: string,
  body: object | null,
  csrf: string | undefined,
  method: "POST" | "PUT" | "DELETE" | "PATCH" | "GET" | "HEAD",
  queryParams: Record<string, string>,
  arrayQueryParam: Record<string, string[]>,
  cache: RequestCache,
  nextRequestConfig: NextFetchRequestConfig | undefined,
  extraOptions: RequestInit | undefined,
  path: string,
) {
  const batchSize = initialBatchSize > 0 ? initialBatchSize : 6;

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
    ...(nextRequestConfig && { next: nextRequestConfig }),
    ...(extraOptions || {}),
  };

  if (body !== null && method !== "GET" && method !== "HEAD") {
    fetchOptions.body = JSON.stringify(body);
  }

  const url = combinedQuery ? `${path}?${combinedQuery}` : path;
  return { batchSize, abortController, fetchOptions, url };
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
  batchSize: initialBatchSize = 6,
  csrf,
  updateOnEmpty = false,
  nextRequestConfig,
  onAbort,
  extraOptions,
}: FetchStreamProps<T>) {
  let batchBuffer: T[] = [];
  let batchIndex = 0;
  let messages: T[] = [];
  let error: E | null = null;
  let isFinished = false;

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

  const handleBatchUpdate = () => {
    // console.log(
    //   "handleBatchUpdate inner",
    //   path,
    //   batchBuffer.length,
    //   batchBuffer,
    //   acceptHeader,
    // );
    messages.push(...batchBuffer);
    successArrayCallback?.(batchBuffer, batchIndex);
    batchBuffer = [];
  };

  try {
    messages = [];
    batchBuffer = [];
    const res = await fetchFactory(fetch)(
      `${process.env.NEXT_PUBLIC_SPRING_CLIENT}${url}`,
      fetchOptions,
    );
    const stream = ndjsonStream<T, E>(res.body);
    const reader = stream.getReader();

    const read = async (): Promise<void> => {
      const { done, value } = await reader.read();
      if (done) {
        isFinished = true;
        if (!updateOnEmpty && batchBuffer.length > 0) {
          // console.log("handleBatchUpdateDone", path, batchBuffer.length);
          handleBatchUpdate();
        } else {
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
          // console.log("handleBatchUpdateSize", path, batchBuffer.length);
          handleBatchUpdate();
          batchIndex++;
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
      onAbort?.();
      batchBuffer = [];
      batchIndex = 0;

      abortController?.abort();
    }
  };

  return { messages, error, isFinished, cleanUp };
}
