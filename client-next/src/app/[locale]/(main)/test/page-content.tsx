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

interface Props extends WithUser {
  metadataValues: {
    metadata: IntlMetadata;
    key: string;
    path: string;
    role: Role | "ROLE_PUBLIC";
  }[];
  texts: AdminDashboardPageTexts;
  editorTexts: EditorTexts;
}
export default function TestPage({
  metadataValues,
  texts,
  authUser,
  editorTexts,
}: Props) {
  const { messages, isFinished, error } = useFetchStream<
    PageableResponse<CustomEntityModel<DayResponse>>
  >({
    path: "/orders/subscriptions/plans/days",
    method: "PATCH",
    authToken: true,
    body: {
      page: 0,
      size: 10,
    },
  });
  console.log("TestPageError", error);
  const days = messages.map((d) => d.content.content);
  return (
    <div>
      <div className="h-48" />
    </div>
  );
}
