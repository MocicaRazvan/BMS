"use client";

import { useMemo } from "react";
import {
  Area,
  Bar,
  BarChart,
  CartesianGrid,
  ComposedChart,
  Label,
  Line,
  ReferenceLine,
  XAxis,
  YAxis,
} from "recharts";

import {
  ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";

import { useLocale } from "next-intl";
import { motion } from "framer-motion";
import regression from "regression";
import { Skeleton } from "@/components/ui/skeleton";
import useDownloadChartButton from "@/hoooks/charts/download-chart-button";
import { v4 as uuidv4 } from "uuid";
import { useDebounceWithFirstTrue } from "@/hoooks/useDebounceWithFirstTrue";
import dynamic from "next/dynamic";

export interface TotalAmountCountOrdersData {
  count: number;
  totalAmount: number;
  date: string;
}

export interface DropDownTexts {
  countLabel: string;
  totalAmountLabel: string;
  bothLabel: string;
}
export interface TotalAmountCountOrdersTexts extends DropDownTexts {
  averageTotalAmountLabel: string;
  averageCountLabel: string;
  showTrendLineLabel: string;
  hideTrendLineLabel: string;
  trendLineLabel: string;
}
type ExtendedData = TotalAmountCountOrdersData &
  Partial<Record<string, string | number>>;

interface Props extends TotalAmountCountOrdersTexts {
  data: TotalAmountCountOrdersData[] | ExtendedData[];
  dataAvailable: boolean;
  showCount?: boolean;
  showTotalAmount?: boolean;
  showTrendLine?: boolean;
  chartName: string;
  extraChartConfig?: ChartConfig;
  countColorIndex?: number;
  totalAmountColorIndex?: number;
  mapCountLabel?: (showCount: boolean, showTotalAmount: boolean) => string;
  mapTotalAmountLabel?: (
    showCount: boolean,
    showTotalAmount: boolean,
  ) => string;
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

export function TotalAmountCountOrders({
  data,
  dataAvailable,
  totalAmountLabel,
  countLabel,
  showCount = true,
  showTotalAmount = true,
  showTrendLine = true,
  trendLineLabel,
  chartName,
  extraChartConfig,
  mapCountLabel = (showCount, showTotalAmount) => "count",
  mapTotalAmountLabel = (showCount, showTotalAmount) => "totalAmount",
  countColorIndex = 1,
  totalAmountColorIndex = 6,
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
    ...(extraChartConfig && extraChartConfig),
  } satisfies ChartConfig;

  const debounceDataAvailable = useDebounceWithFirstTrue(dataAvailable, 225);

  const regressionKey: "countLine" | "totalAmountLine" | null =
    showCount && showTotalAmount
      ? null
      : showCount
        ? "countLine"
        : showTotalAmount
          ? "totalAmountLine"
          : null;

  const regressionPoints = useMemo(() => {
    if (!regressionKey || !showTrendLine) return null;
    // const points = data.map((d, idx) => [idx, d["count"]]);
    const points = data.reduce<
      Record<keyof Omit<TotalAmountCountOrdersData, "date">, [number, number][]>
    >(
      (acc, cur, i) => {
        acc.count.push([i, cur.count]);
        acc.totalAmount.push([i, cur.totalAmount]);
        return acc;
      },
      {
        count: [],
        totalAmount: [],
      },
    );
    const countRegression = regression.linear(points.count);
    const totalAmountRegression = regression.linear(points.totalAmount);

    return data.map((d, idx) => ({
      date: d.date,
      countLine: Math.max(0, countRegression.predict(idx)[1]),
      totalAmountLine: Math.max(0, totalAmountRegression.predict(idx)[1]),
    }));
  }, [data, regressionKey, showTrendLine]);

  const max = useMemo(
    () =>
      data.reduce(
        (acc, cur) => ({
          ...acc,

          count: Math.max(acc.count, cur.count),
          totalAmount: Math.max(acc.totalAmount, cur.totalAmount),
        }),
        { count: 0, totalAmount: 0 },
      ),
    [data],
  );

  const {
    downloadChartRef: barChartRef,
    DownloadChartButton: BarChartDownloadButton,
  } = useDownloadChartButton({ data });

  const {
    DownloadChartButton: ComposedChartDownloadButton,
    downloadChartRef: composedChartRef,
  } = useDownloadChartButton({ data });

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
        ) : data.length === 1 ? (
          <BarChart
            data={data}
            ref={barChartRef}
            margin={{
              top: 15,
            }}
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
              interval={"preserveStartEnd"}
              domain={[0, "dataMax"]}
              allowDecimals={false}
            />
            <ChartTooltip
              cursor={false}
              content={<ChartTooltipContent indicator="dot" />}
            />
            {showCount && (
              <Bar
                dataKey={mapCountLabel(showCount, showTotalAmount)}
                fill="var(--color-count)"
                stackId={stackId}
              />
            )}
            {showTotalAmount && (
              <Bar
                dataKey={mapTotalAmountLabel(showCount, showTotalAmount)}
                fill="var(--color-totalAmount)"
                stackId={stackId}
              />
            )}
            <ChartLegend content={<ChartLegendContent />} />
          </BarChart>
        ) : (
          <ComposedChart
            data={data}
            ref={composedChartRef}
            margin={{
              top: 15,
            }}
          >
            <defs>
              <linearGradient
                id={`fillCount-${stackId}`}
                x1="0"
                y1="0"
                x2="0"
                y2="1"
              >
                <stop
                  offset="5%"
                  stopColor="var(--color-count)"
                  stopOpacity={0.8}
                />
                <stop
                  offset="95%"
                  stopColor="var(--color-count)"
                  stopOpacity={0.1}
                />
              </linearGradient>
              <linearGradient
                id={`fillTotalAmount-${stackId}`}
                x1="0"
                y1="0"
                x2="0"
                y2="1"
              >
                <stop
                  offset="5%"
                  stopColor="var(--color-totalAmount)"
                  stopOpacity={0.8}
                />
                <stop
                  offset="95%"
                  stopColor="var(--color-totalAmount)"
                  stopOpacity={0.1}
                />
              </linearGradient>
            </defs>
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
              domain={[
                0,
                showCount && showTotalAmount
                  ? "auto"
                  : max[showCount ? "count" : "totalAmount"],
              ]}
              allowDecimals={false}
            />
            <ChartTooltip
              cursor={false}
              content={
                <ChartTooltipContent
                  indicator="dot"
                  hiddenKeys={[regressionKey || ""]}
                />
              }
            />
            {showCount && (
              <Area
                dataKey={mapCountLabel(showCount, showTotalAmount)}
                type="monotoneX"
                fill={`url(#fillCount-${stackId})`}
                stroke="var(--color-count)"
                dot={{ r: 4, fill: "var(--color-count)" }}
                stackId={stackId}
              />
            )}
            {showTotalAmount && (
              <Area
                dataKey={mapTotalAmountLabel(showCount, showTotalAmount)}
                type="monotoneX"
                fill={`url(#fillTotalAmount-${stackId})`}
                stroke="var(--color-totalAmount)"
                dot={{ r: 4, fill: "var(--color-totalAmount)" }}
                stackId={stackId}
              />
            )}
            {showTrendLine && regressionPoints && regressionKey && (
              <Line
                type="monotone"
                dataKey={regressionKey}
                data={regressionPoints}
                stroke="hsl(var(--chart-4))"
                strokeWidth={2}
                dot={false}
                activeDot={false}
                label={({ index, x, y }) => {
                  if (index > 0) return <></>;
                  return (
                    <g>
                      <motion.text
                        x={x + 70}
                        y={y}
                        fill="hsl(var(--foreground))"
                        textAnchor="middle"
                        className="md:text-lg font-bold"
                        initial={{ opacity: 0, scale: 0 }}
                        animate={{ opacity: 1, scale: 1 }}
                        transition={{ duration: 0.5 }}
                      >
                        {trendLineLabel}
                      </motion.text>
                    </g>
                  );
                }}
              />
            )}
            <ChartLegend
              content={
                <ChartLegendContent hiddenKeys={[regressionKey || ""]} />
              }
            />
          </ComposedChart>
        )}
      </ChartContainer>
      {data.length > 0 && (
        <div className="w-full mt-2 flex justify-end">
          {data.length === 1 ? (
            <BarChartDownloadButton fileName={`${chartName}_barChart`} />
          ) : (
            <ComposedChartDownloadButton
              fileName={`${chartName}_composedChart`}
            />
          )}
        </div>
      )}
    </div>
  );
}

