"use client";

import { useDayCalendar } from "@/context/day-calendar-context";
import { addMonths, format, isSameMonth, subMonths } from "date-fns";
import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useLocale } from "next-intl";
import { Locale } from "@/navigation/navigation";
import { dateFnsLocaleMapper } from "@/lib/utils";
import { getColorsByDayType } from "@/types/constants";
import { motion } from "framer-motion";
import { useCallback, useEffect, useMemo, useRef } from "react";
import { debounce } from "lodash-es";

export interface DayCalendarHeaderTexts {
  tracked: string;
}

export default function DayCalendarHeaderDate({
  tracked,
}: DayCalendarHeaderTexts) {
  const { date, dayCalendars, setDate, isFinished } = useDayCalendar();
  const locale = useLocale() as Locale;
  const monthDays = useMemo(
    () => dayCalendars.filter((d) => isSameMonth(d.date, date)),
    [dayCalendars, date],
  );
  const deltaRef = useRef(0);

  const applyDelta = useRef(
    debounce(() => {
      setDate((prev) => addMonths(prev, deltaRef.current));
      deltaRef.current = 0;
    }, 150),
  );
  useEffect(() => {
    const cancel = applyDelta.current.cancel;
    return () => {
      cancel();
    };
  }, []);

  return useMemo(() => {
    function handleForward() {
      deltaRef.current += 1;
      applyDelta.current();
    }

    function handleBackward() {
      deltaRef.current -= 1;
      applyDelta.current();
    }
    return (
      <div className="w-full flex flex-col md:flex-row gap-10 items-center justify-between px-2.5">
        <div className="flex items-center gap-2">
          <div className="flex size-14 flex-col items-start overflow-hidden rounded-lg border">
            <p className="capitalize flex h-6 w-full items-center justify-center bg-primary text-center text-xs font-semibold text-background ">
              {format(date, "MMM", {
                locale: dateFnsLocaleMapper?.[locale],
              })}
            </p>
            <p className="flex w-full items-center justify-center text-lg font-bold">
              {format(date, "dd", {
                locale: dateFnsLocaleMapper?.[locale],
              })}
            </p>
          </div>
          <div>
            <div className="flex items-center gap-1">
              <p className="text-lg font-semibold capitalize">
                {format(date, "MMMM yyyy", {
                  locale: dateFnsLocaleMapper?.[locale],
                })}
              </p>
              {isFinished && (
                <motion.div
                  className="whitespace-nowrap rounded-sm border px-1.5 py-0.5 text-xs"
                  key={monthDays.length + date.toISOString()}
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                  transition={{
                    delay: 0.05,
                    duration: 0.35,
                    ease: "easeInOut",
                  }}
                >
                  {monthDays.length} {tracked}
                </motion.div>
              )}
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                className="h-7 w-7 p-1"
                onClick={handleBackward}
              >
                <ChevronLeft className="min-w-5 min-h-5" />
              </Button>

              <span className="min-w-[140px] text-center font-medium">
                {format(date, "MMMM d, yyyy", {
                  locale: dateFnsLocaleMapper?.[locale],
                })}
              </span>

              <Button
                variant="outline"
                className="h-7 w-7 p-1"
                onClick={handleForward}
              >
                <ChevronRight className="min-w-5 min-h-5" />
              </Button>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-3 md:grid-cols-4 place-items-center gap-2 rounded-xl backdrop-blur-sm bg-background/40">
          {Object.entries(getColorsByDayType()).map(([t, c]) => (
            <div key={t} className="flex items-center justify-start gap-1 w-32">
              <div
                className={`h-2.5 w-2.5 rounded-full`}
                style={{
                  backgroundColor: `${c}`,
                }}
              />
              <p className="text-sm text-muted-foreground">
                {t.replace("_", " ")}
              </p>
            </div>
          ))}
        </div>
      </div>
    );
  }, [date, isFinished, locale, monthDays.length, setDate, tracked]);
}
