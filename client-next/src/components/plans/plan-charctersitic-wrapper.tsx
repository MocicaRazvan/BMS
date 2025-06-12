"use client";

import { ChartConfig } from "@/components/ui/chart";
import {
  AverageAmount,
  DietType,
  MonthlyOrderSummary,
  MonthlyOrderSummaryObjective,
  MonthlyOrderSummaryObjectiveType,
  MonthlyOrderSummaryType,
  ObjectiveType,
} from "@/types/dto";
import { DateString } from "@/hoooks/charts/download-chart-button";
import { useMemo, useState } from "react";

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { format, subMonths } from "date-fns";
import useFetchStream from "@/hoooks/useFetchStream";
import { MonthPickerSelect } from "@/components/common/month-picker";
import { Separator } from "@/components/ui/separator";

import dynamic from "next/dynamic";
import { PlanCharacteristicTexts } from "@/components/charts/plan-characteristic";
import { Skeleton } from "@/components/ui/skeleton";

const DynamicPlanCharacteristic = dynamic(
  () =>
    import("@/components/charts/plan-characteristic").then(
      (mod) => mod.PlanCharacteristic,
    ),
  {
    ssr: false,
    loading: () => (
      <div className="w-full h-full">
        <Skeleton className="aspect-auto h-[450px] w-full" />
      </div>
    ),
  },
);

const DynamicPlanCharacteristicScatter = dynamic(
  () =>
    import("@/components/charts/plan-characteristic").then(
      (mod) => mod.PlanCharacteristicScatter,
    ),
  {
    ssr: false,
    loading: () => (
      <div className="w-full h-full">
        <Skeleton className="aspect-auto h-[450px] w-full" />
      </div>
    ),
  },
);

export type PlanCharacteristicKey = "count" | "totalAmount" | "averageAmount";
export type PlanCharacteristicOption = "type" | "objective";

type PlanCharacteristicData =
  | (MonthlyOrderSummary &
      AverageAmount & { objective: ObjectiveType } & DateString)
  | (MonthlyOrderSummary &
      AverageAmount & {
        type: DietType;
      } & DateString);
export interface PlanCharacteristicColors {
  countColorIndex?: number;
  totalAmountColorIndex?: number;
  lineColorIndex?: number;
  averageAmountColorIndex?: number;
}
interface Props extends PlanCharacteristicTexts, PlanCharacteristicColors {
  dataKey: PlanCharacteristicKey;
  characteristic: PlanCharacteristicOption;
  extraChartConfig?: ChartConfig;
  dataAvailable: boolean;
  data: PlanCharacteristicData[];
  chartName: string;
}

export interface DropDownMenuCountTotalPlanCharacteristicProps
  extends PlanCharacteristicTexts {
  radioOption: PlanCharacteristicKey;
  onRadioOptionChange: (option: PlanCharacteristicKey) => void;
}

