"use client";
import React, { memo, useMemo } from "react";
import { createChartConfig } from "@/components/charts/top-chart-wrapper";
import {
  DietType,
  dietTypes,
  ObjectiveType,
  planObjectives,
} from "@/types/dto";
import {
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { Pie, PieChart } from "recharts";
import { isDeepEqual } from "@/lib/utils";

export interface BasePieChartProps<T extends "type" | "objective"> {
  type: T;
  offset?: number;
}

const createChartData = <T extends "type" | "objective">(
  data: T extends "type"
    ? Partial<Record<DietType, number>>
    : Partial<Record<ObjectiveType, number>>,
  type: T,
) =>
  Object.entries(data).map(([k, value]) => ({
    [type]: k,
    value: Number.isInteger(value) ? value : Math.round(value * 100) / 100,
    fill: `var(--color-${k})`,
  }));

interface TopTrainersPieChartProps<T extends "type" | "objective">
  extends BasePieChartProps<T> {
  chartData: T extends "type"
    ? Partial<Record<DietType, number>>
    : Partial<Record<ObjectiveType, number>>;
}

const TopTrainersPieChart = memo(
  <T extends "type" | "objective">({
    type,
    chartData,
    offset = 0,
  }: TopTrainersPieChartProps<T>) => {
    const chartConfig = useMemo(
      () =>
        createChartConfig(
          type === "type" ? dietTypes : planObjectives,
          offset,
          false,
        ),
      [type, offset],
    );

    return (
      <ChartContainer
        config={chartConfig}
        className="min-h-[300px] h-full mx-auto aspect-square [&_.recharts-pie-label-text]:fill-foreground my-0 p-0"
      >
        <PieChart>
          <Pie
            data={createChartData(chartData, type)}
            outerRadius={70}
            dataKey="value"
            stroke="0"
            nameKey={type}
            label={true}
            labelLine={true}
          />
          <ChartTooltip content={<ChartTooltipContent />} />
          <ChartLegend
            content={
              <ChartLegendContent className="flex-wrap min-h-16 items-start" />
            }
          />
        </PieChart>
      </ChartContainer>
    );
  },
  isDeepEqual,
);

TopTrainersPieChart.displayName = "TopTrainersPieChart";

export { TopTrainersPieChart };
