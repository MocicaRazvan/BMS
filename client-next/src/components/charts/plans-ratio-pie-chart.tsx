"use client";
import { ChartConfig, ChartContainer } from "@/components/ui/chart";
import {
  Label,
  PolarGrid,
  PolarRadiusAxis,
  RadialBar,
  RadialBarChart,
} from "recharts";
import React from "react";

const chartConfig = {
  ratio: {
    label: "ratio",
  },
  plan: {
    label: "plan",
    color: "hsl(var(--chart-6))",
  },
} satisfies ChartConfig;

export function RatioPieChart({
  chartData,
  innerLabel,
}: {
  chartData: { ratio: number; fill: string }[];
  innerLabel: string;
}) {
  const endAngle = chartData[0].ratio * 360;
  const percentCharRatio = chartData[0].ratio * 100;
  return (
    <ChartContainer
      config={chartConfig}
      className="mx-auto aspect-square h-[300px]"
    >
      <RadialBarChart
        accessibilityLayer
        data={chartData}
        startAngle={0}
        endAngle={endAngle}
        innerRadius={80}
        outerRadius={110}
      >
        <PolarGrid
          gridType="circle"
          radialLines={false}
          stroke="none"
          className="first:fill-muted last:fill-background"
          polarRadius={[86, 74]}
        />
        <RadialBar dataKey="ratio" background cornerRadius={10} />
        <PolarRadiusAxis tick={false} tickLine={false} axisLine={false}>
          <Label
            content={({ viewBox }) => {
              if (viewBox && "cx" in viewBox && "cy" in viewBox) {
                return (
                  <text
                    x={viewBox.cx}
                    y={viewBox.cy}
                    textAnchor="middle"
                    dominantBaseline="middle"
                  >
                    <tspan
                      x={viewBox.cx}
                      y={viewBox.cy}
                      className="fill-foreground text-2xl font-bold"
                    >
                      {percentCharRatio % 1 === 0
                        ? percentCharRatio.toFixed(0)
                        : percentCharRatio.toFixed(2)}
                      {"%"}
                    </tspan>
                    <tspan
                      x={viewBox.cx}
                      y={(viewBox.cy || 0) + 24}
                      className="fill-muted-foreground"
                    >
                      {innerLabel}
                    </tspan>
                  </text>
                );
              }
            }}
          />
        </PolarRadiusAxis>
      </RadialBarChart>
    </ChartContainer>
  );
}
