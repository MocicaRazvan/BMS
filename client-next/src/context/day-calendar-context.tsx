"use client";

import {
  createContext,
  ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { WithUser } from "@/lib/user";
import {
  addMonths,
  eachDayOfInterval,
  endOfMonth,
  endOfWeek,
  format,
  startOfMonth,
  startOfWeek,
} from "date-fns";
import { CustomEntityModel, DayCalendarResponse, DayType } from "@/types/dto";
import useFetchStream, { UseFetchStreamProps } from "@/hoooks/useFetchStream";
import { usePathname, useRouter } from "@/navigation";
import { useSearchParams } from "next/navigation";

interface DayCalendarContextType extends WithUser {
  dayCalendars: DayCalendarResponse[];
  addDayCalendar: (day: DayCalendarResponse) => void;
  removeDayCalendar: (dayId: number) => void;
  date: Date;
  setDate: (date: Date) => void;
  changeForDate: (day: DayCalendarResponse) => void;
  calendarDays: Date[];
  monthStart: Date;
  monthEnd: Date;
  calendarStart: Date;
  calendarEnd: Date;
  isFinished: boolean;
  refetch: () => void;
}

const DayCalendarContext = createContext<DayCalendarContextType | null>(null);

interface Props extends WithUser {
  children: ReactNode;
}

function getDateRanges(date: Date) {
  // Get the first day of the month
  const monthStart = startOfMonth(date);
  // Get the last day of the month
  const monthEnd = endOfMonth(date);

  // Get the first Monday of the first week (may be in previous month)
  const calendarStart = startOfWeek(monthStart, { weekStartsOn: 1 });
  // Get the last Sunday of the last week (may be in next month)
  const calendarEnd = endOfWeek(monthEnd, { weekStartsOn: 1 });

  // Get all days between start and end
  const calendarDays = eachDayOfInterval({
    start: calendarStart,
    end: calendarEnd,
  });
  return { monthStart, monthEnd, calendarStart, calendarEnd, calendarDays };
}

export const getColorsByDayType = (opacity = 1): Record<DayType, string> => ({
  LOW_CARB: `hsl(var(--chart-1)/${opacity})`,
  HIGH_CARB: `hsl(var(--chart-2)/${opacity})`,
  HIGH_PROTEIN: `hsl(var(--chart-8)/${opacity})`,
  LOW_FAT: `hsl(var(--chart-4)/${opacity})`,
  HIGH_FAT: `hsl(var(--chart-10)/${opacity})`,
  LOW_PROTEIN: `hsl(var(--chart-6)/${opacity})`,
  BALANCED: `hsl(var(--chart-9)/${opacity})`,
});

export const getColorByDayType = (dayType: DayType, opacity = 1) =>
  getColorsByDayType(opacity)?.[dayType];

const baseFetchAgs: UseFetchStreamProps = {
  path: "/daysCalendar/byRange",
  authToken: true,
};

export default function DayCalendarProvider({ children, authUser }: Props) {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();
  const initialDate = useMemo(() => {
    const param = searchParams.get("date");
    const maybeDate = param ? new Date(param) : new Date();
    return isNaN(maybeDate.getTime()) ? new Date() : maybeDate;
  }, [searchParams?.toString()]);

  const [date, setDate] = useState(initialDate);

  useEffect(() => {
    const param = searchParams.get("date");

    if (param) {
      const maybeDate = new Date(param);
      if (!isNaN(maybeDate.getTime())) {
        setDate(maybeDate);
      }
      const updatedParams = new URLSearchParams(searchParams.toString());
      updatedParams.delete("date");

      const newPath =
        pathname +
        (updatedParams.toString() ? `?${updatedParams.toString()}` : "");

      router.replace(newPath, { scroll: false });
    }
  }, [searchParams.get("date")]);

  const [dayCalendars, setDayCalendars] = useState<DayCalendarResponse[]>([]);
  const { monthStart, monthEnd, calendarStart, calendarEnd, calendarDays } =
    getDateRanges(date);
  const { messages, error, isFinished, manualFetcher, refetch } =
    useFetchStream<CustomEntityModel<DayCalendarResponse>>({
      ...baseFetchAgs,
      queryParams: {
        start: format(calendarStart, "yyyy-MM-dd"),
        end: format(calendarEnd, "yyyy-MM-dd"),
      },
    });

  useEffect(() => {
    if (messages && messages.length > 0) {
      setDayCalendars(messages.map((d) => d.content));
    }
  }, [JSON.stringify(messages)]);

  const isoDate = date.toISOString();
  useEffect(() => {
    let isMounted = true;
    const abortController = new AbortController();

    if (isFinished && isMounted) {
      const nextDate = addMonths(date, 1);
      const { calendarStart, calendarEnd } = getDateRanges(nextDate);
      manualFetcher({
        fetchProps: {
          ...baseFetchAgs,
          queryParams: {
            start: format(calendarStart, "yyyy-MM-dd"),
            end: format(calendarEnd, "yyyy-MM-dd"),
          },
        },
      }).catch((e) => {
        console.error(e);
      });
    }
    return () => {
      isMounted = false;
      if (abortController && !abortController?.signal?.aborted) {
        abortController?.abort();
        (abortController as any)?.customAbort?.();
      }
    };
  }, [isoDate, isFinished]);

  const addDayCalendar = useCallback(
    (day: DayCalendarResponse) => {
      setDayCalendars((prevState) => [...prevState, day]);
      refetch();
    },
    [refetch],
  );
  const removeDayCalendar = useCallback(
    (dayId: number) => {
      setDayCalendars((prevState) => prevState.filter((d) => d.id !== dayId));
      refetch();
    },
    [refetch],
  );

  const changeForDate = useCallback(
    (day: DayCalendarResponse) => {
      setDayCalendars((prev) => prev.map((d) => (d.id === day.id ? day : d)));
      refetch();
    },
    [refetch],
  );

  return (
    <DayCalendarContext.Provider
      value={{
        dayCalendars,
        addDayCalendar,
        removeDayCalendar,
        date,
        setDate,
        changeForDate,
        calendarDays,
        monthStart,
        monthEnd,
        calendarStart,
        calendarEnd,
        isFinished,
        authUser,
        refetch,
      }}
    >
      {children}
    </DayCalendarContext.Provider>
  );
}

export function useDayCalendar() {
  const context = useContext(DayCalendarContext);
  if (!context) {
    throw new Error("useDayCalendar must be used within a DayCalendarProvider");
  }
  return context;
}
