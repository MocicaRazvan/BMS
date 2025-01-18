"use client";

import {
  DateRangeParams,
  DateRangePicker,
  DateRangePickerTexts,
} from "@/components/ui/date-range-picker";
import { Link, Locale } from "@/navigation";
import { format, subMonths } from "date-fns";
import React, { memo, Suspense, useMemo, useState } from "react";
import { cn, isDeepEqual } from "@/lib/utils";
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
import {
  Bar,
  BarChart,
  Pie,
  PieChart,
  ReferenceLine,
  XAxis,
  YAxis,
} from "recharts";
import { ro } from "date-fns/locale";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import Lottie from "react-lottie-player";
import noResultsLottie from "../../../public/lottie/noResults.json";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { useFormatter } from "next-intl";
import { Switch } from "@/components/ui/switch";
import { motion } from "framer-motion";

const now = new Date();
const oneMonthAgo = subMonths(now, 1);
const dateFormat = "dd-MM-yyyy";
const formattedNow = format(now, dateFormat);
const formattedOneMonthAgo = format(oneMonthAgo, dateFormat);
const topOptions = Array.from({ length: 10 }, (_, i) => i + 1);

const createChartConfig = (arr: string[]) =>
  arr.reduce((acc, t, i) => {
    acc[t.toLowerCase()] = {
      label: t,
      color: `hsl(var(--chart-${i + 1}))`,
    };
    return acc;
  }, {} as ChartConfig);

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
  dateRangePickerTexts: DateRangePickerTexts;
  topLabel: string;
  periodLabel: string;
  noResults: string;
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
      dateRangePickerTexts,
      userAmountPerOderChartTexts,
      userCardTexts,
      topLabel,
      periodLabel,
      noResults,
      title,
    },
  }: Props) => {
    const [top, setTop] = useState<string>("2");
    const [dateRange, setDateRange] = useState<DateRangeParams>({
      from: formattedOneMonthAgo,
      to: formattedNow,
    });

    const { messages, error, isFinished } = useFetchStream<TopUsersSummary>({
      path: "/orders/admin/topUsers",
      authToken: true,
      method: "GET",
      queryParams: {
        ...dateRange,
        top,
      },
    });
    const dateRangePicker = useMemo(
      () => (
        <DateRangePicker
          onUpdate={({ range: { from, to } }) =>
            setDateRange({
              from: format(from, dateFormat),
              to: format(to || from, dateFormat),
            })
          }
          align="center"
          locale={locale === "ro" ? ro : undefined}
          defaultPreset={"lastMonth"}
          showCompare={false}
          {...dateRangePickerTexts}
        />
      ),
      [dateRangePickerTexts, locale],
    );

    const noMessageOrError = !messages.length || error !== null;

    return (
      <motion.div
        className="w-full h-full p-4 "
        initial={{ opacity: 0 }}
        whileInView={{ opacity: 1 }}
        viewport={{ once: true, amount: "some" }}
      >
        <h2 className="text-2xl lg:text-3xl font-bold tracking-tight capitalize inline">
          {title}
        </h2>
        <div className="flex items-center justify-around w-full  mb-12">
          <div className="flex items-center gap-2">
            <Label className="text-lg font-semibold">{periodLabel}</Label>
            {dateRangePicker}
          </div>
          <div className="flex items-center gap-2">
            <Label className="text-lg font-semibold" htmlFor="top-select">
              {topLabel}
            </Label>
            <Select value={top} onValueChange={(v) => setTop(v)}>
              <SelectTrigger className="w-36" id="top-select">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {topOptions.map((option) => (
                  <SelectItem
                    key={option + "select"}
                    value={`${option}`}
                    className={cn(
                      "cursor-pointer capitalize",
                      messages.length === option && "text-amber",
                    )}
                  >
                    {option}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
        {!isFinished ? (
          <LoadingSpinner sectionClassName="min-h-[450px] md:min-h-[650px] w-full h-full pb-10" />
        ) : noMessageOrError ? (
          <div className="block w-full h-full">
            <h2 className="text-4xl tracking-tighter font-bold w-full max-w-3xl max-h-[550px] mx-auto">
              <p className="text-center">{noResults}</p>
              <Suspense
                fallback={<div className="md:w-1/3 md:h-1/3 mx-auto" />}
              >
                <Lottie
                  animationData={noResultsLottie}
                  loop
                  className="md:w-1/3 md:h-1/3 mx-auto"
                  play
                />
              </Suspense>
            </h2>
          </div>
        ) : (
          <div className={"grid gap-4 md:grid-cols-2 min-h-[500px]"}>
            {messages
              .sort((a, b) => a.rank - b.rank)
              .map((ts) => (
                <div key={ts.userId}>
                  <UserCard
                    topSummary={ts}
                    texts={userCardTexts}
                    amountTexts={userAmountPerOderChartTexts}
                  />
                </div>
              ))}
          </div>
        )}
      </motion.div>
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
        key={topSummary.userId}
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
                className="hover:underline"
              >
                {userAntent} {user.email}
              </Link>
            </CardTitle>
            <Badge variant={topSummary.rank === 1 ? "success" : "default"}>
              {rank}
              {` #`}
              {topSummary.rank}
            </Badge>
          </div>
          <CardDescription>
            {topBuyer} {topSummary.rank}
          </CardDescription>
        </CardHeader>
        <CardContent className="flex-1 grid gap-10 ">
          <div className="flex justify-between">
            <div>
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
    <ChartContainer
      config={{
        amountPerOrder: {
          label: texts.amountPerOrder,
          color: "hsl(var(--chart-6))",
        },
      }}
      className="h-[300px] mx-auto aspect-square"
    >
      <BarChart
        data={[
          {
            name: texts.amountPerOrder,
            amountPerOrder: (topSum.totalAmount / topSum.ordersNumber).toFixed(
              2,
            ),
          },
        ]}
      >
        <XAxis dataKey="name" />
        <YAxis domain={[0, maxAmountPerOrder + 5]} />
        <ChartTooltip content={<ChartTooltipContent hideLabel={true} />} />
        <Bar
          dataKey="amountPerOrder"
          fill="var(--color-amountPerOrder)"
          radius={4}
        />
        <ReferenceLine
          y={meanAmountPerOrder}
          style={{ stroke: "hsl(var(--primary))" }}
          strokeDasharray="3 3"
          fill={"hsl(var(--primary))"}
          label={{
            position: "middle",
            value: texts.meanAmountPerOrder + meanAmountPerOrder.toFixed(2),
            fill: "hsl(var(--primary))",
            fontSize: 12,
            dy: -10,
          }}
        />
      </BarChart>
    </ChartContainer>
  ),
  isDeepEqual,
);

UserAmountPerOderChart.displayName = "UserAmountPerOderChart";