export function DropDownMenuCountTotalPlanCharacteristicSelect({
  countLabel,
  totalAmountLabel,
  radioOption,
  onRadioOptionChange,
  averageAmountLabel,
}: DropDownMenuCountTotalPlanCharacteristicProps) {
  const label =
    radioOption === "count"
      ? countLabel
      : radioOption === "totalAmount"
        ? totalAmountLabel
        : averageAmountLabel;

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline">{label}</Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56">
        <DropdownMenuRadioGroup
          value={radioOption}
          onValueChange={(e) => onRadioOptionChange(e as PlanCharacteristicKey)}
        >
          <DropdownMenuRadioItem value={"count"}>
            {countLabel}
          </DropdownMenuRadioItem>
          <DropdownMenuRadioItem value={"totalAmount"}>
            {totalAmountLabel}
          </DropdownMenuRadioItem>
          <DropdownMenuRadioItem value={"averageAmount"}>
            {averageAmountLabel}
          </DropdownMenuRadioItem>
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
export interface DropDownMenuTypePlanCharacteristicProps
  extends PlanCharacteristicTexts {
  radioOption: PlanCharacteristicOption;
  onRadioOptionChange: (option: PlanCharacteristicOption) => void;
}

export function DropDownMenuTypePlanCharacteristicSelect({
  typeLabel,
  objectiveLabel,
  radioOption,
  onRadioOptionChange,
}: DropDownMenuTypePlanCharacteristicProps) {
  const label = radioOption === "type" ? typeLabel : objectiveLabel;

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline">{label}</Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56">
        <DropdownMenuRadioGroup
          value={radioOption}
          onValueChange={(e) =>
            onRadioOptionChange(e as PlanCharacteristicOption)
          }
        >
          <DropdownMenuRadioItem value={"type"}>
            {typeLabel}
          </DropdownMenuRadioItem>
          <DropdownMenuRadioItem value={"objective"}>
            {objectiveLabel}
          </DropdownMenuRadioItem>
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

const mapToDate = <T extends MonthlyOrderSummary>(arr: T[]) =>
  arr.map((d) => ({
    ...d,
    date: format(new Date(d.year, d.month - 1), "MM-yyyy"),
  }));

export interface PlanCharacteristicWrapperTexts
  extends PlanCharacteristicTexts {
  statisticLabel: string;
  typeSelectLabel: string;
  monthSelectLabel: string;
  chartName: string;
  title: string;
}
interface WrapperProps
  extends Omit<
      Props,
      "dataKey" | "characteristic" | "data" | "dataAvailable" | "chartName"
    >,
    PlanCharacteristicWrapperTexts {
  typePath: string;
  objectivePath: string;
  scatterPath: string;
}
const dateFormat = "dd-MM-yyyy";

export function PlanCharacteristicWrapperCompose({
  typePath,
  objectivePath,
  statisticLabel,
  typeSelectLabel,
  monthSelectLabel,
  chartName,
  title,
  scatterPath,
  ...rest
}: WrapperProps) {
  const lastMonth = subMonths(new Date(), 1);
  const initialMonth = format(lastMonth, dateFormat);
  const [month, setMonth] = useState<string>(initialMonth);
  const [cntAmnt, setCntAmnt] = useState<PlanCharacteristicKey>("count");
  useState<PlanCharacteristicKey>("count");
  const [planChar, setPlanChar] = useState<PlanCharacteristicOption>("type");
  const { messages: typeMessages, isFinished: isTypeFinished } =
    useFetchStream<MonthlyOrderSummaryType>({
      path: typePath,
      authToken: true,
      queryParams: {
        month,
      },
    });
  const { messages: objectiveMessages, isFinished: isObjectiveFinished } =
    useFetchStream<MonthlyOrderSummaryObjective>({
      path: objectivePath,
      authToken: true,
      queryParams: {
        month,
      },
    });

  const formattedTypeData = useMemo(
    () => mapToDate(typeMessages),
    [typeMessages],
  );

  const formattedObjectiveData = useMemo(
    () => mapToDate(objectiveMessages),
    [objectiveMessages],
  );
  return (
    <div className="w-full h-full">
      <h2 className="text-xl font-bold tracking-tighter md:text-2xl text-center mb-3 md:mb-6">
        {title}
      </h2>
      <div className="flex items-center justify-center sm:justify-end gap-10 flex-wrap">
        <div className="flex items-center justify-center gap-1.5">
          <p className="font-semibold">{monthSelectLabel}</p>
          <MonthPickerSelect onDateChange={setMonth} defaultDate={lastMonth} />
        </div>
        <div className="flex items-center justify-center gap-1.5">
          <p className="font-semibold">{statisticLabel}</p>
          <DropDownMenuCountTotalPlanCharacteristicSelect
            {...rest}
            radioOption={cntAmnt}
            onRadioOptionChange={setCntAmnt}
          />
        </div>
        <div className="flex items-center justify-center gap-1.5">
          <p className="font-semibold">{typeSelectLabel}</p>
          <DropDownMenuTypePlanCharacteristicSelect
            {...rest}
            radioOption={planChar}
            onRadioOptionChange={setPlanChar}
          />
        </div>
      </div>
      <div className="mt-2 md:mt-6">
        <DynamicPlanCharacteristic
          {...rest}
          dataAvailable={
            planChar === "type" ? isTypeFinished : isObjectiveFinished
          }
          data={
            planChar === "type" ? formattedTypeData : formattedObjectiveData
          }
          dataKey={cntAmnt}
          chartName={chartName}
          characteristic={planChar}
        />
      </div>
    </div>
  );
}

export function PlanCharacteristicWrapperScatter({
  typePath,
  objectivePath,
  statisticLabel,
  typeSelectLabel,
  monthSelectLabel,
  chartName,
  title,
  scatterPath,
  ...rest
}: WrapperProps) {
  const lastMonth = subMonths(new Date(), 1);
  const initialMonth = format(lastMonth, dateFormat);
  const [scatterMonth, setScatterMonth] = useState<string>(initialMonth);
  const [cntAmntScatter, setCntAmntScatter] =
    useState<PlanCharacteristicKey>("count");
  const { messages: scatterMessages, isFinished: isScatterFinished } =
    useFetchStream<MonthlyOrderSummaryObjectiveType>({
      path: scatterPath,
      authToken: true,
      queryParams: {
        month: scatterMonth,
      },
    });
  const formattedScatterData = useMemo(
    () => mapToDate(scatterMessages),
    [scatterMessages],
  );

  return (
    <div className="w-full h-full">
      <div className="flex items-center justify-center sm:justify-end gap-10 flex-wrap">
        <div className="flex items-center justify-center gap-1.5">
          <p className="font-semibold">{monthSelectLabel}</p>
          <MonthPickerSelect
            onDateChange={setScatterMonth}
            defaultDate={lastMonth}
          />
        </div>
        <div className="flex items-center justify-center gap-1.5">
          <p className="font-semibold">{statisticLabel}</p>
          <DropDownMenuCountTotalPlanCharacteristicSelect
            {...rest}
            radioOption={cntAmntScatter}
            onRadioOptionChange={setCntAmntScatter}
          />
        </div>
      </div>
      <div className="mt-2 md:mt-6">
        <DynamicPlanCharacteristicScatter
          {...rest}
          dataAvailable={isScatterFinished}
          data={formattedScatterData}
          dataKey={cntAmntScatter}
          chartName={chartName}
        />
      </div>
    </div>
  );
}

export function PlanCharacteristicWrapper(props: WrapperProps) {
  return (
    <div className="h-full w-full">
      <PlanCharacteristicWrapperCompose {...props} />
      <Separator className="my-10" />
      <PlanCharacteristicWrapperScatter {...props} />
    </div>
  );
}
