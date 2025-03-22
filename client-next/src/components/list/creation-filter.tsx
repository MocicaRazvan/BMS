"use client";

import {
  DateRange,
  DateRangePicker,
  DateRangePickerProps,
  DateRangePickerTexts,
} from "@/components/ui/date-range-picker";
import { useMemo } from "react";
import { ro } from "date-fns/locale";
import { useLocale } from "next-intl";
import { useSearchParams } from "next/navigation";
import { cn } from "@/lib/utils";
import { addDays } from "date-fns";
import { ButtonProps } from "@/components/ui/button";

export interface CreationFilterTexts {
  dateRangePickerTexts: DateRangePickerTexts;
  createdAtLabel: string;
  updatedAtLabel: string;
  hideCreatedAt?: boolean;
  hideUpdatedAt?: boolean;
  showLabels?: boolean;
}

interface Props extends CreationFilterTexts {
  updateCreatedAtRange?: DateRangePickerProps["onUpdate"];
  updateUpdatedAtRange?: DateRangePickerProps["onUpdate"];
  triggerVariant?: ButtonProps["variant"];
  triggerClassName?: string;
}

function changeEndRange(values: {
  range: DateRange;
  rangeCompare?: DateRange;
}) {
  let endRange = values.range.to;
  if (endRange) {
    endRange = addDays(endRange, 1);
  }
  return endRange;
}

export default function CreationFilter({
  dateRangePickerTexts,
  createdAtLabel,
  updatedAtLabel,
  updateCreatedAtRange = (v, n) => {},
  updateUpdatedAtRange = (v, n) => {},
  hideUpdatedAt,
  hideCreatedAt,
  showLabels = true,
  triggerVariant = "outline",
  triggerClassName,
}: Props) {
  const locale = useLocale();
  const currentSearchParams = useSearchParams();

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
  const defaultCreatedAtNone = useMemo(
    () =>
      defaultCreatedAtLowerBound === null &&
      defaultCreatedAtUpperBound === null,
    [defaultCreatedAtLowerBound, defaultCreatedAtUpperBound],
  );

  const defaultUpdatedAtNone = useMemo(
    () =>
      defaultUpdatedAtLowerBound === null &&
      defaultUpdatedAtUpperBound === null,
    [defaultUpdatedAtLowerBound, defaultUpdatedAtUpperBound],
  );
  const createAtDateRangePicker = useMemo(
    () => (
      <DateRangePicker
        onUpdate={(values, none) =>
          updateCreatedAtRange(
            {
              range: {
                from: values.range.from,
                to: changeEndRange(values),
              },
              rangeCompare: values.rangeCompare,
            },
            none,
          )
        }
        align="center"
        locale={locale === "ro" ? ro : undefined}
        hiddenPresets={["today", "yesterday", "lastWeek"]}
        showCompare={false}
        showNone={true}
        defaultNone={defaultCreatedAtNone}
        triggerVariant={triggerVariant}
        triggerClassName={triggerClassName}
        {...dateRangePickerTexts}
      />
    ),
    [
      dateRangePickerTexts,
      defaultCreatedAtNone,
      locale,
      triggerClassName,
      triggerVariant,
      updateCreatedAtRange,
    ],
  );

  const updatedAtDateRangePicker = useMemo(
    () => (
      <DateRangePicker
        onUpdate={(values, none) =>
          updateUpdatedAtRange(
            {
              range: {
                from: values.range.from,
                to: changeEndRange(values),
              },
              rangeCompare: values.rangeCompare,
            },
            none,
          )
        }
        align="center"
        locale={locale === "ro" ? ro : undefined}
        hiddenPresets={["today", "yesterday", "lastWeek"]}
        showCompare={false}
        showNone={true}
        defaultNone={defaultUpdatedAtNone}
        triggerVariant={triggerVariant}
        triggerClassName={triggerClassName}
        {...dateRangePickerTexts}
      />
    ),
    [
      dateRangePickerTexts,
      defaultUpdatedAtNone,
      locale,
      triggerClassName,
      triggerVariant,
      updateUpdatedAtRange,
    ],
  );
  return (
    <div
      className={cn(
        "flex flex-col md:flex-row items-center justify-between gap-5 md:gap-10 py-1 w-full ",
        hideCreatedAt || (hideUpdatedAt && "md:justify-end"),
      )}
    >
      {!hideCreatedAt && (
        <div className="flex items-center justify-center gap-2.5 md:gap-3">
          {showLabels && (
            <label className=" font-semibold">{createdAtLabel}</label>
          )}
          {createAtDateRangePicker}
        </div>
      )}
      {!hideUpdatedAt && (
        <div className="flex items-center justify-center gap-2.5 md:gap-3">
          {showLabels && (
            <label className=" font-semibold">{updatedAtLabel}</label>
          )}
          {updatedAtDateRangePicker}
        </div>
      )}
    </div>
  );
}
