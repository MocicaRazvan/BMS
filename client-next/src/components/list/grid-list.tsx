"use client";

import { ResponseWithUserDtoEntity, TitleBodyImagesUserDto } from "@/types/dto";
import { ReactNode, useMemo } from "react";
import { cn } from "@/lib/utils";

import { SortDirection } from "@/types/fetch-utils";

import Loader from "@/components/ui/spinner";
import ItemCard, { ItemCardTexts } from "@/components/list/item-card";
import {
  DataTablePagination,
  DataTablePaginationTexts,
} from "@/components/table/data-table-pagination";

import SearchInput from "@/components/forms/input-serach";
import useList, {
  PartialFetchStreamProps,
  UseListProps,
} from "@/hoooks/useList";
import RadioSort, { RadioSortTexts } from "@/components/common/radio-sort";

import { motion } from "framer-motion";
import LoadingItemCard from "@/components/list/loading-item-card";
import CreationFilter, {
  CreationFilterTexts,
} from "@/components/list/creation-filter";
import { ClassValue } from "clsx";
import dynamic from "next/dynamic";
import { Skeleton } from "@/components/ui/skeleton";

export interface SortingOption {
  property: string;
  text: string;
  direction: SortDirection;
}

export interface GridListTexts {
  itemCardTexts: ItemCardTexts;
  dataTablePaginationTexts: DataTablePaginationTexts;
  noResults: string;
  gettingMore: string;
  search: string;
  radioSortTexts: RadioSortTexts;
  creationFilterTexts: CreationFilterTexts;
}

interface GridListProps<T extends TitleBodyImagesUserDto>
  extends GridListTexts,
    UseListProps,
    Omit<
      PartialFetchStreamProps<T>,
      "onAbort" | "successCallback" | "successArrayCallback"
    > {
  onItemClick?: (item: ResponseWithUserDtoEntity<T>) => void;
  passExtraContent?: (item: ResponseWithUserDtoEntity<T>) => ReactNode;
  passExtraHeader?: (item: ResponseWithUserDtoEntity<T>) => ReactNode;
  passExtraImageOverlay?: (item: ResponseWithUserDtoEntity<T>) => ReactNode;
  extraCriteriaWithCallBack?: (callback: () => void) => ReactNode;
  extraCriteriaClassname?: ClassValue;
  forbiddenSortingOptions?: string[];
  itemLinkCallback?: (item: ResponseWithUserDtoEntity<T>) => string;
}

const DynamicNoResultsLottie = dynamic(
  () => import("@/components/lottie/no-results-lottie"),
  {
    ssr: false,
    loading: () => (
      <Skeleton className="w-full h-full md:w-1/3 md:h-1/3 mx-auto" />
    ),
  },
);

