"use client";

import { Skeleton } from "@/components/ui/skeleton";
import {
  DateRangePicker,
  DateRangePickerTexts,
} from "@/components/ui/date-range-picker";
import { useDayCalendar } from "@/context/day-calendar-context";
import { useEffect, useMemo, useRef, useState } from "react";
import { endOfMonth, format, startOfMonth } from "date-fns";
import { dateFormat } from "@/hoooks/useDateRangeFilterParams";
import { useLocale } from "next-intl";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import { DayCalendarResponse, DayCalendarTrackingStats } from "@/types/dto";
import { ro } from "date-fns/locale";
import { motion } from "framer-motion";
import dynamic from "next/dynamic";
import { useDebounce } from "@/components/ui/multiple-selector";

const DynamicNoResultsLottie = dynamic(
  () => import("@/components/lottie/no-results-lottie"),
  {
    ssr: false,
    loading: () => (
      <Skeleton className="w-full h-full md:w-1/3 md:h-1/3 mx-auto" />
    ),
  },
);

const DynamicDayCalendarStatsChart = dynamic(
  () =>
    import("@/components/charts/day-calendar-stats-chart").then(
      (mod) => mod.default,
    ),
  {
    ssr: false,
    loading: () => <Skeleton className="aspect-auto h-[350px] w-full" />,
  },
);

const ChartSkeleton = () => (
  <motion.div
    initial={{ opacity: 0 }}
    animate={{ opacity: 1 }}
    transition={{ delay: 1 }}
  >
    <Skeleton className="h-[350px] w-full" />
  </motion.div>
);

export interface DayCalendarStatsWrapperTexts {
  dateRangePickerTexts: DateRangePickerTexts;
  errorText: string;
  noDataText: string;
  titleText: string;
}

interface WrapperProps {
  texts: DayCalendarStatsWrapperTexts;
}
export default function DayCalendarStatsWrapper({
  texts: { dateRangePickerTexts, noDataText, errorText, titleText },
}: WrapperProps) {
  const {
    date,
    dayCalendars,
    isFinished: isDaysFinished,
    isAbsoluteFinished: isDaysAbsoluteFinished,
    messages: dayCalendarStatsMessages,
  } = useDayCalendar();
  const monthStartDate = useMemo(() => startOfMonth(date), [date]);
  const monthEndDate = useMemo(() => endOfMonth(date), [date]);
  const monthStart = useMemo(
    () => format(monthStartDate, dateFormat),
    [monthStartDate],
  );
  const monthEnd = useMemo(
    () => format(monthEndDate, dateFormat),
    [monthEndDate],
  );
  const [dateRange, setDateRange] = useState<
    Record<string, string> | undefined
  >({
    from: monthStart,
    to: monthEnd,
  });

  const prevDaysRefetch = useRef<DayCalendarResponse[] | null>(null);

  useEffect(() => {
    if (
      prevDaysRefetch.current === null &&
      isDaysAbsoluteFinished &&
      dayCalendarStatsMessages
    ) {
      prevDaysRefetch.current = dayCalendarStatsMessages.map((d) => d.content);
    }
  }, [dayCalendarStatsMessages, isDaysAbsoluteFinished]);

  useEffect(() => {
    setDateRange({
      from: monthStart,
      to: monthEnd,
    });
  }, [monthStart, monthEnd]);

  const locale = useLocale();
  const { messages, isFinished, error, refetch, isAbsoluteFinished } =
    useFetchStream<DayCalendarTrackingStats>({
      path: "/daysCalendar/trackingStats",
      authToken: true,
      queryParams: dateRange,
      trigger: isDaysAbsoluteFinished,
    });

  //todo fix
  const debouncedDayCalendar = useDebounce(dayCalendars, 30);

  // todo fix
  // can trigger more then once sometimes, but request dedup is handling it
  useEffect(() => {
    if (
      prevDaysRefetch.current &&
      isAbsoluteFinished &&
      isDaysAbsoluteFinished &&
      debouncedDayCalendar.length > 0 &&
      debouncedDayCalendar.map((d) => d.id).join(",") !==
        prevDaysRefetch.current.map((d) => d.id).join(",")
    ) {
      refetch();
      prevDaysRefetch.current = debouncedDayCalendar;
    }
  }, [
    debouncedDayCalendar,
    isDaysAbsoluteFinished,
    isAbsoluteFinished,
    refetch,
  ]);

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center mt-20">
        <h1 className="text-destructive">{errorText}</h1>
      </div>
    );
  }
  return (
    <div className="size-full space-y-3 md:space-y-8">
      <h2 className="text-lg md:text-3xl text-center tracking-tight font-semibold capitalize">
        {titleText}
      </h2>
      <div>
        <DateRangePicker
          key={`${monthStart}-${monthEnd}-${locale}-date-range-picker`}
          showCompare={false}
          showNone={true}
          onUpdate={({ range }, none) => {
            console.log("noneRange", none);
            if (none) {
              setDateRange(undefined);
              return;
            }
            setDateRange({
              from: format(range.from, dateFormat),
              to: format(range.to || range.from, dateFormat),
            });
          }}
          locale={locale === "ro" ? ro : undefined}
          hiddenPresets={["pastYear", "today", "yesterday"]}
          initialDateFrom={monthStartDate}
          initialDateTo={monthEndDate}
          {...dateRangePickerTexts}
        />
      </div>
      {isFinished ? (
        messages.length > 0 ? (
          <DynamicDayCalendarStatsChart data={messages} />
        ) : (
          <div className="flex w-full items-center justify-center h-[350px]">
            <div className="flex flex-col items-center justify-center">
              <DynamicNoResultsLottie
                loop
                className="md:w-1/2 md:h-1/2 mx-auto"
              />
              <h1 className="font-medium text-lg">{noDataText}</h1>
            </div>
          </div>
        )
      ) : (
        <ChartSkeleton />
      )}
    </div>
  );
}
