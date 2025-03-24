import { Locale } from "@/navigation";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import { getMetadataValues } from "@/texts/metadata";
import { getAdminDashboardPageTexts } from "@/texts/pages";
import { getUser } from "@/lib/user";
import { getEditorTexts } from "@/texts/components/editor";
import DayCalendarProvider from "@/context/day-calendar-context";
import DayCalendarHeaderDate from "@/components/dayCalendar/day-calendar-header";
import DayCalendarBody from "@/components/dayCalendar/day-calendar-body";
import React from "react";
import {
  getDayCalendarBodyMonthTexts,
  getDayCalendarHeaderTexts,
} from "@/texts/components/day-calendar";

interface Props {
  params: {
    locale: Locale;
  };
}

export default async function TestPage({ params }: Props) {
  const session = await getServerSession(authOptions);
  const metadataValues = await getMetadataValues(session?.user, params.locale);
  const texts = await getAdminDashboardPageTexts();
  const authUser = await getUser();
  const editorTexts = await getEditorTexts();
  const [dayCalendarHeaderTexts, dayCalendarBodyMonthTexts] = await Promise.all(
    [getDayCalendarHeaderTexts(), getDayCalendarBodyMonthTexts()],
  );
  return (
    // <TestPageContent
    //   metadataValues={metadataValues}
    //   texts={texts}
    //   authUser={authUser}
    //   editorTexts={editorTexts}
    // />
    <DayCalendarProvider authUser={authUser}>
      <DayCalendarHeaderDate {...dayCalendarHeaderTexts} />
      <DayCalendarBody dayCalendarBodyMonthTexts={dayCalendarBodyMonthTexts} />
    </DayCalendarProvider>
  );
}
