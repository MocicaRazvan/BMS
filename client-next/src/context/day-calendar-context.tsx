"use client";

import {
  createContext,
  Dispatch,
  ReactNode,
  SetStateAction,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
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
import { CustomEntityModel, DayCalendarResponse } from "@/types/dto";
import useFetchStream, {
  UseFetchStreamProps,
} from "@/lib/fetchers/useFetchStream";
import { usePathname } from "@/navigation/navigation";
import { useRouter } from "@/navigation/client-navigation";
import { useSearchParams } from "next/navigation";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import {
  FlattenPrefetchedPredicate,
  FlattenPrefetchGenerateKeyValue,
  FlattenPrefetchGenerateMarkPrefetchedArgs,
  PrefetchGenerateNewArgs,
  UseFetchStreamPrefetcherReturn,
  useFlattenPrefetcher,
} from "@/lib/fetchers/use-prefetcher";
import { useCounter } from "react-use";

export interface DayCalendarContextType extends WithUser {
  dayCalendars: DayCalendarResponse[];
  addDayCalendar: (day: DayCalendarResponse) => void;
  removeDayCalendar: (dayId: number) => void;
  date: Date;
  setDate: Dispatch<SetStateAction<Date>>;
  changeForDate: (day: DayCalendarResponse) => void;
  calendarDays: Date[];
  monthStart: Date;
  monthEnd: Date;
  calendarStart: Date;
  calendarEnd: Date;
  isFinished: boolean;
  refetch: () => void;
  isAbsoluteFinished: boolean;
  messages: CustomEntityModel<DayCalendarResponse>[];
  daysLocalChange: number;
  addToUpdateDaysCallbacks: (key: string, cb: () => void) => void;
}

const DayCalendarContext = createContext<DayCalendarContextType | null>(null);

interface Props {
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

const baseFetchAgs: UseFetchStreamProps = {
  path: "/daysCalendar/byRange",
  authToken: true,
};

const createPrefetchKey = ({
  calendarStart,
  calendarEnd,
}: {
  calendarStart: Date;
  calendarEnd: Date;
}) => {
  return `${calendarStart.toString()}-${calendarEnd.toString()}`;
};

function useDayCalendarPrefetcher({
  returned,
  date,
}: {
  returned: UseFetchStreamPrefetcherReturn<
    CustomEntityModel<DayCalendarResponse>
  >;
  date: Date;
}) {
  const { calendarStart, calendarEnd } = useMemo(
    () => getDateRanges(date),
    [date],
  );

  const nextDate = useMemo(() => addMonths(date, 1), [date]);

  const { calendarStart: nextCalendarStart, calendarEnd: nextCalendarEnd } =
    useMemo(() => getDateRanges(nextDate), [nextDate]);

  const previousDate = useMemo(() => addMonths(date, -1), [date]);
  const {
    calendarStart: previousCalendarStart,
    calendarEnd: previousCalendarEnd,
  } = useMemo(() => getDateRanges(previousDate), [previousDate]);

  const generateKeyValue: FlattenPrefetchGenerateKeyValue<
    CustomEntityModel<DayCalendarResponse>
  > = useCallback(
    (messages) =>
      createPrefetchKey({
        calendarStart,
        calendarEnd,
      }),
    [calendarEnd, calendarStart],
  );
  const nextPredicate: FlattenPrefetchedPredicate<
    CustomEntityModel<DayCalendarResponse>
  > = useCallback(
    (messages, hasPrefetched) =>
      !hasPrefetched(
        createPrefetchKey({
          calendarStart: nextCalendarStart,
          calendarEnd: nextCalendarEnd,
        }),
      ),
    [nextCalendarEnd, nextCalendarStart],
  );

  const generateNextArgs: PrefetchGenerateNewArgs<
    CustomEntityModel<DayCalendarResponse>
  > = useCallback(
    (messages) => ({
      ...baseFetchAgs,
      queryParams: {
        start: format(nextCalendarStart, "yyyy-MM-dd"),
        end: format(nextCalendarEnd, "yyyy-MM-dd"),
      },
    }),
    [nextCalendarEnd, nextCalendarStart],
  );
  const generateMarkPrefetchedNextArgs: FlattenPrefetchGenerateMarkPrefetchedArgs<
    CustomEntityModel<DayCalendarResponse>
  > = useCallback(
    (messages, newArgs) =>
      createPrefetchKey({
        calendarStart: nextCalendarStart,
        calendarEnd: nextCalendarEnd,
      }),
    [nextCalendarEnd, nextCalendarStart],
  );

  const previousPredicate: FlattenPrefetchedPredicate<
    CustomEntityModel<DayCalendarResponse>
  > = useCallback(
    (messages, hasPrefetched) => {
      return !hasPrefetched(
        createPrefetchKey({
          calendarStart: previousCalendarStart,
          calendarEnd: previousCalendarEnd,
        }),
      );
    },
    [previousCalendarEnd, previousCalendarStart],
  );

  const generatePreviousArgs: PrefetchGenerateNewArgs<
    CustomEntityModel<DayCalendarResponse>
  > = useCallback(
    (messages) => {
      return {
        ...baseFetchAgs,
        queryParams: {
          start: format(previousCalendarStart, "yyyy-MM-dd"),
          end: format(previousCalendarEnd, "yyyy-MM-dd"),
        },
      };
    },
    [previousCalendarEnd, previousCalendarStart],
  );

  const generateMarkPrefetchedPreviousArgs: FlattenPrefetchGenerateMarkPrefetchedArgs<
    CustomEntityModel<DayCalendarResponse>
  > = useCallback(
    (messages, newArgs) =>
      createPrefetchKey({
        calendarStart: previousCalendarStart,
        calendarEnd: previousCalendarEnd,
      }),
    [previousCalendarEnd, previousCalendarStart],
  );

  return useFlattenPrefetcher<CustomEntityModel<DayCalendarResponse>>({
    returned,
    generateKeyValue,
    nextPredicate,
    generateNextArgs,
    generateMarkPrefetchedNextArgs,
    previousPredicate,
    generatePreviousArgs,
    generateMarkPrefetchedPreviousArgs,
    preloadNext: true,
  });
}

export default function DayCalendarProvider({ children }: Props) {
  const { authUser } = useAuthUserMinRole();
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();
  const initialDate = useMemo(() => {
    const param = searchParams.get("date");
    const maybeDate = param ? new Date(param) : new Date();
    return isNaN(maybeDate.getTime()) ? new Date() : maybeDate;
  }, [searchParams]);
  const [daysLocalChange, { inc }] = useCounter(0);

  const [date, setDate] = useState(initialDate);

  const updateDaysCallbacks = useRef<Record<string, () => void>>({});

  const callUpdateDaysCallbacks = useCallback(
    () => Object.values(updateDaysCallbacks.current).forEach((cb) => cb()),
    [],
  );

  const addToUpdateDaysCallbacks = useCallback(
    (key: string, cb: () => void) => {
      if (updateDaysCallbacks.current[key]) {
        return;
      }
      updateDaysCallbacks.current[key] = cb;
    },
    [],
  );

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
  }, [pathname, router, searchParams]);

  const [dayCalendars, setDayCalendars] = useState<DayCalendarResponse[]>([]);
  const { monthStart, monthEnd, calendarStart, calendarEnd, calendarDays } =
    getDateRanges(date);

  const {
    messages,
    error,
    isFinished,
    manualFetcher,
    refetch,
    removeFromCache,
    isAbsoluteFinished,
    isRefetchClosure,
  } = useFetchStream<CustomEntityModel<DayCalendarResponse>>({
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
  }, [messages]);

  useEffect(() => {
    if (daysLocalChange === 0 && isAbsoluteFinished) {
      inc();
    }
  }, [daysLocalChange, inc, isAbsoluteFinished]);

  useDayCalendarPrefetcher({
    returned: {
      isRefetchClosure,
      error,
      messages,
      isAbsoluteFinished,
      manualFetcher,
    },
    date,
  });

  const addDayCalendar = useCallback(
    (day: DayCalendarResponse) => {
      setDayCalendars((prevState) => [...prevState, day]);
      // inc();
      callUpdateDaysCallbacks();
      removeFromCache();
    },
    [callUpdateDaysCallbacks, removeFromCache],
  );
  const removeDayCalendar = useCallback(
    (dayId: number) => {
      setDayCalendars((prevState) => prevState.filter((d) => d.id !== dayId));
      // inc();
      callUpdateDaysCallbacks();
      removeFromCache();
    },
    [callUpdateDaysCallbacks, removeFromCache],
  );

  const changeForDate = useCallback(
    (day: DayCalendarResponse) => {
      setDayCalendars((prev) => prev.map((d) => (d.id === day.id ? day : d)));
      // inc();
      callUpdateDaysCallbacks();
      removeFromCache();
    },
    [callUpdateDaysCallbacks, removeFromCache],
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
        isAbsoluteFinished,
        messages,
        daysLocalChange,
        addToUpdateDaysCallbacks,
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
