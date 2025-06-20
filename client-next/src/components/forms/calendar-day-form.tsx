"use client";

import { Option } from "@/components/ui/multiple-selector";
import React, { ReactNode, useCallback, useState } from "react";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import {
  CustomEntityModel,
  DayCalendarBody,
  DayCalendarResponse,
  DayResponse,
  PageableResponse,
} from "@/types/dto";
import { format } from "date-fns";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { useDayCalendar } from "@/context/day-calendar-context";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import ChildInputMultipleSelector, {
  ChildInputMultipleSelectorTexts,
} from "@/components/forms/child-input-multipleselector";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import { toast } from "@/components/ui/use-toast";
import ErrorMessage from "@/components/forms/error-message";

export interface CalendarDayFormTexts {
  title: string;
  buttonSubmitTexts: ButtonSubmitTexts;
  placeholder: string;
  error: string;
  childInputMultipleSelectorTexts: ChildInputMultipleSelectorTexts;
  toastDescription: string;
  description: string;
}

interface Props {
  date: Date;
  anchor: ReactNode;
  texts: CalendarDayFormTexts;
}
export default function CalendarDayForm({ date, anchor, texts }: Props) {
  const { addDayCalendar, authUser } = useDayCalendar();
  const [selectedOption, setSelectedOption] = useState<Option | null>(null);
  const [open, setOpen] = useState(false);
  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();
  const handleSubmit = useCallback(
    async (option: Option | null) => {
      if (!option || !option?.value) return;
      setIsLoading(true);
      setErrorMsg("");
      const body: DayCalendarBody = {
        dayId: parseInt(option?.value),
        date: format(date, "yyyy-MM-dd"),
      };
      await fetchStream<CustomEntityModel<DayCalendarResponse>>({
        path: "/daysCalendar/create",
        method: "POST",
        token: authUser.token,
        body,
      })
        .then(({ messages, error }) => {
          if (error) {
            setErrorMsg(texts.error);
            console.log("Error", error);
          } else if (messages.length > 0) {
            addDayCalendar(messages[0].content);
            toast({
              description: texts.toastDescription,
              variant: "default",
            });
          }
        })
        .catch((e) => {
          console.log("Error", e);
          setErrorMsg(texts.error);
        })
        .finally(() => {
          setIsLoading(false);
          setOpen(false);
        });
    },
    [
      addDayCalendar,
      authUser.token,
      date,
      setErrorMsg,
      setIsLoading,
      texts.error,
      texts.toastDescription,
    ],
  );

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{anchor}</DialogTrigger>
      <DialogContent className="sm:max-w-[500px] p-10">
        <DialogHeader>
          <DialogTitle>{texts.title}</DialogTitle>
          <DialogDescription>{texts.description}</DialogDescription>
        </DialogHeader>
        <ChildInputMultipleSelector<
          PageableResponse<CustomEntityModel<DayResponse>>
        >
          disabled={false}
          allowDuplicates={true}
          closeOnSelect={true}
          path={`/orders/subscriptions/plans/days`}
          // sortingCriteria={{ title: "asc" }}
          // extraQueryParams={{ approved: "true" }}
          pageSize={20}
          valueKey={"title"}
          mapping={(r) => ({
            value: r.content.content.id.toString(),
            label: r.content.content.title,
            // type: r.content.content.type,
          })}
          giveUnselectedValue={false}
          onChange={(options) => {
            setSelectedOption(options[0]);
          }}
          authUser={authUser}
          {...texts.childInputMultipleSelectorTexts}
          placeholder={texts.placeholder}
          maxSelected={1}
        />
        <div className="flex items-center justify-center">
          <ErrorMessage message={errorMsg} show={!!errorMsg} />
          <ButtonSubmit
            isLoading={isLoading}
            disable={isLoading || !selectedOption || !selectedOption.value}
            size={"sm"}
            buttonSubmitTexts={texts.buttonSubmitTexts}
            onClick={() => handleSubmit(selectedOption)}
          />
        </div>
      </DialogContent>
    </Dialog>
  );
}
