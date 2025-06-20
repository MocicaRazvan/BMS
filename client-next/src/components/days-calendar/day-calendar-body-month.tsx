"use client";

import { useDayCalendar } from "@/context/day-calendar-context";
import {
  eachDayOfInterval,
  endOfWeek,
  format,
  isSameDay,
  isSameMonth,
  startOfWeek,
} from "date-fns";
import { AnimatePresence, motion } from "framer-motion";
import { cn, dateFnsLocaleMapper } from "@/lib/utils";
import DayCalendarEvent, {
  DayCalendarEventTexts,
} from "@/components/days-calendar/day-calendar-event";
import CalendarDayForm, {
  CalendarDayFormTexts,
} from "@/components/forms/calendar-day-form";
import { Button } from "@/components/ui/button";
import { Locale } from "@/navigation/navigation";
import { useLocale } from "next-intl";
import DayCalendarStatsWrapper, {
  DayCalendarStatsWrapperTexts,
} from "@/components/days-calendar/days-calendar-stats-wrapper";

export interface DayCalendarBodyMonthTexts {
  addAnchor: string;
  calendarDayFormTexts: CalendarDayFormTexts;
  dayCalendarEventTexts: DayCalendarEventTexts;
  wrapperChartTexts: DayCalendarStatsWrapperTexts;
}
const getLocalizedWeekdays = (appLocale: Locale) => {
  const locale = dateFnsLocaleMapper?.[appLocale];
  const weekStart = startOfWeek(new Date(), { locale });
  const weekEnd = endOfWeek(new Date(), { locale });

  const days = eachDayOfInterval({ start: weekStart, end: weekEnd });

  return days.map((day) => format(day, "EEE", { locale }));
};
export default function DayCalendarBodyMonth({
  addAnchor,
  calendarDayFormTexts,
  dayCalendarEventTexts,
  wrapperChartTexts,
}: DayCalendarBodyMonthTexts) {
  const locale = useLocale() as Locale;
  const { date, dayCalendars, calendarDays, monthStart } = useDayCalendar();

  const today = new Date();

  return (
    <div className="flex flex-col flex-grow overflow-hidden">
      <div className="hidden md:grid grid-cols-7 border-border divide-x divide-border">
        {getLocalizedWeekdays(locale).map((day) => (
          <div
            key={day}
            className="py-2 capitalize text-center text-sm font-medium text-muted-foreground border-b border-border"
          >
            {day}
          </div>
        ))}
      </div>

      <AnimatePresence initial={false}>
        <motion.div
          key={monthStart.toISOString()}
          className="grid md:grid-cols-7 flex-grow overflow-y-auto relative"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{
            duration: 0.2,
            ease: "easeInOut",
          }}
        >
          {calendarDays.map((day) => {
            const dayEvents = dayCalendars.filter((event) =>
              isSameDay(event.date, day),
            );
            const isToday = isSameDay(day, today);
            const isCurrentMonth = isSameMonth(day, date);

            return (
              <div
                key={day.toISOString()}
                className={cn(
                  "relative flex flex-col border-b border-r p-2 aspect-square ",
                  !isCurrentMonth && "bg-muted/50 hidden md:flex",
                )}
                // onClick={(e) => {
                //   e.stopPropagation();
                //   setDate(day);
                //   setMode("day");
                // }}
              >
                <div
                  className={cn(
                    "text-sm font-medium w-fit p-0.5 mb-0.5 flex flex-col items-center justify-center rounded-full aspect-square",
                    isToday && "bg-primary text-background",
                  )}
                >
                  {format(day, "d")}
                </div>
                <AnimatePresence mode="wait">
                  {dayEvents.length > 0 ? (
                    <div className="flex items-center justify-center flex-1">
                      {dayEvents.map((d) => (
                        <motion.div
                          key={d.id + d.date}
                          className="w-full h-full"
                          initial={{ opacity: 0.1 }}
                          animate={{ opacity: 1 }}
                          transition={{ duration: 0.25, ease: "easeInOut" }}
                        >
                          <DayCalendarEvent
                            dayCalendar={d}
                            texts={dayCalendarEventTexts}
                          />
                        </motion.div>
                      ))}
                    </div>
                  ) : (
                    <div className="flex items-center justify-center flex-1 ">
                      <CalendarDayForm
                        date={day}
                        anchor={<Button variant="outline">{addAnchor}</Button>}
                        texts={calendarDayFormTexts}
                      />
                    </div>
                  )}
                </AnimatePresence>
              </div>
            );
          })}
        </motion.div>
      </AnimatePresence>
      <div className="mt-14 md:mt-20 px-2 md:px-5">
        <DayCalendarStatsWrapper texts={wrapperChartTexts} />
      </div>
    </div>
  );
}
