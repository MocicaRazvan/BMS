"use client";

import { Link, Locale } from "@/navigation";
import React, { memo, useState } from "react";
import { isDeepEqual } from "@/lib/utils";
import {
  CustomEntityModel,
  PageableResponse,
  PlanResponse,
  TopUsersSummary,
  UserDto,
} from "@/types/dto";
import useFetchStream from "@/hoooks/useFetchStream";
import { BaseError } from "@/types/responses";
import LoadingSpinner from "@/components/common/loading-spinner";
import {
  ChartConfig,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { Pie, PieChart } from "recharts";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useFormatter } from "next-intl";
import { Switch } from "@/components/ui/switch";
import { motion } from "framer-motion";
import TopChartWrapper, {
  createChartConfig,
  TopChartMeanRelative,
  TopChartWrapperTexts,
  TopRankBadge,
} from "@/components/charts/top-chart-wrapper";

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
            fill: `var(--color-${String(key).toLowerCase()})`,
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

const typePieChartConfig: ChartConfig = createChartConfig([
  "VEGAN",
  "VEGETARIAN",
  "OMNIVORE",
]);
const objectivePieChartConfig: ChartConfig = createChartConfig([
  "GAIN_MASS",
  "LOSE_WEIGHT",
  "MAINTAIN_WEIGHT",
]);

export interface TopUsersTexts {
  userCardTexts: UserCardTexts;
  userAmountPerOderChartTexts: UserAmountPerOderChartTexts;
  topChartWrapperTexts: TopChartWrapperTexts;
  title: string;
}

interface Props {
  locale: Locale;
  texts: TopUsersTexts;
}
const TopUsers = memo(
  ({
    locale,
    texts: {
      userAmountPerOderChartTexts,
      userCardTexts,
      topChartWrapperTexts,
      title,
    },
  }: Props) => {
    return (
      <TopChartWrapper<TopUsersSummary>
        texts={topChartWrapperTexts}
        path="/orders/admin/topUsers"
        locale={locale}
        processMessage={(ts) => (
          <div key={ts.userId + "topUsers"}>
            <UserCard
              topSummary={ts}
              texts={userCardTexts}
              amountTexts={userAmountPerOderChartTexts}
            />
          </div>
        )}
        title={title}
      />
    );
  },
  isDeepEqual,
);

TopUsers.displayName = "TopUsers";

export default TopUsers;

interface UserCardTexts {
  userAntent: string;
  totalAmount: string;
  orders: string;
  plans: string;
  rank: string;
  amountPerOrderTitle: string;
  planDistributionTitle: string;
  topBuyer: string;
  objective: string;
  type: string;
}

const MotionCard = motion(Card);

const UserCard = memo(
  ({
    topSummary,
    texts: {
      userAntent,
      topBuyer,
      rank,
      totalAmount,
      orders,
      plans,
      amountPerOrderTitle,
      planDistributionTitle,
      type,
      objective,
    },
    amountTexts,
  }: {
    topSummary: TopUsersSummary;
    texts: UserCardTexts;
    amountTexts: UserAmountPerOderChartTexts;
  }) => {
    const formatIntl = useFormatter();
    const [pieType, setPieType] = useState<"type" | "objective">("type");
    const {
      messages: users,
      error: userError,
      refetch: refetchUser,
      isFinished: isUserFinished,
    } = useFetchStream<CustomEntityModel<UserDto>, BaseError>({
      path: `/users/${topSummary.userId}`,
      method: "GET",
      authToken: true,
    });

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
          <div className="flex justify-between items-center">
            <CardTitle>
              <Link
                href={`/admin/users/${user.id}`}
                className="hover:underline flex items-center justify-center gap-2"
              >
                <p>{userAntent}</p>
                <p>{user.email}</p>
              </Link>
            </CardTitle>
            <TopRankBadge rank={topSummary.rank} rankLabel={rank} />
          </div>
          <CardDescription>
            {topBuyer} {topSummary.rank}
          </CardDescription>
        </CardHeader>
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
              <p className="text-sm font-medium">{orders}</p>
              <p className="text-2xl font-bold">{topSummary.ordersNumber}</p>
            </div>
            <div className="grid place-items-center">
              <p className="text-sm font-medium">{plans}</p>
              <p className="text-2xl font-bold">{topSummary.plansNumber}</p>
            </div>
          </div>
          <div className="w-full h-full grid gap-4 grid-cols-1 md:grid-cols-2 place-items-center">
            <div className="grid ">
              <p className="text-sm font-medium mb-2">{amountPerOrderTitle}</p>
              <UserAmountPerOderChart
                topSum={topSummary}
                meanAmountPerOrder={topSummary.avgGroupTotal}
                maxAmountPerOrder={topSummary.maxGroupTotal}
                texts={amountTexts}
              />
            </div>
            <div className="grid md:place-items-end">
              <div className="flex items-start justify-center gap-2.5">
                <p className="text-sm font-medium mb-2 flex items-center justify-center gap-3">
                  {planDistributionTitle}
                </p>
                <div className="flex items-center gap-1.5">
                  <p className="text-sm">{objective}</p>
                  <Switch
                    checked={pieType === "type"}
                    onCheckedChange={(v) =>
                      setPieType(v ? "type" : "objective")
                    }
                    className="h-5 w-10"
                    thumbClassName="h-4 w-4"
                  />
                  <p className="text-sm">{type}</p>
                </div>
              </div>
              <UserPieChart topSum={topSummary} type={pieType} />
            </div>
          </div>
        </CardContent>
      </MotionCard>
    );
  },
  isDeepEqual,
);

UserCard.displayName = "UserCard";

const UserPieChart = memo(
  ({
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
        className="h-[300px] mx-auto aspect-square [&_.recharts-pie-label-text]:fill-foreground my-0 p-0 "
      >
        <PieChart>
          <Pie
            data={chartData}
            outerRadius={70}
            dataKey="value"
            stroke="0"
            nameKey="type"
            label={true}
          />
          <ChartTooltip content={<ChartTooltipContent />} />
        </PieChart>
      </ChartContainer>
    );
  },
  isDeepEqual,
);

UserPieChart.displayName = "UserPieChart";

export interface UserAmountPerOderChartTexts {
  amountPerOrder: string;
  meanAmountPerOrder: string;
}

const UserAmountPerOderChart = memo(
  ({
    topSum,
    meanAmountPerOrder,
    maxAmountPerOrder,
    texts,
  }: {
    topSum: TopUsersSummary;
    texts: UserAmountPerOderChartTexts;
    meanAmountPerOrder: number;
    maxAmountPerOrder: number;
  }) => (
    <TopChartMeanRelative
      chartKey="amountPerOrder"
      chartLabel={texts.amountPerOrder}
      barData={topSum.totalAmount / topSum.ordersNumber}
      maxBar={maxAmountPerOrder}
      referenceValue={meanAmountPerOrder}
      referenceLabel={texts.meanAmountPerOrder}
    />
  ),
  isDeepEqual,
);

UserAmountPerOderChart.displayName = "UserAmountPerOderChart";
