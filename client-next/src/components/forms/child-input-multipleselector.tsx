"use client";

import { WithUser } from "@/lib/user";
import React, { useCallback, useMemo, useState } from "react";
import MultipleSelector, {
  MultipleSelectorProps,
  Option,
} from "@/components/ui/multiple-selector";
import { SortDirection } from "@/types/fetch-utils";
import useFetchStream, {
  UseFetchStreamProps,
} from "@/lib/fetchers/useFetchStream";

export interface ChildInputMultipleSelectorTexts {
  placeholder: string;
  loading: string;
  noResults: string;
}

interface Props<R>
  extends WithUser,
    ChildInputMultipleSelectorTexts,
    Pick<
      MultipleSelectorProps,
      "giveUnselected" | "maxSelected" | "allowDuplicates" | "closeOnSelect"
    > {
  path: string;
  mapping: (i: R) => Option;
  onChange?: (options: Option[]) => void;
  value?: Option[];
  disabled: boolean;
  extraQueryParams?: Record<string, string>;
  sortingCriteria?: Record<string, SortDirection>;
  valueKey: string;
  giveUnselectedValue?: boolean;
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
  closeOnSelect = false,
}: Props<R>) {
  const baseParams: UseFetchStreamProps = useMemo(
    () => ({
      path,
      token: authUser.token,
      queryParams: {
        [valueKey]: "",
        ...(extraQueryParams && extraQueryParams),
      },
      method: "PATCH",
      body: {
        page: 0,
        size: pageSize,
        sortingCriteria: {
          ...(addNameSortWhenSearchingInputEmpty
            ? {
                [valueKey]: "asc",
              }
            : {}),
          ...sortingCriteria,
        },
      },
      acceptHeader: "application/x-ndjson",
      refetchOnFocus: false,
      trigger: !value || value?.length === 0,
    }),
    [
      addNameSortWhenSearchingInputEmpty,
      authUser.token,
      pageSize,
      path,
      JSON.stringify(extraQueryParams),
      JSON.stringify(sortingCriteria),
      valueKey,
      JSON.stringify(value),
    ],
  );
  const [curParams, setCurParams] = useState<UseFetchStreamProps>(baseParams);
  const { messages: initialMessage, isFinished: initialFinished } =
    useFetchStream<R>(curParams);
  const initialOptions = useMemo(
    () =>
      initialMessage && initialMessage.length > 0
        ? initialMessage?.map(mapping)
        : [],
    [initialMessage, mapping],
  );

  const fetchData = useCallback(
    async (searchValue: string): Promise<Option[]> => {
      const extraSortingCriteria: typeof sortingCriteria = {};
      if (addNameSortWhenSearchingInputEmpty && searchValue === "") {
        extraSortingCriteria[valueKey] = "asc";
      }
      setCurParams((cur) => ({
        ...cur,
        queryParams: {
          [valueKey]: searchValue,
          ...(extraQueryParams && extraQueryParams),
        },
        body: {
          ...cur.body,
          sortingCriteria: {
            ...extraSortingCriteria,
            ...sortingCriteria,
          },
        },
        trigger: true,
      }));
      return [];
    },
    [
      addNameSortWhenSearchingInputEmpty,
      JSON.stringify(extraQueryParams),
      JSON.stringify(sortingCriteria),
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
        hidePlaceholderWhenSelected={true}
        closeOnSelect={closeOnSelect}
        defaultOptions={initialOptions}
        watcherMode={true}
        isWatcherLoading={!initialFinished}
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
