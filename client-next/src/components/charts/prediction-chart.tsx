"use client";

import useFetchStream from "@/hoooks/useFetchStream";
import { MonthlyOrderSummaryPrediction } from "@/types/dto";
import { memo, useMemo, useState } from "react";
import { format } from "date-fns";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { isDeepEqual } from "@/lib/utils";
import {
  ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { useDebounce } from "@/components/ui/multiple-selector";
import { Skeleton } from "@/components/ui/skeleton";
import { motion } from "framer-motion";
import Lottie from "react-lottie-player";
import emptyChart from "../../../public/lottie/emptyChart.json";
import {
  Area,
  CartesianGrid,
  ComposedChart,
  Line,
  XAxis,
  YAxis,
} from "recharts";
import {
  CountTotalAmountRadioOptionsType,
  DropDownMenuCountTotalAmountSelect,
} from "@/components/charts/totalAmount-count-ordres";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import useDownloadChartButton from "@/hoooks/charts/download-chart-button";

interface ColorIndexes {
  countColorIndex?: number;
  totalAmountColorIndex?: number;
}

interface DataLabels {
  totalAmountLabel: string;
  countLabel: string;
  totalAmountAreaLabel: string;
  countAreaLabel: string;
}

export interface PredictionChartTexts extends DataLabels {
  title: string;
  disclaimer: string;
  predictionLengthLabel: string;
}
interface Props extends ColorIndexes {
  path: string;
  texts: PredictionChartTexts;
}

interface FormatedData {
  count: string;
  totalAmount: string;
  countArea: string[];
  totalAmountArea: string[];
  date: string;
}
interface MaxedQuantile {
  count: number;
  totalAmount: number;
}

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
      <PredictionChartContainer
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

interface ContainerProps extends Required<ColorIndexes>, DataLabels {
  data: FormatedData[];
  dataAvailable: boolean;
  dataKey: "totalAmount" | "count";
  chartName: string;
  maxedQuantile: MaxedQuantile;
}
const PredictionChartContainer = memo(
  ({
    data,
    dataAvailable,
    dataKey,
    totalAmountColorIndex,
    countColorIndex,
    countAreaLabel,
    countLabel,
    totalAmountLabel,
    totalAmountAreaLabel,
    chartName,
    maxedQuantile,
  }: ContainerProps) => {
    const { downloadChartRef, DownloadChartButton } = useDownloadChartButton({
      data,
    });
    const chartConfig = {
      count: {
        label: countLabel,
        color: `hsl(var(--chart-${countColorIndex}))`,
      },
      countArea: {
        label: countAreaLabel,
        color: `hsl(var(--chart-${countColorIndex}))`,
      },
      totalAmount: {
        label: totalAmountLabel,
        color: `hsl(var(--chart-${totalAmountColorIndex}))`,
      },
      totalAmountArea: {
        label: totalAmountAreaLabel,
        color: `hsl(var(--chart-${totalAmountColorIndex}))`,
      },
    } satisfies ChartConfig;
    const debounceDataAvailable = useDebounce(dataAvailable, 225);

    return (
      <div className="w-full py-16">
        <ChartContainer
          config={chartConfig}
          className="aspect-auto h-[450px] w-full"
        >
          {!debounceDataAvailable ? (
            <Skeleton className="w-full h-full" />
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
            <ComposedChart
              data={data}
              accessibilityLayer
              ref={downloadChartRef}
            >
              <CartesianGrid vertical={false} />
              <XAxis
                dataKey="date"
                tickLine={false}
                axisLine={false}
                tickMargin={8}
                minTickGap={32}
              />
              <YAxis
                tickLine={false}
                axisLine={false}
                tickMargin={8}
                tickFormatter={(tick) => (Number.isInteger(tick) ? tick : "")}
                interval="preserveStartEnd"
                domain={[0, Math.floor(1.1 * maxedQuantile[dataKey])]}
                allowDecimals={false}
                allowDataOverflow={true}
              />
              <ChartTooltip
                cursor={false}
                content={<ChartTooltipContent indicator="dot" />}
              />
              <ChartLegend
                content={<ChartLegendContent hiddenKeys={[`${dataKey}Area`]} />}
              />
              <Line
                type="natural"
                dataKey={dataKey}
                stroke={`var(--color-${dataKey})`}
                connectNulls={true}
              />
              <Area
                type="monotoneX"
                dataKey={`${dataKey}Area`}
                stroke="none"
                fill={`var(--color-${dataKey}Area)`}
                connectNulls
                fillOpacity={0.35}
                dot={false}
                activeDot={false}
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
  },
  isDeepEqual,
);
PredictionChartContainer.displayName = "PredictionChartContainer";

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
