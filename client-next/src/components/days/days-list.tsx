"use client";
import SingleDay, { SingleDayProps } from "@/components/days/single-day";
import useGetDaysWithMeals from "@/hoooks/days/useGetDayWithMeals";
import LoadingSpinner from "@/components/common/loading-spinner";
import React, { memo, useEffect, useState } from "react";
import CustomPaginationButtons, {
  CustomDisplayProps,
} from "@/components/ui/custom-pagination-buttons";
import { AnimatePresence, motion } from "framer-motion";
import { cn } from "@/lib/utils";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Search } from "lucide-react";
import { AnswerFromBodyFormTexts } from "@/components/forms/answer-from-body-form";

export interface DaysListTexts {
  header: string;
  answerFromBodyFormTexts: AnswerFromBodyFormTexts;
}

export interface DaysListProps
  extends DaysListTexts,
    Omit<SingleDayItemProps, "dayId" | "answerFromBodyFormTexts"> {
  dayIds: number[];
}

const DaysList = ({ dayIds, header, ...rest }: DaysListProps) => {
  const [currentIndex, setCurrentIndex] = useState(0);
  return (
    <div className="w-full relative mb-2 lg:mb-3">
      <h3 className="text-3xl md:text-4xl tracking-tighter font-bold text-center mb-5">
        {header}
      </h3>
      <div className="mb-5">
        <CustomPaginationButtons
          items={dayIds}
          currentIndex={currentIndex}
          setCurrentIndex={setCurrentIndex}
          CustomDisplay={SelectDisplay}
        />
      </div>
      <div className="relative">
        <AnimatePresence mode="wait">
          {dayIds.map((dayId, index) => (
            <motion.div
              id={`daysList-${index}`}
              key={dayId + "-" + index}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: index === currentIndex ? 1 : 0 }}
              exit={{ opacity: 0, y: -20 }}
              transition={{ duration: 0.3 }}
              className={cn(index !== currentIndex ? "hidden " : "block ")}
            >
              <SingleDayItem dayId={dayId} {...rest} />
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
      <div className="mt-10">
        <CustomPaginationButtons
          items={dayIds}
          currentIndex={currentIndex}
          setCurrentIndex={setCurrentIndex}
          CustomDisplay={SelectDisplay}
        />
      </div>
    </div>
  );
};
DaysList.displayName = "DaysList";

export default DaysList;

interface SingleDayItemProps
  extends Omit<SingleDayProps, "day" | "author" | "meals"> {
  dayId: number;
  dayBasePath?: string;
  mealsBasePath?: string;
}

const SingleDayItem = memo(
  ({
    dayId,
    dayBasePath,
    mealsBasePath,
    authUser,
    ...rest
  }: SingleDayItemProps) => {
    const { dayState, dayIsFinished, mealsIsFinished, user, meals } =
      useGetDaysWithMeals({
        dayId,
        authUser,
        dayBasePath,
        mealsBasePath,
      });

    if (!mealsIsFinished || !dayIsFinished) {
      return <LoadingSpinner />;
    }

    if (!dayState || !meals) return null;
    return (
      <SingleDay
        day={dayState}
        author={user}
        meals={meals}
        authUser={authUser}
        hideAuthor
        {...rest}
      />
    );
  },
);
SingleDayItem.displayName = "SingleDayItem";

const SelectDisplay = ({
  currentIndex,
  items,
  setCurrentIndex,
}: CustomDisplayProps) => {
  const [inputValue, setInputValue] = useState<number>(currentIndex + 1);
  const [popOpen, setPopOpen] = useState(false);

  const handleSearch = () => {
    let newIndex = inputValue - 1;

    if (isNaN(newIndex) || newIndex < 0) {
      newIndex = 0;
    } else if (newIndex >= items.length) {
      newIndex = items.length - 1;
    }

    setCurrentIndex(newIndex);
    setInputValue(newIndex + 1);
  };

  useEffect(() => {
    if (!popOpen && currentIndex !== inputValue - 1) {
      setInputValue(currentIndex + 1);
    }
  }, [currentIndex, inputValue, popOpen]);

  return (
    <Popover open={popOpen} onOpenChange={setPopOpen}>
      <PopoverTrigger asChild>
        <Button variant="outline">{`${currentIndex + 1} / ${
          items.length
        }`}</Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto p-0 mt-2 bg-transparent">
        <div className="flex items-center p-2 w-full justify-between gap-2 bg-transparent">
          <Input
            className={
              "max-w-[100px] outline-none focus:outline-none focus-visible:ring-transparent focus-visible:ring-0 focus-visible:outline-none"
            }
            type="number"
            value={inputValue}
            onChange={(e) => {
              const value = parseInt(e.target.value, 10);
              setInputValue(value);
            }}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                handleSearch();
              }
            }}
          />
          <Button size={"icon"} onClick={handleSearch}>
            <Search />
          </Button>
        </div>
      </PopoverContent>
    </Popover>
  );
};
