"use client";
import { getNutritionalConversionFactorByName, Macro } from "@/types/responses";
import {
  ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { ComponentProps, memo, useMemo } from "react";
import { Label, Pie, PieChart } from "recharts";
import { motion } from "framer-motion";

export interface MacroChartElement {
  macro: Macro | "salt";
  value: number;
}
interface Props extends ComponentProps<"div"> {
  items: MacroChartElement[];
  texts: IngredientPieChartTexts;
  strokeWith?: number;
  innerRadius?: number;
}
export interface IngredientPieChartTexts {
  macroLabel: string;
  proteinLabel: string;
  fatLabel: string;
  carbohydratesLabel: string;
  caloriesLabel: string;
  saltLabel: string;
}

const getChartConfig = ({
  macroLabel,
  fatLabel,
  proteinLabel,
  carbohydratesLabel,
  saltLabel,
}: IngredientPieChartTexts) =>
  ({
    macro: {
      label: macroLabel,
    },
    protein: {
      label: proteinLabel,
      color: "hsl(var(--chart-1))",
    },
    fat: {
      label: fatLabel,
      color: "hsl(var(--chart-2))",
    },
    carbohydrates: {
      label: carbohydratesLabel,
      color: "hsl(var(--chart-3))",
    },
    salt: {
      label: saltLabel,
      color: "hsl(var(--chart-4))",
    },
  }) satisfies ChartConfig;

export const IngredientMacrosPieChart = memo(
  ({ items, texts, strokeWith = 6, innerRadius = 100, ...props }: Props) => {
    const chartData = useMemo(
      () =>
        items.map((item) => ({
          ...item,
          fill: `var(--color-${item.macro})`,
        })),
      [items],
    );
    const totalCalories = useMemo(
      () =>
        Math.ceil(
          items.reduce(
            (acc, cur) =>
              acc +
              cur.value *
                (cur.macro === "salt"
                  ? 0
                  : getNutritionalConversionFactorByName(cur.macro)),
            0,
          ),
        ),
      [items],
    );
    console.log("totalCalories", items);
    const chartConfig = getChartConfig(texts);

    return (
      <ChartContainer
        config={chartConfig}
        className="mx-auto aspect-square  max-h-[350px] lg:max-h-[400px]"
        {...props}
      >
        <PieChart accessibilityLayer>
          <ChartTooltip
            cursor={false}
            content={<ChartTooltipContent className="gap-2" />}
          />
          <Pie
            data={chartData}
            dataKey="value"
            nameKey="macro"
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
                        className="fill-foreground text-3xl font-bold"
                      >
                        {totalCalories.toLocaleString()}
                      </tspan>
                      <tspan
                        x={viewBox.cx}
                        y={(viewBox.cy || 0) + 24}
                        className="fill-muted-foreground"
                      >
                        {texts.macroLabel}
                      </tspan>
                    </motion.text>
                  );
                }
              }}
            />
          </Pie>
          <ChartLegend content={<ChartLegendContent nameKey={"macro"} />} />
        </PieChart>
      </ChartContainer>
    );
  },
);

IngredientMacrosPieChart.displayName = "IngredientMacrosPieChart";
export default IngredientMacrosPieChart;
