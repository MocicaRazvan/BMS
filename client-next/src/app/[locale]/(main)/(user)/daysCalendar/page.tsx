import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUser } from "@/lib/user";
import {
  getDayCalendarBodyMonthTexts,
  getDayCalendarHeaderTexts,
} from "@/texts/components/day-calendar";
import DayCalendarHeaderDate from "@/components/dayCalendar/day-calendar-header";
import DayCalendarBody from "@/components/dayCalendar/day-calendar-body";
import DayCalendarProvider from "@/context/day-calendar-context";
import React, { Suspense } from "react";
import { Separator } from "@/components/ui/separator";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import LoadingSpinner from "@/components/common/loading-spinner";

export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.DaysCalendar", "/daysCalendar", locale)),
  };
}
interface Props {
  params: {
    locale: Locale;
  };
}
export default async function Page({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [authUser, dayCalendarHeaderTexts, dayCalendarBodyMonthTexts] =
    await Promise.all([
      getUser(),
      getDayCalendarHeaderTexts(),
      getDayCalendarBodyMonthTexts(),
    ]);
  return (
    <Suspense fallback={<LoadingSpinner />}>
      {/*<DayCalendarProvider authUser={authUser}>*/}
      <div className="py-2.5">
        <DayCalendarHeaderDate {...dayCalendarHeaderTexts} />
        <Separator className="mt-4" />
        <DayCalendarBody
          dayCalendarBodyMonthTexts={dayCalendarBodyMonthTexts}
        />
      </div>
      {/*</DayCalendarProvider>*/}
    </Suspense>
  );
}
