"use client";

import useFetchStream from "@/hoooks/useFetchStream";
import { MonthlyOrderSummaryPrediction } from "@/types/dto";
import { useMemo, useState } from "react";
import { format } from "date-fns";
import useClientNotFound from "@/hoooks/useClientNotFound";

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import {
  CountTotalAmountRadioOptionsType,
  DropDownMenuCountTotalAmountSelect,
} from "@/components/charts/totalAmount-count-orders-inputs";
import {
  ColorIndexes,
  DataLabels,
  FormatedData,
} from "@/components/charts/prediction-chart-container";
import dynamic from "next/dynamic";
import { Skeleton } from "@/components/ui/skeleton";

export interface PredictionChartTexts extends DataLabels {
  title: string;
  disclaimer: string;
  predictionLengthLabel: string;
}
interface Props extends ColorIndexes {
  path: string;
  texts: PredictionChartTexts;
}

const DynamicPredictionChartContainer = dynamic(
  () =>
    import("@/components/charts/prediction-chart-container").then(
      (mod) => mod.PredictionChartContainer,
    ),
  {
    ssr: false,
    loading: () => (
      <div className="w-full py-16">
        <Skeleton className="aspect-auto h-[450px] w-full" />
      </div>
    ),
  },
);

export function PredictionChart({
  path,
  countColorIndex = 1,
  totalAmountColorIndex = 6,
  texts,
}: Props) {
  const { navigateToNotFound } = useClientNotFound();
  const [areaRadioOption, setAreaRadioOption] =
    useState<CountTotalAmountRadioOptionsType>("count");
  const [predictionLength, setPredictionLength] = useState(3);
  const { messages, error, isFinished } =
    useFetchStream<MonthlyOrderSummaryPrediction>({
      authToken: true,
      path,
      queryParams: {
        predictionLength: predictionLength.toString(),
      },
    });
  const formattedData: FormatedData[] = useMemo(
    () =>
      messages.map((m) => ({
        count: m.countQuantiles[1].toFixed(2),
        totalAmount: m.totalAmountQuantiles[1].toFixed(2),
        countArea: [
          m.countQuantiles[0].toFixed(2),
          m.countQuantiles[2].toFixed(2),
        ],
        totalAmountArea: [
          m.totalAmountQuantiles[0].toFixed(2),
          m.totalAmountQuantiles[2].toFixed(2),
        ],
        date: format(new Date(m.year, m.month - 1), "MM-yyyy"),
      })),
    [JSON.stringify(messages)],
  );
  const maxedQuantile = useMemo(
    () =>
      messages.reduce(
        (acc, cur) => {
          if (cur.countQuantiles[2] > acc.count) {
            acc.count = cur.countQuantiles[2];
          }
          if (cur.totalAmountQuantiles[2] > acc.totalAmount) {
            acc.totalAmount = cur.totalAmountQuantiles[2];
          }
          return acc;
        },
        {
          count: 0,
          totalAmount: 0,
        },
      ),
    [JSON.stringify(messages)],
  );
  if (error?.status) {
    return navigateToNotFound();
  }
  return (
    <div className="w-full h-ful space-y-10 pt-10 md:space-y-14">
      <div className="w-full flex flex-col sm:flex-row items-center justify-between gap-4 ">
        <div className="space-y-0.5 ">
          <h3 className="text-xl md:text-2xl font-bold tracking-tighter">
            {texts.title}
          </h3>
          <p className="text-sm text-amber font-normal">{texts.disclaimer}</p>
        </div>
        <div className=" flex items-center justify-end gap-4">
          <DropDownMenuCountTotalAmountSelect
            countLabel={texts.countLabel}
            totalAmountLabel={texts.totalAmountLabel}
            bothLabel={""}
            showBoth={false}
            onRadioOptionChange={setAreaRadioOption}
            radioOption={areaRadioOption}
          />
          <div className="flex items-center justify-center gap-1">
            <p className="text-sm font-medium">{texts.predictionLengthLabel}</p>
            <DropDownMenuPredictionLength
              onRadioOptionChange={setPredictionLength}
              radioOption={predictionLength}
            />
          </div>
        </div>
      </div>
      <DynamicPredictionChartContainer
        dataKey={
          areaRadioOption as Exclude<CountTotalAmountRadioOptionsType, "both">
        }
        countColorIndex={countColorIndex}
        totalAmountColorIndex={totalAmountColorIndex}
        {...texts}
        data={formattedData}
        dataAvailable={isFinished}
        chartName={texts.title}
        maxedQuantile={maxedQuantile}
      />
    </div>
  );
}

interface DropDownMenuPredictionLengthProps {
  onRadioOptionChange: (value: number) => void;
  radioOption: number;
}

const predictionLengths = [3, 6] as const;

function DropDownMenuPredictionLength({
  radioOption,
  onRadioOptionChange,
}: DropDownMenuPredictionLengthProps) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline">{radioOption}</Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="min-w-[5rem]">
        <DropdownMenuRadioGroup
          value={radioOption.toString()}
          onValueChange={(e) => onRadioOptionChange(parseInt(e))}
        >
          {predictionLengths.map((value) => (
            <DropdownMenuRadioItem key={value} value={value.toString()}>
              {value}
            </DropdownMenuRadioItem>
          ))}
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
