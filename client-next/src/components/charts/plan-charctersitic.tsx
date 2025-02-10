"use client";

import { v4 as uuidv4 } from "uuid";
import {
  ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { useDebounce } from "@/components/ui/multiple-selector";
import {
  AverageAmount,
  DietType,
  MonthlyOrderSummary,
  MonthlyOrderSummaryObjective,
  MonthlyOrderSummaryObjectiveType,
  MonthlyOrderSummaryType,
  ObjectiveType,
} from "@/types/dto";
import useDownloadChartButton, {
  DateString,
} from "@/hoooks/charts/download-chart-button";
import { useMemo, useState } from "react";
import { Skeleton } from "@/components/ui/skeleton";
import { motion } from "framer-motion";
import Lottie from "react-lottie-player";
import emptyChart from "../../../public/lottie/emptyChart.json";
import {
  Bar,
  CartesianGrid,
  ComposedChart,
  LabelList,
  Line,
  Scatter,
  ScatterChart,
  XAxis,
  YAxis,
  ZAxis,
} from "recharts";
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

export interface PlanCharacteristicTexts {
  countLabel: string;
  totalAmountLabel: string;
  averageAmountLabel: string;
  typeLabel: string;
  objectiveLabel: string;
}

export type PlanCharacteristicKey = "count" | "totalAmount" | "averageAmount";
export type PlanCharacteristicOption = "type" | "objective";
const allKeys = ["count", "totalAmount", "averageAmount"] as const;

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

export default function PlanCharacteristic({
  dataKey,
  characteristic,
  extraChartConfig,
  countColorIndex = 1,
  totalAmountColorIndex = 6,
  averageAmountColorIndex = 3,
  lineColorIndex = 8,
  totalAmountLabel,
  countLabel,
  dataAvailable,
  data,
  chartName,
  averageAmountLabel,
}: Props) {
  const stackId = uuidv4();
  const chartConfig = {
    count: {
      label: countLabel,
      color: `hsl(var(--chart-${countColorIndex}))`,
    },
    totalAmount: {
      label: totalAmountLabel,
      color: `hsl(var(--chart-${totalAmountColorIndex}))`,
    },
    averageAmount: {
      label: averageAmountLabel,
      color: `hsl(var(--chart-${averageAmountColorIndex}))`,
    },
    ...(extraChartConfig && extraChartConfig),
  } satisfies ChartConfig;
  const debounceDataAvailable = useDebounce(dataAvailable, 225);

  const { downloadChartRef, DownloadChartButton } = useDownloadChartButton({
    data,
  });

  const max = useMemo(() => {
    return Math.max(...data.map((item) => item[dataKey]));
  }, [JSON.stringify(data), dataKey]);

  return (
    <div className="w-full h-full">
      <ChartContainer
        config={chartConfig}
        className="aspect-auto h-[450px] w-full "
      >
        {!debounceDataAvailable ? (
          <Skeleton className={"w-full h-full"} />
        ) : data.length === 0 ? (
          <motion.div
            initial={{ scale: 0, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.5 }}
          >
            <Lottie
              loop
              animationData={emptyChart}
              play
              className="md:w-1/3 md:h-1/3 mx-auto"
            />
          </motion.div>
        ) : (
          <ComposedChart accessibilityLayer data={data} ref={downloadChartRef}>
            <CartesianGrid vertical={false} />
            <XAxis
              dataKey={characteristic}
              tickLine={false}
              tickMargin={10}
              axisLine={false}
              // tickFormatter={(value) => value.slice(0, 3)}
            />
            <YAxis domain={[0, Math.round(max + max / 10)]} />
            <ChartTooltip
              cursor={false}
              content={<ChartTooltipContent indicator="dot" />}
            />
            <Bar
              dataKey={dataKey}
              fill={`var(--color-${dataKey})`}
              radius={4}
              stackId={stackId}
            >
              <LabelList
                position="top"
                offset={12}
                className="fill-foreground text-[15px]"
                fontSize={12}
                formatter={formatLabelList}
              />
            </Bar>

            {data.length > 1 && (
              <Line
                type="monotone"
                dataKey={dataKey}
                stroke={`hsl(var(--chart-${lineColorIndex}))`}
                strokeWidth={2}
                // dot={false}
              />
            )}

            <ChartLegend
              key={dataKey + stackId}
              content={
                <ChartLegendContent
                  hiddenKeys={allKeys.filter((key) => key !== dataKey)}
                />
              }
            />
          </ComposedChart>
        )}
      </ChartContainer>
      {data.length > 0 && (
        <div className="w-full mt-2 flex justify-end">
          <DownloadChartButton fileName={`${chartName}_${dataKey}`} />
        </div>
      )}
    </div>
  );
}

interface ScatterProps
  extends PlanCharacteristicTexts,
    Omit<PlanCharacteristicColors, "lineColorIndex"> {
  data: (MonthlyOrderSummaryObjectiveType & DateString)[];
  dataKey: PlanCharacteristicKey;
  dataAvailable: boolean;
  chartName: string;
}

function formatLabelList(value: number | string) {
  return typeof value === "number" && !Number.isInteger(value)
    ? value.toFixed(2)
    : value;
}

export function PlanCharacteristicScatter({
  averageAmountColorIndex = 3,
  countColorIndex = 1,
  averageAmountLabel,
  totalAmountColorIndex = 6,
  totalAmountLabel,
  countLabel,
  objectiveLabel,
  dataAvailable,
  data,
  typeLabel,
  dataKey,
  chartName,
}: ScatterProps) {
  const stackId = uuidv4();
  const chartConfig = {
    count: {
      label: countLabel,
      color: `hsl(var(--chart-${countColorIndex}))`,
    },
    totalAmount: {
      label: totalAmountLabel,
      color: `hsl(var(--chart-${totalAmountColorIndex}))`,
    },
    averageAmount: {
      label: averageAmountLabel,
      color: `hsl(var(--chart-${averageAmountColorIndex}))`,
    },
  } satisfies ChartConfig;
  const debounceDataAvailable = useDebounce(dataAvailable, 225);

  const { downloadChartRef, DownloadChartButton } = useDownloadChartButton({
    data,
  });

  return (
    <div className="w-full h-full">
      <ChartContainer
        config={chartConfig}
        className="aspect-auto h-[450px] w-full "
      >
        {!debounceDataAvailable ? (
          <Skeleton className={"w-full h-full"} />
        ) : data.length === 0 ? (
          <motion.div
            initial={{ scale: 0, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.5 }}
          >
            <Lottie
              loop
              animationData={emptyChart}
              play
              className="md:w-1/3 md:h-1/3 mx-auto"
            />
          </motion.div>
        ) : (
          <ScatterChart
            accessibilityLayer
            ref={downloadChartRef}
            margin={{
              left: 35,
            }}
          >
            <CartesianGrid />
            <XAxis type="category" dataKey="objective" name={objectiveLabel} />
            <YAxis type="category" dataKey="type" name={typeLabel} />
            <ZAxis
              type="number"
              dataKey={dataKey}
              name="totalAmount"
              domain={["auto", "auto"]}
              range={[250, 2250]}
              scale="sqrt"
            />
            <ChartTooltip
              cursor={false}
              content={<ChartTooltipContent indicator="dot" />}
            />
            <Scatter
              name={dataKey}
              data={data}
              dataKey={dataKey}
              fill={`var(--color-${dataKey})`}
            >
              <LabelList
                position="top"
                offset={12}
                className="fill-foreground text-[15px]"
                fontSize={12}
                dataKey={dataKey}
                formatter={formatLabelList}
              />
            </Scatter>
            <ChartLegend content={<ChartLegendContent />} />
          </ScatterChart>
        )}
      </ChartContainer>
      {data.length > 0 && (
        <div className="w-full mt-2 flex justify-end">
          <DownloadChartButton fileName={`${chartName}_scatter_${dataKey}`} />
        </div>
      )}
    </div>
  );
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
    [JSON.stringify(typeMessages)],
  );

  const formattedObjectiveData = useMemo(
    () => mapToDate(objectiveMessages),
    [JSON.stringify(objectiveMessages)],
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
        <PlanCharacteristic
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
    [JSON.stringify(scatterMessages)],
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
        <PlanCharacteristicScatter
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
