"use client";

import { useCallback, useMemo, useState } from "react";
import {
  DateRange,
  DateRangePickerProps,
} from "@/components/ui/date-range-picker";
import { format } from "date-fns";
const dateFormat = "dd-MM-yyyy";

function verifyDate(date: string | undefined) {
  if (!date) return undefined;
  return date.match(/^\d{2}-\d{2}-\d{4}$/) ? date : undefined;
}

export default function useDateRangeFilterParams(
  lowerBoundKey: string,
  upperBoundKey: string,
  defaultRangeLowerBound?: string,
  defaultRangeUpperBound?: string,
) {
  const [dateRange, setDateRange] = useState<
    Record<string, string | undefined>
  >({
    [lowerBoundKey]: verifyDate(defaultRangeLowerBound),
    [upperBoundKey]: verifyDate(defaultRangeUpperBound),
  });

  const updateRange: DateRangePickerProps["onUpdate"] = useCallback(
    ({ range }: { range: DateRange }, none?: boolean) => {
      if (none) {
        setDateRange({
          [lowerBoundKey]: undefined,
          [upperBoundKey]: undefined,
        });
      } else {
        setDateRange({
          [lowerBoundKey]: format(range.from, dateFormat),
          [upperBoundKey]: format(range.to || range.from, dateFormat),
        });
      }
    },
    [lowerBoundKey, upperBoundKey],
  );

  const queryParams = useMemo(
    () => ({
      ...(dateRange[lowerBoundKey] && {
        [lowerBoundKey]: dateRange[lowerBoundKey],
      }),
      ...(dateRange[upperBoundKey] && {
        [upperBoundKey]: dateRange[upperBoundKey],
      }),
    }),
    [dateRange, lowerBoundKey, upperBoundKey],
  );
  const updateSearchParams = useCallback(
    (params: URLSearchParams) => {
      if (dateRange[lowerBoundKey]) {
        params.set(lowerBoundKey, dateRange[lowerBoundKey] || "");
      } else {
        params.delete(lowerBoundKey);
      }
      if (dateRange[upperBoundKey]) {
        params.set(upperBoundKey, dateRange[upperBoundKey] || "");
      } else {
        params.delete(upperBoundKey);
      }
    },
    [dateRange, lowerBoundKey, upperBoundKey],
  );

  return {
    queryParams,
    updateRange,
    updateSearchParams,
  };
}
