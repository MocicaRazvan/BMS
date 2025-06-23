import { LocaleProps } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import {
  getDayCalendarBodyMonthTexts,
  getDayCalendarHeaderTexts,
} from "@/texts/components/day-calendar";
import DayCalendarHeaderDate from "@/components/days-calendar/day-calendar-header";
import DayCalendarBody from "@/components/days-calendar/day-calendar-body";
import { Separator } from "@/components/ui/separator";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.DaysCalendar", "/daysCalendar", locale)),
  };
}

export default async function Page({ params: { locale } }: LocaleProps) {
  unstable_setRequestLocale(locale);
  const [dayCalendarHeaderTexts, dayCalendarBodyMonthTexts] = await Promise.all(
    [getDayCalendarHeaderTexts(), getDayCalendarBodyMonthTexts()],
  );
  return (
    <div className="py-2.5 mt-2">
      <DayCalendarHeaderDate {...dayCalendarHeaderTexts} />
      <Separator className="mt-6" />
      <DayCalendarBody dayCalendarBodyMonthTexts={dayCalendarBodyMonthTexts} />
      <DayCalendarHeaderDate {...dayCalendarHeaderTexts} />
    </div>
  );
}
