"use client";
import { ReactNode, useState } from "react";
import { DayCalendarResponse } from "@/types/dto";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { SingleDayTexts } from "@/components/days/single-day";
import { useDayCalendar } from "@/context/day-calendar-context";
import dynamicWithPreload from "@/lib/dynamic-with-preload";
import { Skeleton } from "@/components/ui/skeleton";
import usePreloadDynamicComponents from "@/hoooks/use-prelod-dynamic-components";

const DynamicSingleDay = dynamicWithPreload(
  () => import("@/components/days/single-day"),
  {
    loading: () => <Skeleton className="w-full min-h-[95vh]" />,
  },
);

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

  usePreloadDynamicComponents(DynamicSingleDay);

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{anchor}</DialogTrigger>
      <DialogContent className="w-full md:min-w-[95vw] max-h-[95vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="sr-only">{texts.title}</DialogTitle>
        </DialogHeader>
        <div>
          <DynamicSingleDay
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
