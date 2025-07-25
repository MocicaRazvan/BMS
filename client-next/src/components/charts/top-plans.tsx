"use client";

import TopChartWrapper, {
  TopChartWrapperTexts,
  TopRankBadge,
} from "@/components/charts/top-chart-wrapper";
import { Link, Locale } from "@/navigation/navigation";
import { memo, useMemo } from "react";
import {
  PlanResponse,
  ResponseWithUserDtoEntity,
  TopPlansSummary,
} from "@/types/dto";
import { motion } from "framer-motion";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import { BaseError } from "@/types/responses";
import LoadingSpinner from "@/components/common/loading-spinner";
import DietBadge from "@/components/common/diet-badge";
import { WithUser } from "@/lib/user";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import { Skeleton } from "@/components/ui/skeleton";
import dynamicWithPreload from "@/lib/dynamic-with-preload";
import usePreloadDynamicComponents from "@/hoooks/use-prelod-dynamic-components";
import { isDeepEqual } from "@/lib/utils";

export interface TopPlansTexts {
  topChartWrapperTexts: TopChartWrapperTexts;
  planCardTexts: PlanCardTexts;
  title: string;
}

interface Props {
  texts: TopPlansTexts;
  locale: Locale;
  path: string;
}

const DynamicTopChartMeanRelative = dynamicWithPreload(
  () =>
    import("@/components/charts/top-chart-mean-relative").then(
      (mod) => mod.TopChartMeanRelative,
    ),
  {
    loading: () => <Skeleton className="h-[300px] mx-auto aspect-square" />,
  },
);

const DynamicRatioPieChart = dynamicWithPreload(
  () =>
    import("@/components/charts/plans-ratio-pie-chart").then(
      (mod) => mod.RatioPieChart,
    ),
  {
    loading: () => <Skeleton className="h-[300px] mx-auto aspect-square" />,
  },
);

export const TopPlans = memo(
  ({
    locale,
    path,
    texts: { topChartWrapperTexts, title, planCardTexts },
  }: Props) => {
    usePreloadDynamicComponents([
      DynamicTopChartMeanRelative,
      DynamicRatioPieChart,
    ]);
    const { authUser } = useAuthUserMinRole();

    return (
      <TopChartWrapper<TopPlansSummary>
        texts={topChartWrapperTexts}
        path={path}
        locale={locale}
        processMessage={(ts) => (
          <div key={ts.planId}>
            <PlanCard
              topSummary={ts}
              texts={planCardTexts}
              authUser={authUser}
            />
          </div>
        )}
        title={title}
      />
    );
  },
  isDeepEqual,
);
TopPlans.displayName = "TopPlans";
export default TopPlans;

const MotionCard = motion(Card);

interface PlanCardTexts {
  planAntet: string;
  topPlan: string;
  dietType: string;
  madeBy: string;
  numberOfPurchases: string;
  meanNumberOfPurchases: string;
  ratioLabel: string;
  rankLabel: string;
  ordersCount: string;
}

interface PlanCardProps extends WithUser {
  topSummary: TopPlansSummary;
  texts: PlanCardTexts;
}

const PlanCard = ({ topSummary, texts, authUser }: PlanCardProps) => {
  const {
    messages: plans,
    error,
    isFinished: isPlansFinished,
  } = useFetchStream<ResponseWithUserDtoEntity<PlanResponse>, BaseError>({
    path: `/plans/withUser/${topSummary.planId}`,
    method: "GET",
    authToken: true,
  });

  const ratioChartData = useMemo(
    () => [
      {
        ratio: topSummary.ratio,
        fill: "var(--color-plan)",
      },
    ],
    [topSummary.ratio],
  );

  if (!isPlansFinished || !plans.length) {
    return <LoadingSpinner sectionClassName="min-h-[575px] w-full h-full" />;
  }

  const plan = plans[0].model.content;
  const user = plans[0].user;

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
              href={`/${authUser.role === "ROLE_ADMIN" ? "admin" : "trainer"}/plans/single/${plan.id}`}
              className="hover:underline flex items-center justify-center gap-2"
            >
              <p>{texts.planAntet}</p>
              <OverflowTextTooltip
                text={plan.title}
                triggerClassName="max-w-[125px] sm:max-w-[230px] lg:max-w-[400px]"
              />
            </Link>
          </CardTitle>
          <TopRankBadge rank={topSummary.rank} rankLabel={texts.rankLabel} />
        </div>
        <CardDescription>
          {texts.topPlan} {topSummary.rank}
        </CardDescription>
        <CardContent className="flex-1 grid gap-10 pt-1.5">
          <div className="flex justify-between">
            <div className="grid place-items-center">
              <p className="text-sm font-medium">{texts.ordersCount}</p>
              <p className="text-2xl font-bold">{topSummary.count}</p>
            </div>
            <div className="grid place-items-center">
              <p className="text-sm font-medium">{texts.dietType}</p>
              <DietBadge dietType={plan.type} pClassName="text-xs px-2 py-1" />
            </div>
            <div className="grid place-items-center">
              <p className="text-sm font-medium">{texts.madeBy}</p>
              <Link
                href={`/admin/users/${user.id}`}
                className="hover:underline"
              >
                {user.email}
              </Link>
            </div>
          </div>
          <div className="w-full h-full grid gap-4 grid-cols-1 md:grid-cols-2 place-items-center">
            <div className="grid ">
              <p className="text-sm font-medium mb-2">
                {texts.numberOfPurchases}
              </p>
              <DynamicTopChartMeanRelative
                chartKey="count"
                chartLabel={texts.numberOfPurchases}
                barData={topSummary.count}
                maxBar={topSummary.maxGroupCount}
                referenceValue={topSummary.avgGroupCount}
                referenceLabel={texts.meanNumberOfPurchases}
                chartColorNumber={2}
                maxOffset={4}
              />
            </div>
            <div className="grid md:place-items-end">
              <DynamicRatioPieChart
                innerLabel={texts.ratioLabel}
                chartData={ratioChartData}
              />
            </div>
          </div>
        </CardContent>
      </CardHeader>
    </MotionCard>
  );
};
PlanCard.displayName = "PlanCard";
