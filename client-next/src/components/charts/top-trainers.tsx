"use client";

import TopChartWrapper, {
  createChartConfig,
  TopChartMeanRelative,
  TopChartWrapperTexts,
  TopRankBadge,
} from "@/components/charts/top-chart-wrapper";
import { Link, Locale } from "@/navigation";
import React, { memo, useMemo, useState } from "react";
import {
  CustomEntityModel,
  DietType,
  dietTypes,
  ObjectiveType,
  planObjectives,
  TopTrainersSummary,
  UserDto,
} from "@/types/dto";
import { cn, isDeepEqual } from "@/lib/utils";
import { motion } from "framer-motion";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useFormatter } from "next-intl";
import useFetchStream from "@/hoooks/useFetchStream";
import { BaseError } from "@/types/responses";
import LoadingSpinner from "@/components/common/loading-spinner";
import {
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { Pie, PieChart } from "recharts";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";

export interface TopTrainersTexts {
  trainerCardTexts: TrainerCardTexts;
  topChartWrapperTexts: TopChartWrapperTexts;
  title: string;
}

interface Props {
  texts: TopTrainersTexts;
  locale: Locale;
}

const TopTrainers = memo(({ texts, locale }: Props) => {
  return (
    <TopChartWrapper<TopTrainersSummary>
      texts={texts.topChartWrapperTexts}
      path="/orders/admin/topTrainers"
      locale={locale as Locale}
      processMessage={(ts) => (
        <div key={ts.userId + "topTrainers"}>
          <TrainerCard topSummary={ts} texts={texts.trainerCardTexts} />
        </div>
      )}
      title={texts.title}
    />
  );
}, isDeepEqual);

TopTrainers.displayName = "TopTrainers";
export default TopTrainers;

const MotionCard = motion(Card);

interface TrainerCardTexts {
  userAntent: string;
  topTrainer: string;
  totalAmount: string;
  countPlans: string;
  totalAmountReference: string;
  countPlansReference: string;
  dropDownMenuTexts: DropDownMenuTopTrainersPieSelectTexts;
  typePieChartTitle: string;
  objectivePieChartTitle: string;
  rankLabel: string;
}

const TrainerCard = memo(
  ({
    topSummary,
    texts: {
      topTrainer,
      countPlans,
      countPlansReference,
      totalAmountReference,
      totalAmount,
      dropDownMenuTexts,
      typePieChartTitle,
      objectivePieChartTitle,
      userAntent,
      rankLabel,
    },
  }: {
    topSummary: TopTrainersSummary;
    texts: TrainerCardTexts;
  }) => {
    const formatIntl = useFormatter();
    const {
      messages: users,
      error: userError,
      isFinished: isUserFinished,
    } = useFetchStream<CustomEntityModel<UserDto>, BaseError>({
      path: `/users/${topSummary.userId}`,
      method: "GET",
      authToken: true,
    });

    // console.log(
    //   "STATES AND CACHE",
    //   `/users/${topSummary.userId}`,
    //   users,
    //   isUserFinished,
    //   userError,
    // );

    if (!isUserFinished || !users.length) {
      return <LoadingSpinner sectionClassName="min-h-[575px] w-full h-full" />;
    }
    const user = users[0].content;

    return (
      <MotionCard
        className="flex flex-col min-h-[575px] shadow"
        initial={{ opacity: 0, scale: 0.8 }}
        whileInView={{ opacity: 1, scale: 1 }}
        viewport={{ once: true, amount: "some" }}
        transition={{
          duration: 0.5,
          delay: 0.15,
          type: "spring",
          stiffness: 200,
          damping: 15,
        }}
      >
        <CardHeader>
          <div className="flex flex-col sm:flex-row gap-2 justify-between items-center">
            <CardTitle>
              <Link
                href={`/admin/users/${user.id}`}
                className="hover:underline flex items-center justify-center gap-2"
              >
                <p>{userAntent}</p>
                <OverflowTextTooltip
                  text={user.email}
                  triggerClassName="max-w-[125px] sm:max-w-[140px] md:max-w-[225px] lg:max-w-[400px]"
                />
              </Link>
            </CardTitle>
            <TopRankBadge rank={topSummary.rank} rankLabel={rankLabel} />
          </div>
          <CardDescription>
            {topTrainer} {topSummary.rank}
          </CardDescription>
          <CardContent className="flex-1 grid gap-10 ">
            <div className="flex justify-between">
              <div className="grid place-items-center">
                <p className="text-sm font-medium">{totalAmount}</p>
                <p className="text-2xl font-bold">
                  {formatIntl.number(topSummary.totalAmount, {
                    style: "currency",
                    currency: "EUR",
                    maximumFractionDigits: 2,
                  })}
                </p>
              </div>
              <div className="grid place-items-center">
                <p className="text-sm font-medium">{countPlans}</p>
                <p className="text-2xl font-bold">{topSummary.planCount}</p>
              </div>
            </div>
            <div className="w-full h-full grid gap-4 grid-cols-1 md:grid-cols-2 place-items-center">
              <div className="grid ">
                <p className="text-sm font-medium mb-2">{totalAmount}</p>
                <TopChartMeanRelative
                  chartKey="totalAmount"
                  chartLabel={totalAmount}
                  barData={topSummary.totalAmount}
                  maxBar={topSummary.maxGroupTotal}
                  referenceValue={topSummary.avgGroupTotal}
                  referenceLabel={totalAmountReference}
                />
              </div>
              <div className="grid ">
                <p className="text-sm font-medium mb-2">{countPlans}</p>
                <TopChartMeanRelative
                  chartKey="planCount"
                  chartLabel={countPlans}
                  barData={topSummary.planCount}
                  maxBar={topSummary.maxGroupPlanCount}
                  referenceValue={topSummary.avgGroupPlanCount}
                  referenceLabel={countPlansReference}
                  chartColorNumber={3}
                />
              </div>
              <TopTrainersPieChartWrapper
                type="type"
                chartData={{
                  count: topSummary.typeCounts,
                  amount: topSummary.typeAmounts,
                  avg: topSummary.typeAvgs,
                }}
                texts={{
                  title: typePieChartTitle,
                  dropDownMenuTexts,
                }}
              />
              <TopTrainersPieChartWrapper
                type="objective"
                chartData={{
                  count: topSummary.objectiveCounts,
                  amount: topSummary.objectiveAmounts,
                  avg: topSummary.objectiveAvgs,
                }}
                texts={{
                  title: objectivePieChartTitle,
                  dropDownMenuTexts,
                }}
                offset={6}
              />
            </div>
          </CardContent>
        </CardHeader>
      </MotionCard>
    );
  },
  isDeepEqual,
);

TrainerCard.displayName = "TrainerCard";

interface BasePieChartProps<T extends "type" | "objective"> {
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
type PieChartOptions = "count" | "amount" | "avg";
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

    const numberOfLabels = (type === "type" ? dietTypes : planObjectives)
      .length;

    return (
      <ChartContainer
        config={chartConfig}
        className="min-h-[300px] h-full mx-auto aspect-square [&_.recharts-pie-label-text]:fill-foreground my-0 p-0 "
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
              <ChartLegendContent
                className={cn(numberOfLabels > 3 && "grid grid-cols-3 gap-1")}
              />
            }
          />
        </PieChart>
      </ChartContainer>
    );
  },
  isDeepEqual,
);

