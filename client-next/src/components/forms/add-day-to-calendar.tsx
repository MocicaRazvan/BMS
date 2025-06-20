"use client";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import {
  AddDayToCalendarSchemaTexts,
  AddDayToCalendarSchemaType,
  getAddDayToCalendarSchema,
} from "@/types/forms";
import React, { useCallback, useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  CustomEntityModel,
  DayCalendarBody,
  DayCalendarResponse,
  DayResponse,
} from "@/types/dto";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { cn } from "@/lib/utils";
import { addYears, endOfYear, format, startOfYear, subYears } from "date-fns";
import { CalendarIcon, Notebook } from "lucide-react";
import { Calendar } from "@/components/ui/calendar";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { toast } from "@/components/ui/use-toast";
import { WithUser } from "@/lib/user";
import ErrorMessage from "@/components/forms/error-message";
import { useDayCalendar } from "@/context/day-calendar-context";

export interface AddDayToCalendarTexts {
  addDayToCalendarSchemaTexts: AddDayToCalendarSchemaTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  description: string;
  errorText: string;
  toastDescription: string;
  anchorText: string;
  titleText: string;
  formLabel: string;
  formDescription: string;
  formPlaceholder: string;
}
export interface AddDayToCalendarProps extends AddDayToCalendarTexts, WithUser {
  day: DayResponse;
  forbiddenDates: string[];
  onAddDayToCalendar: () => void;
}
export default function AddDayToCalendar({
  addDayToCalendarSchemaTexts,
  day,
  buttonSubmitTexts,
  errorText,
  authUser,
  forbiddenDates,
  onAddDayToCalendar,
  toastDescription,
  anchorText,
  titleText,
  formDescription,
  formLabel,
  formPlaceholder,
  description,
}: AddDayToCalendarProps) {
  const { refetch: dayCalendarRefetch } = useDayCalendar();
  const schema = useMemo(
    () => getAddDayToCalendarSchema(addDayToCalendarSchemaTexts),
    [addDayToCalendarSchemaTexts],
  );
  const [open, setOpen] = useState(false);

  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();

  const form = useForm<AddDayToCalendarSchemaType>({
    resolver: zodResolver(schema),
    defaultValues: {
      date: undefined,
    },
  });

  const onSubmit = useCallback(
    async (data: AddDayToCalendarSchemaType) => {
      setIsLoading(true);
      setErrorMsg("");
      const body: DayCalendarBody = {
        dayId: day.id,
        date: format(data.date, "yyyy-MM-dd"),
      };

      await fetchStream<CustomEntityModel<DayCalendarResponse>>({
        path: "/daysCalendar/create",
        method: "POST",
        token: authUser.token,
        body,
      })
        .then(({ messages, error }) => {
          if (error) {
            setErrorMsg(errorText);
            console.log("Error", error);
          } else if (messages.length > 0) {
            dayCalendarRefetch();
            toast({
              description: toastDescription,
              variant: "default",
            });
            onAddDayToCalendar();
            router.push(
              "/daysCalendar?date=" + format(data.date, "yyyy-MM-dd"),
            );
          }
        })
        .catch((e) => {
          console.log("Error", e);
          setErrorMsg(errorText);
        })
        .finally(() => {
          setIsLoading(false);
          setOpen(false);
        });
    },
    [
      authUser.token,
      day.id,
      dayCalendarRefetch,
      errorText,
      onAddDayToCalendar,
      router,
      setErrorMsg,
      setIsLoading,
      toastDescription,
    ],
  );
  const today = new Date();
  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button
          variant="outlineSuccess"
          className="flex items-center justify-between gap-3"
        >
          <p>{anchorText}</p>
          <Notebook size={20} />
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[500px] p-10">
        <DialogHeader>
          <DialogTitle>{titleText}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-8 lg:space-y-12"
            noValidate
          >
            <FormField
              control={form.control}
              name="date"
              render={({ field }) => (
                <FormItem className="flex flex-col">
                  <FormLabel>{formLabel}</FormLabel>
                  <Popover>
                    <PopoverTrigger asChild>
                      <FormControl>
                        <Button
                          variant="outline"
                          className={cn(
                            "w-[275px] pl-3 text-left font-normal",
                            !field.value && "text-muted-foreground",
                          )}
                        >
                          {field.value ? (
                            format(field.value, "yyyy-MM-dd")
                          ) : (
                            <span>{formPlaceholder}</span>
                          )}
                          <CalendarIcon className="ml-auto h-4 w-4 opacity-50" />
                        </Button>
                      </FormControl>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0" align="start">
                      <Calendar
                        mode="single"
                        selected={field.value}
                        onSelect={field.onChange}
                        disabled={(date) =>
                          date > startOfYear(addYears(today, 3)) ||
                          date < endOfYear(subYears(today, 3)) ||
                          forbiddenDates.includes(format(date, "yyyy-MM-dd"))
                        }
                        initialFocus
                      />
                    </PopoverContent>
                  </Popover>
                  <FormDescription>{formDescription}</FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="flex items-center justify-center">
              <ErrorMessage message={errorMsg} show={!!errorMsg} />
              <ButtonSubmit
                isLoading={isLoading}
                disable={isLoading}
                size={"sm"}
                buttonSubmitTexts={buttonSubmitTexts}
              />
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
