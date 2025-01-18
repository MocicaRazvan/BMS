"use client";

import { useEffect, useMemo } from "react";
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
import { useDebounce } from "@/components/ui/multiple-selector";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import Lottie from "react-lottie-player";
import emptyChart from "../../../public/lottie/emptyChart.json";
import { motion } from "framer-motion";
import * as ss from "simple-statistics";
import { Skeleton } from "@/components/ui/skeleton";
import useDownloadChartButton from "@/hoooks/charts/download-chart-button";

export interface TotalAmountCountOrdersData {
  count: number;
  totalAmount: number;
  date: string;
}
export interface TotalAmountCountOrdersTexts {
  countLabel: string;
  totalAmountLabel: string;
  bothLabel: string;
  averageTotalAmountLabel: string;
  averageCountLabel: string;
  showTrendLineLabel: string;
  hideTrendLineLabel: string;
  trendLineLabel: string;
}

interface Props extends TotalAmountCountOrdersTexts {
  data: TotalAmountCountOrdersData[];
  dataAvailable: boolean;
  showCount?: boolean;
  showTotalAmount?: boolean;
  showTrendLine?: boolean;
  chartName: string;
}

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
}: Props) {
  const chartConfig = {
    count: {
      label: countLabel,
      color: "hsl(var(--chart-1))",
    },
    totalAmount: {
      label: totalAmountLabel,
      color: "hsl(var(--chart-6))",
    },
  } satisfies ChartConfig;

  const debounceDataAvailable = useDebounce(dataAvailable, 225);

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
      (acc, cur, i) => ({
        count: [...acc.count, [i, cur.count]],
        totalAmount: [...acc.totalAmount, [i, cur.totalAmount]],
      }),
      {
        count: [],
        totalAmount: [],
      },
    );
    const countRegression = ss.linearRegression(points.count);
    const totalAmountRegression = ss.linearRegression(points.totalAmount);
    const countLine = ss.linearRegressionLine(countRegression);
    const totalAmountLine = ss.linearRegressionLine(totalAmountRegression);

    return data.map((d, idx) => ({
      date: d.date,
      countLine: countLine(idx),
      totalAmountLine: totalAmountLine(idx),
    }));
  }, [JSON.stringify(data), regressionKey, showTrendLine]);

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
        ) : data.length === 1 ? (
          <BarChart data={data} ref={barChartRef}>
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
            {showCount && <Bar dataKey="count" fill="var(--color-count)" />}
            {showTotalAmount && (
              <Bar dataKey="totalAmount" fill="var(--color-totalAmount)" />
            )}
            <ChartLegend content={<ChartLegendContent />} />
          </BarChart>
        ) : (
          <ComposedChart data={data} ref={composedChartRef}>
            <defs>
              <linearGradient id="fillCount" x1="0" y1="0" x2="0" y2="1">
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
              <linearGradient id="fillTotalAmount" x1="0" y1="0" x2="0" y2="1">
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
              interval={"preserveStartEnd"}
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
                dataKey="count"
                type="monotoneX"
                fill="url(#fillCount)"
                stroke="var(--color-count)"
                dot={{ r: 4, fill: "var(--color-count)" }}
              />
            )}
            {showTotalAmount && (
              <Area
                dataKey="totalAmount"
                type="monotoneX"
                fill="url(#fillTotalAmount)"
                stroke="var(--color-totalAmount)"
                dot={{ r: 4, fill: "var(--color-totalAmount)" }}
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

export const CountTotalAmountRadioOptions = [
  "both",
  "count",
  "totalAmount",
] as const;

export type CountTotalAmountRadioOptionsType =
  (typeof CountTotalAmountRadioOptions)[number];

export interface DropDownMenuCountTotalAmountSelectProps
  extends TotalAmountCountOrdersTexts {
  radioOption: CountTotalAmountRadioOptionsType;
  onRadioOptionChange: (option: CountTotalAmountRadioOptionsType) => void;
  showBoth?: boolean;
}

export function DropDownMenuCountTotalAmountSelect({
  countLabel,
  totalAmountLabel,
  bothLabel,
  radioOption,
  onRadioOptionChange,
  showBoth = true,
}: DropDownMenuCountTotalAmountSelectProps) {
  const label =
    radioOption === "both"
      ? bothLabel
      : radioOption === "count"
        ? countLabel
        : totalAmountLabel;
  useEffect(() => {
    if (!showBoth && radioOption === "both") {
      onRadioOptionChange("count");
    }
  }, [onRadioOptionChange, radioOption, showBoth]);
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline">{label}</Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56">
        <DropdownMenuRadioGroup
          value={radioOption}
          onValueChange={(e) =>
            onRadioOptionChange(e as CountTotalAmountRadioOptionsType)
          }
        >
          {showBoth && (
            <DropdownMenuRadioItem value={CountTotalAmountRadioOptions[0]}>
              {bothLabel}
            </DropdownMenuRadioItem>
          )}
          <DropdownMenuRadioItem value={CountTotalAmountRadioOptions[1]}>
            {countLabel}
          </DropdownMenuRadioItem>
          <DropdownMenuRadioItem value={CountTotalAmountRadioOptions[2]}>
            {totalAmountLabel}
          </DropdownMenuRadioItem>
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

export interface TrendLineButtonProps extends TotalAmountCountOrdersTexts {
  showTrendLine: boolean;
  onShowTrendLineChange: (showTrendLine: boolean) => void;
  hide?: boolean;
}

export const TrendLineButton = ({
  showTrendLine,
  onShowTrendLineChange,
  showTrendLineLabel,
  hideTrendLineLabel,
  hide,
}: TrendLineButtonProps) => {
  if (hide) return null;
  return (
    <Button
      variant="outline"
      className="min-w-[180px]"
      onClick={() => onShowTrendLineChange(showTrendLine ? false : true)}
    >
      {showTrendLine ? hideTrendLineLabel : showTrendLineLabel}
    </Button>
  );
};

interface TotalAmountOrdersSingleBarChartProps
  extends TotalAmountCountOrdersTexts {
  data: TotalAmountCountOrdersData[];
  dataAvailable: boolean;
  fieldKey: keyof Omit<TotalAmountCountOrdersData, "date"> & string;
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
}: TotalAmountOrdersSingleBarChartProps) {
  const locale = useLocale();
  const label = fieldKey === "count" ? countLabel : totalAmountLabel;
  const avgLabel =
    fieldKey === "count" ? averageCountLabel : averageTotalAmountLabel;
  const chartConfig = {
    [fieldKey]: {
      label,
      color:
        fieldKey === "count" ? "hsl(var(--chart-1))" : "hsl(var(--chart-6))",
    },
  } satisfies ChartConfig;

  const debounceDataAvailable = useDebounce(dataAvailable, 225);
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
          <BarChart data={data} ref={downloadChartRef}>
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
            <Bar dataKey={fieldKey} fill={`var(--color-${fieldKey})`} />
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
