"use client";
import React, { ReactNode, useState } from "react";
import { DayCalendarResponse } from "@/types/dto";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import SingleDay, { SingleDayTexts } from "@/components/days/single-day";
import { useDayCalendar } from "@/context/day-calendar-context";

export interface DayCalendarSingleDayTexts {
  singleDayTexts: SingleDayTexts;
  title: string;
}

interface Props {
  dayCalendar: DayCalendarResponse;
  anchor: ReactNode;
  texts: DayCalendarSingleDayTexts;
}

export default function DayCalendarSingleDay({
  dayCalendar,
  anchor,
  texts,
}: Props) {
  const { authUser, refetch } = useDayCalendar();
  const [open, setOpen] = useState(false);
  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{anchor}</DialogTrigger>
      <DialogContent
        className="w-full md:min-w-[95vw]
       max-h-[95vh]
       overflow-y-auto"
      >
        <DialogHeader>
          <DialogTitle className="sr-only">{texts.title}</DialogTitle>
        </DialogHeader>
        <div>
          <SingleDay
            authUser={authUser}
            day={dayCalendar.dayResponse}
            author={dayCalendar.author}
            texts={texts.singleDayTexts}
            meals={dayCalendar.mealResponses}
            disableLikes={false}
            onReactCallback={refetch}
            recipeBasePath={`/daysCalendar/recipe/${dayCalendar.id}`}
          />
        </div>
      </DialogContent>
    </Dialog>
  );
}
