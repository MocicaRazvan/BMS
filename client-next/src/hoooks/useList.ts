import { SortDirection } from "@/types/fetch-utils";
import {
  ChangeEvent,
  ReactNode,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";
import { usePathname, useRouter } from "@/navigation";
import { useSearchParams } from "next/navigation";
import {
  makeSortFetchParams,
  makeSortString,
  parseSortString,
} from "@/lib/utils";
import useFetchStream, { UseFetchStreamProps } from "@/hoooks/useFetchStream";
import {
  PageableResponse,
  PageInfo,
  ResponseWithUserDtoEntity,
  TitleBodyDto,
} from "@/types/dto";
import { FetchStreamProps } from "@/lib/fetchers/fetchStream";
import { useDebounceWithCallBack } from "@/hoooks/useDebounceWithCallback";
import useDateRangeFilterParams from "@/hoooks/useDateRangeFilterParams";

export interface UseListProps {
  sortingOptions: SortingOption[];
  path: string;
  extraCriteria?: ReactNode;
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
  "path" | "arrayQueryParam" | "queryParams" | "body" | "token"
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
}

type Constrained<T> = T extends TitleBodyDto
  ? T
  : T extends ResponseWithUserDtoEntity<infer U>
    ? U extends TitleBodyDto
      ? T
      : never
    : never;

const DEBOUNCE_DELAY = 500;

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
  ...props
}: UseListArgs<T>) {
  const pathname = usePathname();
  const router = useRouter();
  const currentSearchParams = useSearchParams();
  const filterValue = currentSearchParams.get(filterKey) || "";
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
    // todo verify
    ...props,
  };

  const { messages, error, isFinished, refetch, manualFetcher } =
    useFetchStream<PageableResponse<T>>(fetchArgs);
  const [nextMessages, setNextMessages] = useState<
    PageableResponse<T>[] | null
  >(null);

  const debounceCallback = useCallback(() => {
    setPageInfo((prev) => ({
      ...prev,
      currentPage: 0,
    }));
    setSort([]);
  }, []);

  const debouncedFilter = useDebounceWithCallBack(
    filter,
    DEBOUNCE_DELAY,
    debounceCallback,
  );

  const items = useMemo(
    () => messages?.map((m) => m.content) || [],
    [messages],
  );

  useEffect(() => {
    let isMounted = true;
    const abortController = new AbortController();
    if (
      isFinished &&
      preloadNext &&
      messages &&
      messages.length > 0 &&
      messages[0].pageInfo &&
      !error
    ) {
      const maxPage = messages[0].pageInfo.totalPages;
      if (pageInfo.currentPage === maxPage || !isMounted) {
        return;
      }

      const nextPage = pageInfo.currentPage + 1;
      const newArgs = {
        ...fetchArgs,
        body: {
          ...fetchArgs.body,
          page: nextPage,
        },
      };
      // console.log("newArgs", newArgs);
      manualFetcher({
        fetchProps: newArgs,
        aboveController: abortController,
        localAuthToken: true,
        batchCallback: (data) => {
          if (data.length > 0) {
            setNextMessages((prev) => [...(prev || []), ...data]);
          }
        },
        errorCallback: () => {
          setNextMessages(null);
        },
      }).catch((e) => {
        console.log("manualFetcher Error fetching", e);
      });
    }
    return () => {
      isMounted = false;
      if (abortController && !abortController?.signal?.aborted) {
        abortController?.abort();
        (abortController as any)?.customAbort?.();
      }
    };
  }, [
    error,
    JSON.stringify(fetchArgs),
    manualFetcher,
    messages,
    pageInfo.currentPage,
    pageInfo.pageSize,
    preloadNext,
    isFinished,
  ]);

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
          console.log("Setting defaultEffect sort to createdAt desc");
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
    (e: ChangeEvent<HTMLInputElement>) => {
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
    nextMessages,
    refetchWithResetPage,
  };
}
