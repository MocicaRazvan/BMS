"use client";
import { v4 as uuid } from "uuid";
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { Bar, BarChart, ReferenceLine, XAxis, YAxis } from "recharts";
import React from "react";
import useAxisNumberFormatter from "@/hoooks/charts/use-axis-number-formatter";

interface TopChartMeanRelativeProps {
  chartKey: string;
  chartLabel: string;
  barData: number;
  maxBar: number;
  maxOffset?: number;
  referenceValue: number;
  referenceLabel: string;
  chartColorNumber?: number;
}

export function TopChartMeanRelative({
  chartKey,
  chartLabel,
  barData,
  maxBar,
  maxOffset = 10,
  referenceValue,
  referenceLabel,
  chartColorNumber = 6,
}: TopChartMeanRelativeProps) {
  const axisFormatter = useAxisNumberFormatter();
  const stackId = uuid();
  if (maxBar - referenceValue < maxOffset) {
    maxOffset += Math.min(maxBar / 25, 80);
  }
  return (
    <ChartContainer
      config={{
        [chartKey]: {
          label: chartLabel,
          color: `hsl(var(--chart-${chartColorNumber}))`,
        },
      }}
      className="h-[300px] mx-auto aspect-square"
    >
      <BarChart
        data={[
          {
            name: chartLabel,
            [chartKey]: barData.toFixed(2),
          },
        ]}
      >
        <XAxis dataKey="name" />
        <YAxis
          domain={[0, Math.floor(maxBar + maxOffset)]}
          tickFormatter={axisFormatter}
        />
        <ChartTooltip content={<ChartTooltipContent hideLabel={true} />} />
        <Bar
          dataKey={chartKey}
          fill={`var(--color-${chartKey})`}
          radius={4}
          stackId={stackId}
        />
        <ReferenceLine
          y={referenceValue}
          style={{ stroke: "hsl(var(--primary))" }}
          strokeDasharray="3 3"
          fill={"hsl(var(--primary))"}
          label={{
            position: "middle",
            value: axisFormatter(referenceLabel + referenceValue.toFixed(2)),
            fill: "hsl(var(--primary))",
            fontSize: 12,
            dy: -10,
          }}
        />
      </BarChart>
    </ChartContainer>
  );
}
