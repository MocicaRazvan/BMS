"use client";

import { WithUser } from "@/lib/user";
import React, { useCallback } from "react";
import MultipleSelector, { Option } from "@/components/ui/multiple-selector";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { SortDirection } from "@/types/fetch-utils";

export interface ChildInputMultipleSelectorTexts {
  placeholder: string;
  loading: string;
  noResults: string;
}

interface Props<R> extends WithUser, ChildInputMultipleSelectorTexts {
  path: string;
  mapping: (i: R) => Option;
  onChange?: (options: Option[]) => void;
  value: Option[];
  disabled: boolean;
  extraQueryParams?: Record<string, string>;
  sortingCriteria?: Record<string, SortDirection>;
  valueKey: string;
  maxSelected?: number;
  giveUnselectedValue?: boolean;
  allowDuplicates?: boolean;
  pageSize?: number;
  addNameSortWhenSearchingInputEmpty?: boolean;
}

export default function ChildInputMultipleSelector<R>({
  authUser,
  path,
  mapping,
  onChange,
  value,
  disabled,
  loading,
  noResults,
  placeholder,
  extraQueryParams,
  sortingCriteria = {},
  valueKey,
  maxSelected,
  giveUnselectedValue = true,
  allowDuplicates = false,
  pageSize = 10,
  addNameSortWhenSearchingInputEmpty = true,
}: Props<R>) {
  const fetchData = useCallback(
    async (value: string): Promise<Option[]> => {
      const extraSortingCriteria: typeof sortingCriteria = {};
      if (addNameSortWhenSearchingInputEmpty && value === "") {
        extraSortingCriteria[valueKey] = "asc";
      }
      const res = await fetchStream<R>({
        path,
        token: authUser.token,
        queryParams: {
          [valueKey]: value,
          ...(extraQueryParams && extraQueryParams),
        },
        method: "PATCH",
        body: {
          page: 0,
          size: pageSize,
          sortingCriteria: {
            ...extraSortingCriteria,
            ...sortingCriteria,
          },
        },
        acceptHeader: "application/x-ndjson",
      });

      return res.messages.map(mapping);
    },
    [
      addNameSortWhenSearchingInputEmpty,
      authUser.token,
      extraQueryParams,
      mapping,
      pageSize,
      path,
      sortingCriteria,
      valueKey,
    ],
  );

  return (
    <div className="w-full">
      <MultipleSelector
        onSearch={fetchData}
        disabled={disabled}
        value={value}
        onChange={onChange}
        giveUnselected={giveUnselectedValue}
        maxSelected={maxSelected}
        placeholder={placeholder}
        allowDuplicates={allowDuplicates}
        loadingIndicator={
          <p className="py-2 text-center text-lg leading-10 text-muted-foreground">
            {loading}
          </p>
        }
        emptyIndicator={
          <p className="w-full text-center text-lg leading-10 text-muted-foreground">
            {noResults}
          </p>
        }
      />
    </div>
  );
}
