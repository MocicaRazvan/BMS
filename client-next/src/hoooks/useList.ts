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
import useFetchStream from "@/hoooks/useFetchStream";
import {
  PageableResponse,
  PageInfo,
  ResponseWithUserDtoEntity,
  TitleBodyDto,
} from "@/types/dto";
import { FetchStreamProps } from "@/hoooks/fetchStream";
import { useDebounceWithCallBack } from "@/hoooks/useDebounceWithCallback";

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

//
interface Args<T>
  extends Partial<
    Omit<
      FetchStreamProps<T>,
      "path" | "arrayQueryParam" | "queryParams" | "body" | "token"
    >
  > {
  path: string;

  sortingOptions: SortingOption[];
  sizeOptions?: number[];
  extraQueryParams?: Record<string, string>;
  extraArrayQueryParam?: Record<string, string[]>;
  extraUpdateSearchParams?: (searchParams: URLSearchParams) => void;
  filterKey?: FilterKey;
  useAbortController?: boolean;
  navigate?: boolean;
}

type Constrained<T> = T extends TitleBodyDto
  ? T
  : T extends ResponseWithUserDtoEntity<infer U>
    ? U extends TitleBodyDto
      ? T
      : never
    : never;

const DEBOUNCE_DELAY = 500;
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
  ...props
}: Args<T>) {
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

  console.log("currentPage", currentPage);

  const sortString = currentSearchParams.get("sort");
  const sortQ = parseSortString(sortString, sortingOptions);
  const [sort, setSort] = useState(sortQ);
  const [sortValue, setSortValue] = useState(
    sort.length > 0 ? `${sort[0].property}-${sort[0].direction}` : "",
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
  const { messages, error, isFinished, refetch } = useFetchStream<
    PageableResponse<T>
  >({
    path,
    method: "PATCH",
    authToken: true,
    body: {
      page: currentPage,
      size: pageSize,
      sortingCriteria: makeSortFetchParams(sortQ),
    },
    useAbortController,
    queryParams: {
      [filterKey]: navigate ? filterValue : filter[filterKey] || filterValue,
      ...(extraQueryParams && extraQueryParams),
    },
    arrayQueryParam: {
      ...(extraArrayQueryParam && extraArrayQueryParam),
    },
    // todo verify
    ...props,
  });

  const debounceCallback = useCallback(() => {
    setPageInfo((prev) => ({
      ...prev,
      currentPage: 0,
    }));
    setSort([]);
    setSortValue("");
  }, []);

  const debouncedFilter = useDebounceWithCallBack(
    filter,
    DEBOUNCE_DELAY,
    debounceCallback,
  );

  const items = useMemo(
    () => messages?.map((m) => m.content) || [],
    [JSON.stringify(messages)],
  );

  useEffect(() => {
    if (messages && messages.length > 0 && messages[0].pageInfo) {
      setPageInfo((prev) => ({
        ...prev,
        totalPages: messages[0].pageInfo.totalPages,
        totalElements: messages[0].pageInfo.totalElements,
      }));
    }
  }, [JSON.stringify(messages)]);

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

      if (curFilter === "" && Object.keys(curSortParams).length === 0) {
        const createdAt = sortingOptions.find(
          (o) => o.property === "createdAt" && o.direction === "desc",
        );
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
  ]);

  const resetCurrentPage = useCallback(() => {
    setPageInfo((prev) => ({
      ...prev,
      currentPage: 0,
    }));
  }, []);

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
        console.log("here", option);
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
    setSortValue,
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
  };
}
