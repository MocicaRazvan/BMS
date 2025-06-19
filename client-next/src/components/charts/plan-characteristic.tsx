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
import {
  AverageAmount,
  DietType,
  MonthlyOrderSummary,
  MonthlyOrderSummaryObjectiveType,
  ObjectiveType,
} from "@/types/dto";
import useDownloadChartButton, {
  DateString,
} from "@/hoooks/charts/download-chart-button";
import { useMemo } from "react";
import { Skeleton } from "@/components/ui/skeleton";
import { motion } from "framer-motion";
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
import { formatChartValue } from "@/lib/utils";
import { useDebounceWithFirstTrue } from "@/hoooks/useDebounceWithFirstTrue";
import dynamic from "next/dynamic";
import useAxisNumberFormatter from "@/hoooks/charts/use-axis-number-formatter";

const DynamicEmptyChartLottie = dynamic(
  () => import("@/components/lottie/empty-chart-lottie"),
  {
    ssr: false,
    loading: () => (
      <Skeleton className="w-full h-full md:w-1/3 md:h-1/3 mx-auto" />
    ),
  },
);

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

export function PlanCharacteristic({
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
  const axisFormatter = useAxisNumberFormatter();
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
  const debounceDataAvailable = useDebounceWithFirstTrue(dataAvailable, 225);

  const { downloadChartRef, DownloadChartButton } = useDownloadChartButton({
    data,
  });

  const max = useMemo(
    () => Math.max(...data.map((item) => item[dataKey])),
    [data, dataKey],
  );

  return (
    <div className="w-full h-full">
      <ChartContainer
        config={chartConfig}
        className="aspect-auto h-[450px] w-full"
      >
        {!debounceDataAvailable ? (
          <Skeleton className={"w-full h-full"} />
        ) : data.length === 0 ? (
          <motion.div
            initial={{ scale: 0, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.5 }}
          >
            <DynamicEmptyChartLottie
              loop
              className="md:w-1/3 md:h-1/3 mx-auto"
            />
          </motion.div>
        ) : (
          <ComposedChart
            accessibilityLayer
            data={data}
            ref={downloadChartRef}
            margin={{
              bottom: 10,
              top: 24,
            }}
          >
            <CartesianGrid vertical={false} />
            <XAxis
              dataKey={characteristic}
              tickLine={false}
              tickMargin={10}
              axisLine={false}
              tickFormatter={(t) =>
                typeof t === "string" ? t.replace("_", " ") : t
              }
            />
            <YAxis
              domain={[0, Math.round(max + max / 10)]}
              tickFormatter={(t) => axisFormatter(t, dataKey !== "count")}
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
                formatter={formatChartValue}
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
                  retainUniqueKeys={true}
                  // hiddenKeys={allKeys.filter((key) => key !== dataKey)}
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
  const chartConfig = {
    count: {
      label: countLabel.replace("_", " "),
      color: `hsl(var(--chart-${countColorIndex}))`,
    },
    totalAmount: {
      label: totalAmountLabel.replace("_", " "),
      color: `hsl(var(--chart-${totalAmountColorIndex}))`,
    },
    averageAmount: {
      label: averageAmountLabel.replace("_", " "),
      color: `hsl(var(--chart-${averageAmountColorIndex}))`,
    },
  } satisfies ChartConfig;
  const debounceDataAvailable = useDebounceWithFirstTrue(dataAvailable, 225);

  const { downloadChartRef, DownloadChartButton } = useDownloadChartButton({
    data,
  });

  return (
    <div className="w-full h-full">
      <ChartContainer
        config={chartConfig}
        className="aspect-auto h-[450px] w-full"
      >
        {!debounceDataAvailable ? (
          <Skeleton className={"w-full h-full"} />
        ) : data.length === 0 ? (
          <motion.div
            initial={{ scale: 0, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.5 }}
          >
            <DynamicEmptyChartLottie
              loop
              className="md:w-1/3 md:h-1/3 mx-auto"
            />
          </motion.div>
        ) : (
          <ScatterChart
            accessibilityLayer
            ref={downloadChartRef}
            margin={{
              left: 35,
              bottom: 10,
            }}
          >
            <CartesianGrid />
            <XAxis
              type="category"
              dataKey="objective"
              name={objectiveLabel}
              tickFormatter={(t) =>
                typeof t === "string" ? t.replace("_", " ") : t
              }
              tickLine={false}
            />
            <YAxis
              type="category"
              dataKey="type"
              name={typeLabel}
              tickLine={false}
            />
            <ZAxis
              type="number"
              dataKey={dataKey}
              name={dataKey}
              domain={["auto", "auto"]}
              range={[250, 2250]}
              scale="sqrt"
            />
            <ChartTooltip
              cursor={false}
              content={
                <ChartTooltipContent
                  indicator="dot"
                  valueFormatter={(v) => v.replace("_", " ")}
                />
              }
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
                formatter={formatChartValue}
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
