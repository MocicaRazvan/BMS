import { Locale } from "@/navigation";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import { getMetadataValues } from "@/texts/metadata";
import { getAdminDashboardPageTexts } from "@/texts/pages";
import { getUser } from "@/lib/user";
import { getEditorTexts } from "@/texts/components/editor";
import DayCalendarProvider from "@/context/day-calendar-context";
import DayCalendarHeaderDate from "@/components/days-calendar/day-calendar-header";
import DayCalendarBody from "@/components/days-calendar/day-calendar-body";
import React from "react";
import {
  getDayCalendarBodyMonthTexts,
  getDayCalendarHeaderTexts,
} from "@/texts/components/day-calendar";

import TestPageContent from "./page-content";
import { ArchiveQueuesTableTexts } from "@/components/table/archive-queues-table";
import { getArchiveQueuesTableTexts } from "@/texts/components/table";

interface Props {
  params: {
    locale: Locale;
  };
}

export default async function TestPage({ params }: Props) {
  const session = await getServerSession(authOptions);
  const texts = await getArchiveQueuesTableTexts();
  const authUser = await getUser();

  return <TestPageContent authUser={authUser} texts={texts} />;
}