TopTrainersPieChart.displayName = "TopTrainersPieChart";

interface DropDownMenuTopTrainersPieSelectTexts {
  countLabel: string;
  totalAmountLabel: string;
  averageAmountLabel: string;
}
interface DropDownMenuTopTrainersPieSelectProps
  extends DropDownMenuTopTrainersPieSelectTexts {
  radioOption: PieChartOptions;
  onRadioOptionChange: (option: PieChartOptions) => void;
}
export function DropDownMenuTopTrainersPieSelect({
  radioOption,
  onRadioOptionChange,
  averageAmountLabel,
  countLabel,
  totalAmountLabel,
}: DropDownMenuTopTrainersPieSelectProps) {
  const label =
    radioOption === "count"
      ? countLabel
      : radioOption === "amount"
        ? totalAmountLabel
        : averageAmountLabel;

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" className="w-44">
          {label}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56">
        <DropdownMenuRadioGroup
          value={radioOption}
          onValueChange={(e) => onRadioOptionChange(e as PieChartOptions)}
        >
          <DropdownMenuRadioItem value={"count"}>
            {countLabel}
          </DropdownMenuRadioItem>
          <DropdownMenuRadioItem value={"amount"}>
            {totalAmountLabel}
          </DropdownMenuRadioItem>
          <DropdownMenuRadioItem value={"avg"}>
            {averageAmountLabel}
          </DropdownMenuRadioItem>
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

interface TopTrainersPieChartWrapperTexts {
  title: string;
  dropDownMenuTexts: DropDownMenuTopTrainersPieSelectTexts;
}

interface TopTrainersPieChartWrapperProps<T extends "type" | "objective">
  extends BasePieChartProps<T> {
  chartData: Record<
    PieChartOptions,
    Partial<Record<T extends "type" ? DietType : ObjectiveType, number>>
  >;
  texts: TopTrainersPieChartWrapperTexts;
}

const TopTrainersPieChartWrapper = memo(
  <T extends "type" | "objective">({
    type,
    chartData,
    offset = 0,
    texts: { dropDownMenuTexts, title },
  }: TopTrainersPieChartWrapperProps<T>) => {
    const [radioOption, setRadioOption] = useState<PieChartOptions>("count");

    return (
      <div className="w-full flex flex-col gap-2 items-center justify-end">
        <div className="flex items-center justify-center gap-5 md:gap-2 w-full ">
          <h2 className="font-semibold text-sm tracking-tight text-center">
            {title}{" "}
          </h2>
          <DropDownMenuTopTrainersPieSelect
            radioOption={radioOption}
            onRadioOptionChange={setRadioOption}
            {...dropDownMenuTexts}
          />
        </div>
        <TopTrainersPieChart
          type={type}
          chartData={chartData[radioOption]}
          offset={offset}
        />
      </div>
    );
  },
  isDeepEqual,
);

TopTrainersPieChartWrapper.displayName = "TopTrainersPieChartWrapper";
