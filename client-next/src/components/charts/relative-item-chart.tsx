"use client";

import {
  Bar,
  BarChart,
  Label,
  LabelList,
  Pie,
  PieChart,
  ResponsiveContainer,
  XAxis,
  YAxis,
} from "recharts";
import React, { ComponentProps } from "react";
import {
  ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { motion } from "framer-motion";

export type RelativeItems =
  | "posts"
  | "orders"
  | "recipes"
  | "plans"
  | "comments";

interface RelativeItemBarChartProps {
  month: string;
  year: string;
  count: number;
  color: string;
  fill: string;
  maxCount: number;
}

export function RelativeItemBarChart({
  month,
  year,
  count,
  color,
  fill,
  maxCount,
}: RelativeItemBarChartProps) {
  return (
    <div className="h-8">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart
          layout="vertical"
          margin={{ left: 0, top: 0, right: 0, bottom: 0 }}
          data={[{ date: `${month}/${year}`, count: count }]}
        >
          <XAxis type="number" domain={[0, maxCount]} hide />
          <YAxis dataKey="date" type="category" tickCount={1} hide />
          <Bar dataKey="count" fill={color} radius={4} barSize="100%">
            <LabelList
              position="insideLeft"
              dataKey="date"
              offset={8}
              fontSize={12}
              fill={fill}
            />
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

export interface RelativeItemPieChartBaseProps extends ComponentProps<"div"> {
  strokeWith?: number;
  innerRadius?: number;
}

interface RelativeItemPieChartProps extends RelativeItemPieChartBaseProps {
  chartConfig: ChartConfig;
  chartData: {
    item: RelativeItems;
    value: number;
    fill: string;
  }[];
  itemsText: string;
  totalItems: number;
}

export function RelativeItemPieChart({
  chartConfig,
  chartData,
  strokeWith = 2,
  innerRadius = 6,
  itemsText,
  totalItems,
  ...props
}: RelativeItemPieChartProps) {
  return (
    <ChartContainer
      config={chartConfig}
      className="mx-auto aspect-square h-[255px]"
      {...props}
    >
      <PieChart>
        <ChartTooltip
          cursor={false}
          content={<ChartTooltipContent className="gap-2" />}
        />
        <Pie
          data={chartData}
          dataKey="value"
          nameKey="item"
          innerRadius={innerRadius}
          strokeWidth={strokeWith}
        >
          <Label
            content={({ viewBox }) => {
              if (viewBox && "cx" in viewBox && "cy" in viewBox) {
                return (
                  <motion.text
                    x={viewBox.cx}
                    y={viewBox.cy}
                    textAnchor="middle"
                    dominantBaseline="middle"
                    initial={{ opacity: 0, scale: 0 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ duration: 0.5 }}
                  >
                    <tspan
                      x={viewBox.cx}
                      y={viewBox.cy}
                      className="fill-foreground text-2xl font-bold"
                    >
                      {totalItems.toLocaleString()}
                    </tspan>
                    <tspan
                      x={viewBox.cx}
                      y={(viewBox.cy || 0) + 24}
                      className="fill-muted-foreground"
                    >
                      {itemsText}
                    </tspan>
                  </motion.text>
                );
              }
            }}
          />
        </Pie>
        <ChartLegend
          content={
            <ChartLegendContent
              nameKey="item"
              className="w-full flex flex-wrap items-center justify-center"
            />
          }
        />
      </PieChart>
    </ChartContainer>
  );
}
