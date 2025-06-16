"use client";

import { memo } from "react";
import useDownloadChartButton from "@/hoooks/charts/download-chart-button";
import {
  ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { useDebounceWithFirstTrue } from "@/hoooks/useDebounceWithFirstTrue";
import { Skeleton } from "@/components/ui/skeleton";
import { motion } from "framer-motion";
import {
  Area,
  CartesianGrid,
  ComposedChart,
  Line,
  XAxis,
  YAxis,
} from "recharts";
import { isDeepEqual } from "@/lib/utils";
import dynamic from "next/dynamic";
import useAxisNumberFormatter from "@/hoooks/charts/use-axis-number-formatter";

export interface DataLabels {
  totalAmountLabel: string;
  countLabel: string;
  totalAmountAreaLabel: string;
  countAreaLabel: string;
}

export interface ColorIndexes {
  countColorIndex?: number;
  totalAmountColorIndex?: number;
}
export interface FormatedData {
  count: string;
  totalAmount: string;
  countArea: string[];
  totalAmountArea: string[];
  date: string;
}
interface ContainerProps extends Required<ColorIndexes>, DataLabels {
  data: FormatedData[];
  dataAvailable: boolean;
  dataKey: "totalAmount" | "count";
  chartName: string;
  maxedQuantile: MaxedQuantile;
}

interface MaxedQuantile {
  count: number;
  totalAmount: number;
}

const DynamicEmptyChartLottie = dynamic(
  () => import("@/components/lottie/empty-chart-lottie"),
  {
    ssr: false,
    loading: () => (
      <Skeleton className="w-full h-full md:w-1/3 md:h-1/3 mx-auto" />
    ),
  },
);

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
    const debounceDataAvailable = useDebounceWithFirstTrue(dataAvailable, 225);
    const axisFormatter = useAxisNumberFormatter();

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
              <DynamicEmptyChartLottie
                loop
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
                tickFormatter={(t) => axisFormatter(t, dataKey !== "count")}
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

export { PredictionChartContainer };
