"use client";

import { TitleBodyImagesUserDto, ResponseWithUserDtoEntity } from "@/types/dto";
import { ReactNode } from "react";
import { cn } from "@/lib/utils";

import { SortDirection } from "@/types/fetch-utils";

import Loader from "@/components/ui/spinner";
import ItemCard, { ItemCardTexts } from "@/components/list/item-card";
import {
  DataTablePagination,
  DataTablePaginationTexts,
} from "@/components/table/data-table-pagination";
import LoadingSpinner from "@/components/common/loading-spinner";

import SearchInput from "@/components/forms/input-serach";
import useList, { UseListProps } from "@/hoooks/useList";
import RadioSort, { RadioSortTexts } from "@/components/common/radio-sort";

import { motion } from "framer-motion";

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
}

interface GridListProps<T extends TitleBodyImagesUserDto>
  extends GridListTexts,
    UseListProps {
  onItemClick: (item: ResponseWithUserDtoEntity<T>) => void;
  passExtraContent?: (item: ResponseWithUserDtoEntity<T>) => ReactNode;
  passExtraHeader?: (item: ResponseWithUserDtoEntity<T>) => ReactNode;
  passExtraImageOverlay?: (item: ResponseWithUserDtoEntity<T>) => ReactNode;
  extraCriteriaWithCallBack?: (callback: () => void) => ReactNode;
}

export default function GridList<T extends TitleBodyImagesUserDto>({
  onItemClick,
  sizeOptions,
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
}: GridListProps<T>) {
  const {
    messages,
    pageInfo,
    filter,
    setFilter,
    debouncedFilter,
    sort,
    setSort,
    sortValue,
    setSortValue,
    items,
    updateSortState,
    isFinished,
    error,
    updateFilterValue,
    clearFilterValue,
    setPageInfo,
    resetCurrentPage,
  } = useList<ResponseWithUserDtoEntity<T>>({
    path,
    extraQueryParams,
    extraArrayQueryParam,
    extraUpdateSearchParams,
    sizeOptions,
    sortingOptions,
  });
  console.log("sort", sort);

  return (
    <div className="w-full ">
      <div className="my-10 w-full flex items-start justify-start flex-wrap gap-10 transition-all ">
        <SearchInput
          value={filter.title || ""}
          onChange={updateFilterValue}
          onClear={clearFilterValue}
          searchInputTexts={{ placeholder: search }}
        />
        <div className="flex items-center justify-center ml-12 gap-2 ">
          <RadioSort
            sortingOptions={sortingOptions}
            sort={sort}
            sortValue={sortValue}
            setSort={setSort}
            setSortValue={setSortValue}
            {...radioSortTexts}
            callback={resetCurrentPage}
          />
        </div>
        {extraCriteria && extraCriteria}
        {extraCriteriaWithCallBack &&
          extraCriteriaWithCallBack(resetCurrentPage)}
      </div>
      <div className="w-full mt-10">
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6 lg:gap-8">
          {items.length === 0 && isFinished && !error && (
            <div className="w-full items-center justify-center md:col-span-2 lg:col-span-3 text-center mt-10">
              <h2 className="text-4xl tracking-tighter font-bold ">
                {noResults}
              </h2>
            </div>
          )}
          {items.length === 0 && !isFinished && (
            <section className="mt-10 w-full md:col-span-2 lg:col-span-3 ">
              <LoadingSpinner />
            </section>
          )}
          {items.map((item, i) => (
            <motion.div
              key={
                item.model.content.title +
                i +
                item.model.content.body.substring(1)
              }
              initial={{ opacity: 0, y: 50, scale: 0.5 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              transition={{ duration: 0.3, delay: i * 0.15 }}
            >
              <ItemCard
                item={item}
                onClick={() => onItemClick(item)}
                generateExtraContent={passExtraContent}
                generateExtraHeader={passExtraHeader}
                texts={itemCardTexts}
                generateImageOverlay={passExtraImageOverlay}
              />
            </motion.div>
          ))}
          {!isFinished && items.length > 0 && (
            <div className="w-full flex flex-col items-center justify-center">
              <Loader className="w-full" />
              <p className="font-bold">{gettingMore}</p>
            </div>
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
      </div>
    </div>
  );
}
