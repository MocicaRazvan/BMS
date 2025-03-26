"use client";

import { IntlMetadata } from "@/texts/metadata";
import { Role } from "@/types/fetch-utils";
import React from "react";
import useFetchStream from "@/hoooks/useFetchStream";
import { AdminDashboardPageTexts } from "@/app/[locale]/admin/dashboard/page-content";
import { WithUser } from "@/lib/user";
import { CustomEntityModel, DayResponse, PageableResponse } from "@/types/dto";
import { EditorTexts } from "@/components/editor/editor";
import DayCalendarProvider from "@/context/day-calendar-context";
import DayCalendarHeaderDate from "@/components/dayCalendar/day-calendar-header";
import DayCalendarBody from "@/components/dayCalendar/day-calendar-body";
import TopViewedPosts from "@/components/charts/top-viewed-posts";

interface Props extends WithUser {}
export default function TestPage({ authUser }: Props) {
  return (
    <div>
      {/*<TopViewedPosts path="/posts/admin/viewStats" authUser={authUser} />*/}
    </div>
  );
}
