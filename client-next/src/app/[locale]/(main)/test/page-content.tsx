"use client";

import RecommendationList, {
  RecommendationListTexts,
} from "@/components/recomandation/recommendation-list";
import PostItemRenderer, {
  PostItemRendererTexts,
} from "@/components/recomandation/post-item-renderer";
import PlanItemRenderer, {
  PlanItemRendererTexts,
} from "@/components/recomandation/plan-item-renderer";
import { usePlansSubscription } from "@/context/subscriptions-context";
import useFetchStream from "@/hoooks/useFetchStream";
import {
  MonthlyOrderSummary,
  MonthlyOrderSummaryObjective,
  MonthlyOrderSummaryObjectiveType,
  MonthlyOrderSummaryType,
} from "@/types/dto";
import {
  ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import {
  Bar,
  BarChart,
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
import {
  MonthPicker,
  MonthPickerSelect,
} from "@/components/common/month-picker";
import { useMemo, useState } from "react";
import { format, subMonths, subYears } from "date-fns";
import PlanCharacteristic, {
  DropDownMenuCountTotalPlanCharacteristicSelect,
  DropDownMenuTypePlanCharacteristicSelect,
  PlanCharacteristicKey,
  PlanCharacteristicOption,
  PlanCharacteristicTexts,
  PlanCharacteristicWrapper,
  PlanCharacteristicWrapperTexts,
} from "@/components/charts/plan-charctersitic";
import { satisfies } from "semver";
import { v4 as uuidv4 } from "uuid";

interface Props {
  texts: PlanCharacteristicWrapperTexts;
}
const chartConfig = {
  count: {
    label: "count",
    color: `hsl(var(--chart-1))`,
  },
  totalAmount: {
    label: "total",
    color: `hsl(var(--chart-2))`,
  },
  averageAmount: {
    label: "avg",
    color: `hsl(var(--chart-3))`,
  },
} satisfies ChartConfig;
export default function PageContent({ texts }: Props) {
  const stackId = uuidv4();

  const { messages } = useFetchStream<MonthlyOrderSummaryObjectiveType>({
    path: "/orders/trainer/countAndAmount/objectiveType/1",
    authToken: true,
    queryParams: {
      month: "01-08-2024",
    },
  });
  return (
    <div className="h-full w-full p-10 mt-20">
      {" "}
      <ChartContainer
        config={chartConfig}
        className="aspect-auto h-[450px] w-full "
      >
        <ScatterChart accessibilityLayer>
          <CartesianGrid />
          <XAxis type="category" dataKey="objective" name="objective" />
          <YAxis type="category" dataKey="type" name="type" />
          <ZAxis
            type="number"
            dataKey="totalAmount"
            name="totalAmount"
            domain={["auto", "auto"]}
            range={[250, 2250]}
            scale="sqrt"
          />
          <ChartTooltip
            cursor={false}
            content={<ChartTooltipContent indicator="dot" />}
          />
          <Scatter
            name="totalAmount"
            data={messages}
            dataKey="totalAmount"
            fill="var(--color-totalAmount)"
          >
            <LabelList
              position="top"
              offset={12}
              className="fill-foreground text-[15px]"
              fontSize={12}
              dataKey="totalAmount"
              formatter={(value: number | string) =>
                typeof value === "number" && !Number.isInteger(value)
                  ? value.toFixed(2)
                  : value
              }
            />
          </Scatter>{" "}
          <ChartLegend content={<ChartLegendContent />} />
        </ScatterChart>
      </ChartContainer>
    </div>
  );
}