interface TotalAmountOrdersSingleBarChartProps
  extends TotalAmountCountOrdersTexts {
  data: TotalAmountCountOrdersData[];
  dataAvailable: boolean;
  fieldKey: keyof Omit<TotalAmountCountOrdersData, "date"> & string;
  countColorIndex?: number;
  totalAmountColorIndex?: number;
}

export function TotalAmountOrdersSingleBarChart({
  totalAmountLabel,
  countLabel,
  bothLabel,
  dataAvailable,
  data,
  fieldKey,
  averageTotalAmountLabel,
  averageCountLabel,
  countColorIndex = 1,
  totalAmountColorIndex = 6,
}: TotalAmountOrdersSingleBarChartProps) {
  const stackId = uuidv4();
  const locale = useLocale();
  const label = fieldKey === "count" ? countLabel : totalAmountLabel;
  const avgLabel =
    fieldKey === "count" ? averageCountLabel : averageTotalAmountLabel;
  const chartConfig = {
    [fieldKey]: {
      label,
      color:
        fieldKey === "count"
          ? `hsl(var(--chart-${countColorIndex}))`
          : `hsl(var(--chart-${totalAmountColorIndex}))`,
    },
  } satisfies ChartConfig;

  const debounceDataAvailable = useDebounceWithFirstTrue(dataAvailable, 225);
  const meanValue = useMemo(
    () => data.reduce((acc, curr) => acc + curr[fieldKey], 0) / data.length,
    [data, fieldKey],
  );
  const { downloadChartRef, DownloadChartButton } = useDownloadChartButton({
    data,
  });

  return (
    <div className="w-full py-16">
      <ChartContainer
        config={chartConfig}
        className="aspect-auto h-[450px] w-full"
      >
        {!debounceDataAvailable ? (
          <Skeleton className={"w-full h-full"} />
        ) : (
          <BarChart
            data={data}
            ref={downloadChartRef}
            margin={{
              top: 15,
            }}
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
              tickCount={8}
              domain={[0, "dataMax"]}
              allowDecimals={false}
            />
            <ChartTooltip
              cursor={false}
              content={<ChartTooltipContent indicator="dot" />}
            />
            <Bar
              dataKey={fieldKey}
              fill={`var(--color-${fieldKey})`}
              stackId={stackId}
            />
            <ChartLegend content={<ChartLegendContent />} />
            <ReferenceLine
              y={meanValue}
              stroke="red"
              strokeDasharray="3 3"
              strokeWidth={4}
              className="custom-reference-line"
            >
              <Label
                position="top"
                value={avgLabel}
                offset={10}
                fill="hsl(var(--foreground))"
                className="md:text-lg"
              />
              <Label
                position="bottom"
                value={Math.round(meanValue).toLocaleString(locale)}
                className="text-lg md:text-xl "
                fill="hsl(var(--foreground))"
                offset={10}
                startOffset={100}
              />
            </ReferenceLine>
          </BarChart>
        )}
      </ChartContainer>
      {data.length > 0 && (
        <div className="w-full mt-2 flex justify-end">
          <DownloadChartButton fileName={`${label}_barChart`} />
        </div>
      )}
    </div>
  );
}
