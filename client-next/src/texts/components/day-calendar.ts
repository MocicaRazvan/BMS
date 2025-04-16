"use server";
import { DayCalendarHeaderTexts } from "@/components/days-calendar/day-calendar-header";
import { getTranslations } from "next-intl/server";
import { DayCalendarBodyMonthTexts } from "@/components/days-calendar/day-calendar-body-month";
import { getCalendarDayFormTexts } from "@/texts/components/forms";
import { DayCalendarEventTexts } from "@/components/days-calendar/day-calendar-event";
import { DayCalendarSingleDayTexts } from "@/components/days-calendar/day-calendar-single-day";
import { getSingleDayTexts } from "@/texts/components/days";
import { DaysCalendarCTATexts } from "@/components/days-calendar/days-calendar-cta";

export async function getDayCalendarHeaderTexts(): Promise<DayCalendarHeaderTexts> {
  const [t] = await Promise.all([
    getTranslations("components.daysCalendar.DayCalendarHeaderTexts"),
  ]);
  return {
    tracked: t("tracked"),
  };
}
export async function getDayCalendarEventTexts(): Promise<DayCalendarEventTexts> {
  const [t, dayCalendarSingleDayTexts] = await Promise.all([
    getTranslations("components.daysCalendar.DayCalendarEventTexts"),
    getDayCalendarSingleDayTexts(),
  ]);
  return {
    toastDescription: t("toastDescription"),
    dayCalendarSingleDayTexts,
  };
}

export async function getDayCalendarSingleDayTexts(): Promise<DayCalendarSingleDayTexts> {
  const [t, singleDayTexts] = await Promise.all([
    getTranslations("components.daysCalendar.DayCalendarSingleDayTexts"),
    getSingleDayTexts(),
  ]);
  return {
    singleDayTexts,
    title: t("title"),
  };
}

export async function getDayCalendarBodyMonthTexts(): Promise<DayCalendarBodyMonthTexts> {
  const [t, calendarDayFormTexts, dayCalendarEventTexts] = await Promise.all([
    getTranslations("components.daysCalendar.DayCalendarBodyMonthTexts"),
    getCalendarDayFormTexts(),
    getDayCalendarEventTexts(),
  ]);
  return {
    addAnchor: t("addAnchor"),
    calendarDayFormTexts,
    dayCalendarEventTexts,
  };
}

export async function getDaysCalendarCTATexts(): Promise<DaysCalendarCTATexts> {
  const t = await getTranslations(
    "components.daysCalendar.DaysCalendarCTATexts",
  );

  return {
    header: t("header"),
  };
}
