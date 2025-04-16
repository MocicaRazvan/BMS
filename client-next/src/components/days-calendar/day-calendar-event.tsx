"use client";

import { CustomEntityModel, DayCalendarResponse } from "@/types/dto";
import { useCallback, useEffect, useState } from "react";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import {
  getColorByDayType,
  useDayCalendar,
} from "@/context/day-calendar-context";
import { toast } from "@/components/ui/use-toast";
import { Button } from "@/components/ui/button";
import { Info, Trash2 } from "lucide-react";
import DayCalendarSingleDay, {
  DayCalendarSingleDayTexts,
} from "@/components/days-calendar/day-calendar-single-day";
import { Badge } from "@/components/ui/badge";

export interface DayCalendarEventTexts {
  toastDescription: string;
  dayCalendarSingleDayTexts: DayCalendarSingleDayTexts;
}

export default function DayCalendarEvent({
  dayCalendar,
  texts,
}: {
  dayCalendar: DayCalendarResponse;
  texts: DayCalendarEventTexts;
}) {
  const { authUser, removeDayCalendar } = useDayCalendar();
  const [isDeletePress, setIsDeletePress] = useState(false);
  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();
  const handleDelete = useCallback(async () => {
    setIsLoading(true);
    await fetchStream<CustomEntityModel<DayCalendarResponse>>({
      path: "/daysCalendar/delete/" + dayCalendar.id,
      method: "DELETE",
      token: authUser.token,
    })
      .then(({ messages, error }) => {
        if (error) {
          setErrorMsg("Error");
          console.log("Error", error);
        } else if (messages.length > 0) {
          removeDayCalendar(dayCalendar.id);
          toast({
            description: texts.toastDescription,
            variant: "default",
          });
        }
      })
      .catch((error) => {
        setErrorMsg("Error");
        console.log("Error", error);
      });
    setIsLoading(false);
  }, [
    authUser.token,
    dayCalendar.id,
    removeDayCalendar,
    setErrorMsg,
    setIsLoading,
    texts.toastDescription,
  ]);
  useEffect(() => {
    let timer: NodeJS.Timeout;
    if (isDeletePress) {
      timer = setTimeout(() => {
        setIsDeletePress(false);
      }, 5000);
    }
    return () => {
      clearTimeout(timer);
    };
  }, [isDeletePress]);
  return (
    <div
      className="w-full h-full rounded-xl border-2 p-2 flex flex-col justify-between"
      style={{
        backgroundColor: getColorByDayType(dayCalendar.dayResponse.type, 0.5),
        borderColor: getColorByDayType(dayCalendar.dayResponse.type),
      }}
    >
      <div className="flex flex-col justify-around h-full">
        <DayCalendarSingleDay
          dayCalendar={dayCalendar}
          texts={texts.dayCalendarSingleDayTexts}
          anchor={
            <p className="text-xl tracking-tight hover:underline cursor-pointer w-full truncate">
              {dayCalendar.dayResponse.title}
            </p>
          }
        />
        <div className="flex items-center justify-center">
          <Badge
            className="text-center bg-opacity-75"
            variant="accent"
            style={
              {
                // color: getColorByDayType(days-calendar.dayResponse.type),
              }
            }
          >
            {dayCalendar.dayResponse.type}
          </Badge>
        </div>
      </div>
      <div className="flex justify-end">
        {!isDeletePress ? (
          <Button
            size="icon"
            variant="outlineDestructive"
            className="bg-destructive/20 shadow text-destructive"
            onClick={() => setIsDeletePress(true)}
            disabled={isLoading}
          >
            <Trash2 />
          </Button>
        ) : (
          <Button
            onClick={handleDelete}
            size="icon"
            variant="outlineAmber"
            className="bg-amber/20 shadow text-amber"
            disabled={isLoading}
          >
            <Info />
          </Button>
        )}
      </div>
    </div>
  );
}
