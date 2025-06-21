import { SortDirection } from "@/types/fetch-utils";
import {
  ChangeEvent,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { usePathname } from "@/navigation/navigation";
import { useRouter } from "@/navigation/client-navigation";
import { useSearchParams } from "next/navigation";
import {
  makeSortFetchParams,
  makeSortString,
  parseSortString,
  wrapItemToString,
} from "@/lib/utils";
import useFetchStream, {
  UseFetchStreamProps,
} from "@/lib/fetchers/useFetchStream";
import {
  PageableResponse,
  PageInfo,
  ResponseWithUserDtoEntity,
  TitleBodyDto,
} from "@/types/dto";
import { FetchStreamProps } from "@/lib/fetchers/fetchStream";
import { useDebounceWithCallBack } from "@/hoooks/useDebounceWithCallback";
import useDateRangeFilterParams from "@/hoooks/useDateRangeFilterParams";
import { useDeepCompareMemo } from "@/hoooks/use-deep-memo";
import { BaseError } from "@/types/responses";
import usePrefetcher, {
  PrefetchedPredicate,
  PrefetchGenerateKeyValue,
  PrefetchGenerateMarkPrefetchedArgs,
  PrefetchGenerateNewArgs,
  UseFetchStreamPrefetcherReturn,
} from "@/lib/fetchers/use-prefetcher";
import { v3 as murmurV3 } from "murmurhash";

export interface UseListProps {
  sortingOptions: SortingOption[];
  path: string;
  extraQueryParams?: Record<string, string>;
  extraArrayQueryParam?: Record<string, string[]>;
  extraUpdateSearchParams?: (searchParams: URLSearchParams) => void;
  sizeOptions?: number[];
}

export interface SortingOption {
  property: string;
  text: string;
  direction: SortDirection;
}

type FilterKey =
  | "title"
  | "email"
  | "filterReceiver"
  | "name"
  | "city"
  | "state"
  | "country";
type Filter = { [key in FilterKey]?: string };

export type PartialFetchStreamProps<T> = Omit<
  FetchStreamProps<T>,
  | "path"
  | "arrayQueryParam"
  | "queryParams"
  | "body"
  | "token"
  | "aboveController"
>;

//
export interface UseListArgs<T> extends PartialFetchStreamProps<T> {
  path: string;
  sortingOptions: SortingOption[];
  sizeOptions?: number[];
  extraQueryParams?: Record<string, string>;
  extraArrayQueryParam?: Record<string, string[]>;
  extraUpdateSearchParams?: (searchParams: URLSearchParams) => void;
  filterKey?: FilterKey;
  useAbortController?: boolean;
  navigate?: boolean;
  defaultSort?: boolean;
  preloadNext?: boolean;
  debounceDelay?: number;
}

type Constrained<T> = T extends TitleBodyDto
  ? T
  : T extends ResponseWithUserDtoEntity<infer U>
    ? U extends TitleBodyDto
      ? T
      : never
    : never;

const DEFAULT_DEBOUNCE_DELAY = 300;

function getCreatedAtOption(sortingOptions: SortingOption[]) {
  return sortingOptions.find(
    (o) => o.property === "createdAt" && o.direction === "desc",
  );
}

export default function useList<T>({
  path,
  sortingOptions,
  sizeOptions,
  extraUpdateSearchParams,
  extraQueryParams,
  extraArrayQueryParam,
  filterKey = "title",
  useAbortController = true,
  navigate = true,
  defaultSort = true,
  preloadNext = true,
  debounceDelay = DEFAULT_DEBOUNCE_DELAY,
  ...props
}: UseListArgs<T>) {
  const pathname = usePathname();
  const router = useRouter();
  const currentSearchParams = useSearchParams();
  const filterValue = currentSearchParams.get(filterKey) || "";
  const initialFilterValue = useRef(filterValue);
  const currentPage = parseInt(
    currentSearchParams.get("currentPage") || "0",
    10,
  );
  const pageSize = parseInt(
    currentSearchParams.get("pageSize") || sizeOptions?.[0].toString() || "6",
    10,
  );
  const defaultCreatedAtLowerBound = currentSearchParams.get(
    "createdAtLowerBound",
  );
  const defaultCreatedAtUpperBound = currentSearchParams.get(
    "createdAtUpperBound",
  );
  const defaultUpdatedAtLowerBound = currentSearchParams.get(
    "updatedAtLowerBound",
  );
  const defaultUpdatedAtUpperBound = currentSearchParams.get(
    "updatedAtUpperBound",
  );

  const {
    updateRange: updateCreatedAtRange,
    queryParams: createdAtRangeParams,
    updateSearchParams: updateCreatedAtSearchParams,
  } = useDateRangeFilterParams(
    "createdAtLowerBound",
    "createdAtUpperBound",
    defaultCreatedAtLowerBound ?? undefined,
    defaultCreatedAtUpperBound ?? undefined,
  );

  const {
    updateRange: updateUpdatedAtRange,
    queryParams: updatedAtRangeParams,
    updateSearchParams: updateUpdatedAtSearchParams,
  } = useDateRangeFilterParams(
    "updatedAtLowerBound",
    "updatedAtUpperBound",
    defaultUpdatedAtLowerBound ?? undefined,
    defaultUpdatedAtUpperBound ?? undefined,
  );

  const [pageInfo, setPageInfo] = useState<PageInfo>({
    currentPage,
    totalPages: currentPage + 1,
    totalElements: (currentPage + 1) * pageSize,
    pageSize,
  });

  const [filter, setFilter] = useState<Filter>({
    [filterKey]: filterValue,
  });

  const sortString = currentSearchParams.get("sort");

  const [sort, setSort] = useState(() => {
    const parsed = parseSortString(sortString, sortingOptions);
    const curFilter = filter[filterKey] || "";
    if (defaultSort && curFilter === "" && parsed.length === 0) {
      const createdAt = getCreatedAtOption(sortingOptions);
      return createdAt ? [createdAt] : [];
    }
    return parsed;
  });

  const sortValue = useMemo(
    () =>
      sort.length > 0
        ? sort.map((s) => `${s.property}-${s.direction}`).join(",")
        : "",
    [sort],
  );

  const fetchArgs: UseFetchStreamProps = {
    path,
    method: "PATCH",
    authToken: true,
    body: {
      page: currentPage,
      size: pageSize,
      sortingCriteria: makeSortFetchParams(sort),
    },
    useAbortController,
    queryParams: {
      [filterKey]: navigate ? filterValue : filter[filterKey] || filterValue,
      ...createdAtRangeParams,
      ...updatedAtRangeParams,
      ...(extraQueryParams && extraQueryParams),
    },
    arrayQueryParam: {
      ...(extraArrayQueryParam && extraArrayQueryParam),
    },
    ...props,
  };

  const stableFetchArgs = useDeepCompareMemo(() => fetchArgs, [fetchArgs]);

  const {
    messages,
    error,
    isFinished,
    refetch,
    manualFetcher,
    isAbsoluteFinished,
    isRefetchClosure,
  } = useFetchStream<PageableResponse<T>>(stableFetchArgs);

  usePageInfoPrefetcher({
    stableFetchArgs,
    pageInfo,
    preloadNext,
    returned: {
      isRefetchClosure,
      messages,
      error,
      manualFetcher,
      isAbsoluteFinished,
    },
  });

  const debounceCallback = useCallback(() => {
    setPageInfo((prev) => ({
      ...prev,
      currentPage: 0,
    }));
    // setSort([]);
  }, []);

  const debouncedFilter = useDebounceWithCallBack(
    filter,
    debounceDelay,
    debounceCallback,
  );

  const items = useMemo(
    () => messages?.map((m) => m.content) || [],
    [messages],
  );

  useEffect(() => {
    if (messages && messages.length > 0 && messages[0].pageInfo) {
      const { totalPages, totalElements } = messages[0].pageInfo;

      setPageInfo((prev) => {
        if (
          prev.totalPages === totalPages &&
          prev.totalElements === totalElements
        ) {
          return prev;
        }
        return {
          ...prev,
          totalPages: messages[0].pageInfo.totalPages,
          totalElements: messages[0].pageInfo.totalElements,
        };
      });
    } else {
      setPageInfo((prev) => {
        if (prev.totalPages === 0 && prev.totalElements === 0) {
          return prev;
        }
        return {
          ...prev,
          totalPages: 0,
          totalElements: 0,
        };
      });
    }
  }, [messages]);

  useEffect(() => {
    if (navigate) {
      const updatedSearchParams = new URLSearchParams(
        currentSearchParams.toString(),
      );
      const curFilter = debouncedFilter[filterKey] || "";
      updatedSearchParams.set(filterKey, curFilter);
      updatedSearchParams.set("currentPage", pageInfo.currentPage.toString());
      updatedSearchParams.set("pageSize", pageInfo.pageSize.toString());

      const curSortParams = makeSortFetchParams(sort);

      if (
        defaultSort &&
        curFilter === "" &&
        Object.keys(curSortParams).length === 0
      ) {
        const createdAt = getCreatedAtOption(sortingOptions);
        if (createdAt) {
          setSort([createdAt]);
          updatedSearchParams.set(
            "sort",
            makeSortString(makeSortFetchParams([createdAt])),
          );
        } else {
          updatedSearchParams.delete("sort");
        }
      } else if (Object.keys(curSortParams).length === 0) {
        updatedSearchParams.delete("sort");
      } else {
        updatedSearchParams.set(
          "sort",
          makeSortString(makeSortFetchParams(sort)),
        );
      }
      updateCreatedAtSearchParams(updatedSearchParams);
      updateUpdatedAtSearchParams(updatedSearchParams);

      if (extraUpdateSearchParams) {
        extraUpdateSearchParams(updatedSearchParams);
      }
      const newSearchString = updatedSearchParams.toString();
      if (newSearchString !== currentSearchParams.toString()) {
        router.replace(`${pathname}?${newSearchString}`, { scroll: true });
      }
    }
  }, [
    debouncedFilter,
    pageInfo.currentPage,
    pageInfo.pageSize,
    router,
    pathname,
    currentSearchParams,
    sort,
    extraUpdateSearchParams,
    filterKey,
    navigate,
    sortingOptions,
    updateCreatedAtSearchParams,
    updateUpdatedAtSearchParams,
    defaultSort,
    updateCreatedAtRange,
    updateUpdatedAtRange,
  ]);

  const resetCurrentPage = useCallback(() => {
    setPageInfo((prev) => ({
      ...prev,
      currentPage: 0,
    }));
  }, []);

  const refetchWithResetPage = useCallback(() => {
    resetCurrentPage();
    refetch();
  }, [refetch, resetCurrentPage]);

  const updateSortState = useCallback((option: SortingOption) => {
    setSort((prev) => {
      const exists = prev.find(
        (p) =>
          p.property === option.property && p.direction === option.direction,
      );
      if (exists) {
        return prev.filter(
          (p) =>
            !(
              p.property === option.property && p.direction === option.direction
            ),
        );
      } else {
        return [...prev.filter((o) => o.property !== option.property), option];
      }
    });
  }, []);

  const updateFilterValue = useCallback(
    (e: ChangeEvent<HTMLInputElement> | string) => {
      if (typeof e === "string") {
        setFilter({ [filterKey]: e });
        return;
      }
      setFilter({ [filterKey]: e.target.value });
    },
    [filterKey],
  );

  const clearFilterValue = useCallback(() => {
    setFilter({ [filterKey]: "" });
  }, [filterKey]);

  const updateFilterValueFromString = useCallback(
    (value: string) => {
      setFilter({ [filterKey]: value });
    },
    [filterKey],
  );

  return {
    items,
    pageInfo,
    setPageInfo,
    filter,
    setFilter,
    sort,
    setSort,
    updateSortState,
    sortValue,
    isFinished,
    error,
    messages,
    debouncedFilter,
    refetch,
    updateFilterValue,
    clearFilterValue,
    resetCurrentPage,
    updateFilterValueFromString,
    filterValue,
    updateCreatedAtRange,
    updateUpdatedAtRange,
    updatedAtRangeParams,
    createdAtRangeParams,
    refetchWithResetPage,
    initialFilterValue: initialFilterValue.current,
  };
}

export function usePageInfoPrefetcher<T, E extends BaseError>({
  returned,
  pageInfo,
  preloadNext = true,
  stableFetchArgs,
}: {
  returned: UseFetchStreamPrefetcherReturn<PageableResponse<T>, E>;
  pageInfo: PageInfo;
  preloadNext?: boolean;
  stableFetchArgs: UseFetchStreamProps;
}) {
  const additionalKey = useDeepCompareMemo(
    () =>
      wrapItemToString(
        murmurV3(
          JSON.stringify({
            ...stableFetchArgs.queryParams,
            ...stableFetchArgs.arrayQueryParam,
          }),
        ),
      ),
    [stableFetchArgs.queryParams, stableFetchArgs.arrayQueryParam],
  );

  const generateKeyValue: PrefetchGenerateKeyValue<PageableResponse<T>> =
    useCallback(
      (messages) => [
        pageInfo.pageSize.toString(),
        pageInfo.currentPage.toString(),
      ],
      [pageInfo.pageSize, pageInfo.currentPage],
    );
  const nextPredicate: PrefetchedPredicate<PageableResponse<T>> = useCallback(
    (messages, hasPrefetched) => {
      if (
        !(
          messages.length > 0 &&
          messages[0].pageInfo.totalPages > 1 &&
          messages[0].pageInfo.pageSize === pageInfo.pageSize
        )
      ) {
        return false;
      }
      const maxPage = messages[0].pageInfo.totalPages - 1;
      if (pageInfo.currentPage === maxPage) {
        return false;
      }
      const nextPage = pageInfo.currentPage + 1;
      return !hasPrefetched([
        pageInfo.pageSize.toString(),
        nextPage.toString(),
      ]);
    },
    [pageInfo.currentPage, pageInfo.pageSize],
  );
  const generateNextArgs: PrefetchGenerateNewArgs<PageableResponse<T>> =
    useCallback(
      (messages) => {
        const nextPage = pageInfo.currentPage + 1;
        return {
          ...stableFetchArgs,
          body: {
            ...stableFetchArgs.body,
            page: nextPage,
          },
        };
      },
      [pageInfo.currentPage, stableFetchArgs],
    );
  const generateMarkPrefetchedNextArgs: PrefetchGenerateMarkPrefetchedArgs<
    PageableResponse<T>
  > = useCallback(
    (messages, newArgs) => [
      pageInfo.pageSize.toString(),
      (pageInfo.currentPage + 1).toString(),
    ],
    [pageInfo.pageSize, pageInfo.currentPage],
  );
  const previousPredicate: PrefetchedPredicate<PageableResponse<T>> =
    useCallback(
      (messages, hasPrefetched) => {
        if (
          !(
            messages.length > 0 &&
            messages[0].pageInfo.pageSize === pageInfo.pageSize &&
            pageInfo.currentPage > 0
          )
        ) {
          return false;
        }
        const prevPage = pageInfo.currentPage - 1;
        return !hasPrefetched([
          pageInfo.pageSize.toString(),
          prevPage.toString(),
        ]);
      },
      [pageInfo.currentPage, pageInfo.pageSize],
    );

  const generatePreviousArgs: PrefetchGenerateNewArgs<PageableResponse<T>> =
    useCallback(
      (messages) => {
        const prevPage = pageInfo.currentPage - 1;
        return {
          ...stableFetchArgs,
          body: {
            ...stableFetchArgs.body,
            page: prevPage,
          },
        };
      },
      [pageInfo.currentPage, stableFetchArgs],
    );

  const generateMarkPrefetchedPreviousArgs: PrefetchGenerateMarkPrefetchedArgs<
    PageableResponse<T>
  > = useCallback(
    (messages, newArgs) => [
      pageInfo.pageSize.toString(),
      (pageInfo.currentPage - 1).toString(),
    ],
    [pageInfo.pageSize, pageInfo.currentPage],
  );
  return usePrefetcher<PageableResponse<T>, E>({
    generateKeyValue,
    nextPredicate,
    generateNextArgs,
    generateMarkPrefetchedNextArgs,
    previousPredicate,
    generatePreviousArgs,
    generateMarkPrefetchedPreviousArgs,
    preloadNext,
    returned,
    additionalKey,
  });
}