export default function GridList<T extends TitleBodyImagesUserDto>({
  onItemClick,
  sizeOptions = [6, 12, 24],
  sortingOptions,
  path,
  passExtraContent,
  passExtraHeader,
  extraCriteria,
  extraQueryParams = {},
  extraArrayQueryParam = {},
  extraUpdateSearchParams,
  itemCardTexts,
  gettingMore,
  noResults,
  search,
  dataTablePaginationTexts,
  radioSortTexts,
  extraCriteriaWithCallBack,
  passExtraImageOverlay,
  creationFilterTexts,
  extraCriteriaClassname,
  forbiddenSortingOptions = ["userDislikesLength"],
  itemLinkCallback,
  ...rest
}: GridListProps<T>) {
  const finalSortingOptions = useMemo(
    () =>
      sortingOptions.filter(
        ({ property }) => !forbiddenSortingOptions.includes(property),
      ),
    [JSON.stringify(sortingOptions), JSON.stringify(forbiddenSortingOptions)],
  );
  const {
    pageInfo,
    filter,
    sort,
    setSort,
    sortValue,
    items,
    isFinished,
    error,
    updateFilterValue,
    clearFilterValue,
    setPageInfo,
    resetCurrentPage,
    updateCreatedAtRange,
    updateUpdatedAtRange,
    nextMessages,
  } = useList<ResponseWithUserDtoEntity<T>>({
    path,
    extraQueryParams,
    extraArrayQueryParam,
    extraUpdateSearchParams,
    sizeOptions,
    sortingOptions: finalSortingOptions,
    ...rest,
  });

  return (
    <div className="w-full ">
      <div className="w-full h-full space-y-5">
        <div className="my-10 w-full flex items-start justify-start flex-wrap gap-10 transition-all ">
          <SearchInput
            value={filter.title || ""}
            onChange={updateFilterValue}
            onClear={clearFilterValue}
            searchInputTexts={{ placeholder: search }}
          />

          <div
            className={cn(
              "flex items-center justify-end ml-12 gap-4 flex-1",
              extraCriteriaClassname,
            )}
          >
            <RadioSort
              sortingOptions={finalSortingOptions}
              sort={sort}
              sortValue={sortValue}
              setSort={setSort}
              {...radioSortTexts}
              callback={resetCurrentPage}
              filterKey="title"
            />
            {extraCriteria && extraCriteria}
            {extraCriteriaWithCallBack &&
              extraCriteriaWithCallBack(resetCurrentPage)}
          </div>
        </div>
        <div>
          <CreationFilter
            {...creationFilterTexts}
            updateCreatedAtRange={updateCreatedAtRange}
            updateUpdatedAtRange={updateUpdatedAtRange}
          />
        </div>
      </div>
      <div className="w-full mt-10">
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6 lg:gap-8">
          {items.length === 0 && isFinished && !error && (
            <div className="w-full items-center justify-center md:col-span-2 lg:col-span-3 text-center mt-10">
              <h2 className="text-4xl tracking-tighter font-bold ">
                {noResults}

                <DynamicNoResultsLottie
                  loop
                  className="md:w-1/3 md:h-1/3 mx-auto"
                />
              </h2>
            </div>
          )}
          {items.length === 0 &&
            !isFinished &&
            Array.from({ length: sizeOptions?.[0] || 6 }).map((_, i) => (
              <LoadingItemCard key={i + "loading"} />
            ))}
          {items.map((item, i) => (
            <motion.div
              key={
                item.model.content.id +
                item.model.content.title +
                i +
                item.model.content.body.substring(1)
              }
              initial={{ opacity: 0, y: 50, scale: 0.5 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              transition={{
                duration: 0.25,
                // , delay: i * 0.125
              }}
            >
              <ItemCard
                item={item}
                onClick={() => {
                  onItemClick?.(item);
                }}
                generateExtraContent={passExtraContent}
                generateExtraHeader={passExtraHeader}
                texts={itemCardTexts}
                generateImageOverlay={passExtraImageOverlay}
                itemHref={itemLinkCallback ? itemLinkCallback(item) : undefined}
              />
            </motion.div>
          ))}
          {!isFinished && items.length > 0 && (
            <motion.div
              className="w-full flex flex-col items-center justify-center"
              initial={{ opacity: 0, y: 50, scale: 0.5 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              transition={{
                duration: 0.25,
                delay: 1,
              }}
            >
              <Loader className="w-full" />
              <p className="font-bold">{gettingMore}</p>
            </motion.div>
          )}
        </div>
        <div className={cn("mt-6", items.length === 0 && "hidden")}>
          <DataTablePagination
            pageInfo={pageInfo}
            setPageInfo={setPageInfo}
            sizeOptions={sizeOptions}
            {...dataTablePaginationTexts}
          />
        </div>
        {nextMessages && nextMessages.length > 0 && (
          <div className="hidden">
            {nextMessages.map((item, i) => (
              <div
                key={
                  item.content.model.content.id +
                  item.content.model.content.title +
                  i +
                  item.content.model.content.body.substring(1) +
                  "____next"
                }
              >
                <ItemCard item={item.content} texts={itemCardTexts} />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
