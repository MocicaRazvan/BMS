"use client";

import * as React from "react";
import { useEffect, useState } from "react";
import { format, startOfMonth, subMonths } from "date-fns";
import { CalendarIcon } from "lucide-react";
import type { DateRange } from "react-day-picker";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

export interface MonthPickerTexts {
  placeholder: string;
}

interface Props {
  onDateChange?: (date: string) => void;
  defaultDate?: Date;
}

export function MonthPicker({ onDateChange, defaultDate = new Date() }: Props) {
  const [texts, setTexts] = useState<MonthPickerTexts>({
    placeholder: "",
  });
  const [date, setDate] = useState<DateRange | undefined>({
    from: startOfMonth(defaultDate),
    to: startOfMonth(defaultDate),
  });

  // useEffect(() => {
  //   getMonthPickerTexts().then(setTexts);
  // }, []);

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          variant={"outline"}
          className={cn(
            "w-[125px] justify-between text-left font-normal",
            !date && "text-muted-foreground",
          )}
        >
          <CalendarIcon className="mr-2 h-4 w-4" />
          {date?.from ? (
            format(date.from, "MM-yyyy")
          ) : (
            <span>{texts.placeholder}</span>
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto p-0" align="start">
        <Calendar
          mode="single"
          selected={date?.from}
          onSelect={(newDate) => {
            setDate(
              newDate
                ? { from: startOfMonth(newDate), to: startOfMonth(newDate) }
                : undefined,
            );
            if (newDate && onDateChange) {
              onDateChange(format(newDate, "dd-MM-yyyy"));
            }
          }}
          showOutsideDays={false}
          fixedWeeks
          ISOWeek
          defaultMonth={date?.from}
          disabled={(d) => d.getDate() !== 1}
          modifiersStyles={{ disabled: { opacity: 0.5 } }}
          fromDate={new Date(1970, 0, 1)}
          toDate={new Date()}
        />
      </PopoverContent>
    </Popover>
  );
}

export function MonthPickerSelect({
  onDateChange,
  defaultDate = new Date(),
}: Props) {
  const [selectedDate, setSelectedDate] = useState<Date>(
    startOfMonth(defaultDate),
  );
  const [monthOptions, setMonthOptions] = useState<Date[]>([]);

  useEffect(() => {
    const options: Date[] = [];
    const currentDate = new Date();
    for (let i = 0; i < 24; i++) {
      options.push(startOfMonth(subMonths(currentDate, i)));
    }
    setMonthOptions(options);
  }, []);

  const handleSelectChange = (value: string) => {
    setSelectedDate(new Date(value));
    if (onDateChange) {
      onDateChange(format(value, "dd-MM-yyyy"));
    }
  };

  return (
    <Select
      onValueChange={handleSelectChange}
      value={selectedDate.toISOString()}
    >
      <SelectTrigger className="w-[120px]">
        <SelectValue>{format(selectedDate, "MM-yyyy")}</SelectValue>
      </SelectTrigger>
      <SelectContent>
        {monthOptions.map((date) => (
          <SelectItem key={date.toISOString()} value={date.toISOString()}>
            {format(date, "MM-yyyy")}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
