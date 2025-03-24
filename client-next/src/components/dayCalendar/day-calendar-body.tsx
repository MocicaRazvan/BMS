"use client";

import DayCalendarBodyMonth, {
  DayCalendarBodyMonthTexts,
} from "@/components/dayCalendar/day-calendar-body-month";

interface DayCalendarBodyProps {
  dayCalendarBodyMonthTexts: DayCalendarBodyMonthTexts;
}

export default function DayCalendarBody({
  dayCalendarBodyMonthTexts,
}: DayCalendarBodyProps) {
  return <>{true && <DayCalendarBodyMonth {...dayCalendarBodyMonthTexts} />}</>;
}
