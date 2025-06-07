"use client";

import { Link, Locale } from "@/navigation";
import React, { memo, useState } from "react";
import { isDeepEqual } from "@/lib/utils";
import { CustomEntityModel, TopUsersSummary, UserDto } from "@/types/dto";
import useFetchStream from "@/hoooks/useFetchStream";
import { BaseError } from "@/types/responses";
import LoadingSpinner from "@/components/common/loading-spinner";

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
  TopChartWrapperTexts,
  TopRankBadge,
} from "@/components/charts/top-chart-wrapper";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
import { Skeleton } from "@/components/ui/skeleton";
import dynamicWithPreload from "@/lib/dynamic-with-preload";
import usePreloadDynamicComponents from "@/hoooks/use-prelod-dynamic-components";

const DynamicUserPieChart = dynamicWithPreload(
  () =>
    import("@/components/charts/user-pie-chart").then(
      (mod) => mod.UserPieChart,
    ),
  {
    loading: () => <Skeleton className="h-[300px] mx-auto aspect-square" />,
  },
);

const DynamicTopChartMeanRelative = dynamicWithPreload(
  () =>
    import("@/components/charts/top-chart-mean-relative").then(
      (mod) => mod.TopChartMeanRelative,
    ),
  {
    loading: () => <Skeleton className="h-[300px] mx-auto aspect-square" />,
  },
);

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
    usePreloadDynamicComponents([
      DynamicUserPieChart,
      DynamicTopChartMeanRelative,
    ]);
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
                <OverflowTextTooltip
                  text={user.email}
                  triggerClassName="max-w-[125px] sm:max-w-[140px] md:max-w-[225px] lg:max-w-[400px]"
                />
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
              <DynamicUserPieChart topSum={topSummary} type={pieType} />
            </div>
          </div>
        </CardContent>
      </MotionCard>
    );
  },
  isDeepEqual,
);

UserCard.displayName = "UserCard";

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
    <DynamicTopChartMeanRelative
      chartKey="amountPerOrder"
      chartLabel={texts.amountPerOrder}
      barData={topSum.totalAmount}
      maxBar={maxAmountPerOrder}
      referenceValue={meanAmountPerOrder}
      referenceLabel={texts.meanAmountPerOrder}
    />
  ),
  isDeepEqual,
);

UserAmountPerOderChart.displayName = "UserAmountPerOderChart";
