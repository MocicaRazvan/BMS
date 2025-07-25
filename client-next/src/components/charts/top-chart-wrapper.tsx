"use client";
import {
  DateRangeParams,
  DateRangePicker,
  DateRangePickerTexts,
} from "@/components/ui/date-range-picker";
import { Locale } from "@/navigation/navigation";
import React, { ReactNode, useMemo, useState } from "react";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import { format, subMonths } from "date-fns";
import { ro } from "date-fns/locale";
import { motion } from "framer-motion";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { cn } from "@/lib/utils";
import LoadingSpinner from "@/components/common/loading-spinner";
import { RankSummary } from "@/types/dto";
import { Badge } from "@/components/ui/badge";
import { ChartConfig } from "@/components/ui/chart";
import dynamic from "next/dynamic";
import { Skeleton } from "@/components/ui/skeleton";

export interface TopChartWrapperTexts {
  dateRangePickerTexts: DateRangePickerTexts;
  topLabel: string;
  periodLabel: string;
  noResults: string;
}

interface Props<T> {
  locale: Locale;
  texts: TopChartWrapperTexts;
  path: string;
  processMessage: (message: T) => ReactNode;
  title: string;
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

const now = new Date();
const oneMonthAgo = subMonths(now, 1);
const dateFormat = "dd-MM-yyyy";
const formattedNow = format(now, dateFormat);
const formattedOneMonthAgo = format(oneMonthAgo, dateFormat);
const topOptions = Array.from({ length: 10 }, (_, i) => i + 1);
const TopChartWrapper = <T extends RankSummary>({
  locale,
  path,
  processMessage,
  title,
  texts: { dateRangePickerTexts, topLabel, periodLabel, noResults },
}: Props<T>) => {
  const [top, setTop] = useState<string>("2");
  const [dateRange, setDateRange] = useState<DateRangeParams>({
    from: formattedOneMonthAgo,
    to: formattedNow,
  });

  const { messages, error, isFinished } = useFetchStream<T>({
    path,
    authToken: true,
    method: "GET",
    queryParams: {
      ...dateRange,
      top,
    },
  });
  const dateRangePicker = useMemo(
    () => (
      <DateRangePicker
        onUpdate={({ range: { from, to } }) =>
          setDateRange({
            from: format(from, dateFormat),
            to: format(to || from, dateFormat),
          })
        }
        align="center"
        locale={locale === "ro" ? ro : undefined}
        defaultPreset={"lastMonth"}
        showCompare={false}
        {...dateRangePickerTexts}
      />
    ),
    [dateRangePickerTexts, locale],
  );

  const noMessageOrError = !messages.length || error !== null;

  return (
    <motion.div
      className="w-full h-full p-4 "
      initial={{ opacity: 0 }}
      whileInView={{ opacity: 1 }}
      viewport={{ once: true, amount: "some" }}
    >
      <h2 className="text-2xl lg:text-3xl font-bold tracking-tight capitalize inline ">
        {title}
      </h2>
      <div className="flex flex-col md:flex-row items-center justify-around w-full gap-5 md:gap-1 mt-2 mb-12">
        <div className="flex items-center gap-2">
          <Label className="text-lg font-semibold">{periodLabel}</Label>
          {dateRangePicker}
        </div>
        <div className="flex items-center gap-2">
          <Label className="text-lg font-semibold" htmlFor="top-select">
            {topLabel}
          </Label>
          <Select value={top} onValueChange={(v) => setTop(v)}>
            <SelectTrigger className="w-36" id="top-select">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {topOptions.map((option) => (
                <SelectItem
                  key={option + "select"}
                  value={`${option}`}
                  className={cn(
                    "cursor-pointer capitalize",
                    messages.length === option && "text-amber",
                  )}
                >
                  {option}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>
      {!isFinished ? (
        <LoadingSpinner sectionClassName="min-h-[450px] md:min-h-[650px] w-full h-full pb-10 " />
      ) : noMessageOrError ? (
        <div className="block w-full h-full">
          <h2 className="text-4xl tracking-tighter font-bold w-full max-w-3xl max-h-[550px] mx-auto">
            <p className="text-center">{noResults}</p>
            <DynamicNoResultsLottie
              loop
              className="md:w-1/3 md:h-1/3 mx-auto"
            />
          </h2>
        </div>
      ) : (
        <div className={"grid gap-4 md:grid-cols-2 min-h-[500px]"}>
          {messages
            .sort((a, b) => a.rank - b.rank)
            .map((ts) => processMessage(ts))}
        </div>
      )}
    </motion.div>
  );
};

export default TopChartWrapper;

export function TopRankBadge({
  rankLabel,
  rank,
}: {
  rankLabel: string;
  rank: number;
}) {
  return (
    <Badge variant={rank === 1 ? "success" : "default"}>
      {rankLabel}
      {` #`}
      {rank}
    </Badge>
  );
}

export const createChartConfig = (
  arr: string[],
  offset = 0,
  lowerCase = true,
) =>
  arr.reduce((acc, t, i) => {
    acc[lowerCase ? t.toLowerCase() : t] = {
      label: t.replace("_", " "),
      color: `hsl(var(--chart-${((i + 1 + offset) % 10) + 1}))`,
    };
    return acc;
  }, {} as ChartConfig);
