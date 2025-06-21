"use client";
import {
  CustomEntityModel,
  PageableResponse,
  PlanResponse,
  TopUsersSummary,
} from "@/types/dto";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import { BaseError } from "@/types/responses";
import LoadingSpinner from "@/components/common/loading-spinner";
import {
  ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { Pie, PieChart } from "recharts";
import { createChartConfig } from "@/components/charts/top-chart-wrapper";

const createChartData = <T extends string | number | symbol>(
  plansMessages: PageableResponse<CustomEntityModel<PlanResponse>>[],
  keySelector: (plan: PlanResponse) => T,
) =>
  Object.values(
    plansMessages.reduce(
      (acc, { content: { content } }) => {
        const key = keySelector(content);
        if (!acc[key]) {
          acc[key] = {
            type: key,
            value: 1,
            fill: `var(--color-${String(key)})`,
          };
        } else {
          acc[key].value += 1;
        }
        return acc;
      },
      {} as Record<
        T,
        {
          type: T;
          value: number;
          fill: string;
        }
      >,
    ),
  );

const typePieChartConfig: ChartConfig = createChartConfig(
  ["VEGAN", "VEGETARIAN", "OMNIVORE"],
  0,
  false,
);
const objectivePieChartConfig: ChartConfig = createChartConfig(
  ["GAIN_MASS", "LOSE_WEIGHT", "MAINTAIN_WEIGHT"],
  0,
  false,
);

const UserPieChart = ({
  topSum,
  type,
}: {
  topSum: TopUsersSummary;
  type: "type" | "objective";
}) => {
  const {
    messages: plansMessages,
    error: planError,
    refetch: refetchUser,
    isFinished: isPlanFinished,
  } = useFetchStream<
    PageableResponse<CustomEntityModel<PlanResponse>>,
    BaseError
  >({
    path: `/plans/byIds`,
    method: "PATCH",
    authToken: true,
    arrayQueryParam: {
      ids: topSum.planValues.map((plan) => plan.toString()),
    },
    body: {
      page: 0,
      size: topSum.planValues.length,
    },
  });

  if (!isPlanFinished || !plansMessages.length) {
    return <LoadingSpinner sectionClassName="min-h-[300px] w-full h-full" />;
  }
  const chartConfig =
    type === "type" ? typePieChartConfig : objectivePieChartConfig;

  const chartData = createChartData(plansMessages, (plan) =>
    type === "type" ? plan.type : plan.objective,
  );

  return (
    <ChartContainer
      config={chartConfig}
      className="h-[300px] mx-auto aspect-square [&_.recharts-pie-label-text]:fill-foreground my-0 p-0"
    >
      <PieChart accessibilityLayer>
        <Pie
          data={chartData}
          outerRadius={70}
          dataKey="value"
          stroke="0"
          nameKey="type"
          label={true}
          labelLine={true}
        />
        <ChartTooltip content={<ChartTooltipContent />} />
        <ChartLegend
          content={
            <ChartLegendContent className="flex-wrap min-h-[90px] items-start" />
          }
        />
      </PieChart>
    </ChartContainer>
  );
};

UserPieChart.displayName = "UserPieChart";

export { UserPieChart };
