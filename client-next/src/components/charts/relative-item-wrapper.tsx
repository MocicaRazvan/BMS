"use client";

import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { WithUser } from "@/lib/user";
import {
  CustomEntityModel,
  MonthlyEntityGroup,
  WithUserDto,
} from "@/types/dto";
import { Progress } from "@/components/ui/progress";
import { cn } from "@/lib/utils";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import { useEffect, useMemo, useState } from "react";
import Loader from "@/components/ui/spinner";
import { ChartConfig } from "@/components/ui/chart";
import { useDebounce } from "@/components/ui/multiple-selector";
import {
  RelativeItemPieChartBaseProps,
  RelativeItems,
} from "@/components/charts/relative-item-chart";
import dynamicWithPreload from "@/lib/dynamic-with-preload";
import { Skeleton } from "@/components/ui/skeleton";
import usePreloadDynamicComponents from "@/hoooks/use-prelod-dynamic-components";
import { relativeItems } from "@/types/constants";

const DynamicRelativeItemBarChart = dynamicWithPreload(
  () =>
    import("@/components/charts/relative-item-chart").then(
      async (mod) => mod.RelativeItemBarChart,
    ),
  {
    loading: () => <Skeleton className="h-8" />,
  },
);

const DynamicRelativeItemPieChart = dynamicWithPreload(
  () =>
    import("@/components/charts/relative-item-chart").then(
      (mod) => mod.RelativeItemPieChart,
    ),
  {
    loading: () => (
      <div className="flex justify-center h-[255px]">
        <Loader />
      </div>
    ),
  },
);

export interface RelativeItemTexts {
  type: string;
  description: string;
  month: string;
  increase: string;
  decrease: string;
  updateAboveCount?: (count: number) => void;
  updateAllFinished?: (finished: boolean) => void;
}

interface Props extends WithUser, RelativeItemTexts {
  basePath: string;
}

type StateKey = "cur" | "prev";

export default function RelativeItem<M extends WithUserDto>({
  authUser,
  basePath,
  month,
  decrease,
  description,
  increase,
  type,
  updateAboveCount,
  updateAllFinished,
}: Props) {
  const currMonth: string = (new Date().getMonth() + 1).toString();
  const currYear: string = new Date().getFullYear().toString();
  const prevMonth: string =
    currMonth === "1" ? "12" : new Date().getMonth().toString();
  const prevYear: string =
    currMonth === "1" ? (new Date().getFullYear() - 1).toString() : currYear;

  const [count, setCount] = useState<Record<StateKey, number>>({
    cur: 0,
    prev: 0,
  });

  const [color, setColor] = useState<Record<StateKey, string>>({
    cur: "",
    prev: "",
  });

  const [fill, setFill] = useState<Record<StateKey, string>>({
    cur: "",
    prev: "",
  });

  const relativePercent = useMemo(
    () =>
      count.prev > 0
        ? ((count.cur - count.prev) / count.prev) * 100
        : count.cur === 0
          ? 0
          : count.cur,
    [count.cur, count.prev],
  );

  const maxCount = useMemo(
    () => Math.max(count.cur, count.prev),
    [count.cur, count.prev],
  );

  const {
    messages: curMessage,
    error: curError,
    isFinished: curFinished,
  } = useFetchStream<MonthlyEntityGroup<CustomEntityModel<M>>>({
    path: `${basePath}/admin/groupedByMonth`,
    authToken: true,
    method: "GET",
    queryParams: {
      month: currMonth,
    },
  });
  const {
    messages: prevMessage,
    error: prevError,
    isFinished: prevFinished,
  } = useFetchStream<MonthlyEntityGroup<CustomEntityModel<M>>>({
    path: `${basePath}/admin/groupedByMonth`,
    authToken: true,
    method: "GET",
    queryParams: {
      month: prevMonth,
    },
  });

  useEffect(() => {
    if (curMessage) {
      setCount((prev) => ({
        ...prev,
        cur: curMessage.length,
      }));
      updateAboveCount?.(curMessage.length);
    }
  }, [curMessage]);

  useEffect(() => {
    if (prevMessage) {
      setCount((prev) => ({
        ...prev,
        prev: prevMessage.length,
      }));
    }
  }, [prevMessage]);

  useEffect(() => {
    if (curFinished && prevFinished) {
      updateAllFinished?.(true);
    }
  }, [curFinished, prevFinished]);

  useEffect(() => {
    const isCurBigger = count.cur > count.prev;
    const curColor = isCurBigger ? "hsl(var(--chart-1))" : "hsl(var(--muted))";
    const prevColor = isCurBigger ? "hsl(var(--muted))" : "hsl(var(--chart-1))";
    const curFill = isCurBigger ? "white" : "hsl(var(--muted-foreground))";
    const prevFill = isCurBigger ? "hsl(var(--muted-foreground))" : "white";
    setColor({ cur: curColor, prev: prevColor });
    setFill({ cur: curFill, prev: prevFill });
  }, [count.cur, count.prev]);

  const bothFinished = curFinished && prevFinished;

  usePreloadDynamicComponents(DynamicRelativeItemBarChart);

  if (curError?.status || prevError?.status) {
    return null;
  }

  return (
    <Card className="max-w-[350px] mx-auto shadow h-full">
      <CardHeader>
        <CardTitle className="capitalize">{type}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent className="grid gap-4">
        {!bothFinished ? (
          <Loader className="mx-auto" />
        ) : (
          <>
            <div className="grid auto-rows-min gap-2">
              <div className="flex items-baseline gap-1 text-2xl font-bold tabular-nums leading-none">
                {count.cur}
                <span className="text-sm font-normal text-muted-foreground">
                  {`${type}/${month}`}
                </span>
              </div>
              <DynamicRelativeItemBarChart
                month={currMonth}
                year={currYear}
                count={count.cur}
                maxCount={maxCount}
                color={color.cur}
                fill={fill.cur}
              />
            </div>
            <div className="grid auto-rows-min gap-2">
              <div className="flex items-baseline gap-1 text-2xl font-bold tabular-nums leading-none">
                {count.prev}
                <span className="text-sm font-normal text-muted-foreground">
                  {`${type}/${month}`}
                </span>
              </div>
              <DynamicRelativeItemBarChart
                month={prevMonth}
                year={prevYear}
                count={count.prev}
                maxCount={maxCount}
                color={color.prev}
                fill={fill.prev}
              />
            </div>
          </>
        )}
      </CardContent>
      {bothFinished && (
        <CardFooter className="flex items-center flex-col justify-center gap-1 mt-8">
          <p
            className={cn(
              count.cur > count.prev ? "text-success" : "text-destructive",
              " font-semibold",
            )}
          >
            {Math.floor(relativePercent)} {"%"}{" "}
            {relativePercent > 0 ? increase : decrease}
          </p>
          <Progress
            value={Math.min(Math.abs(relativePercent), 100)}
            indicatorClassName={
              count.cur > count.prev ? "bg-success" : "bg-destructive"
            }
          />
        </CardFooter>
      )}
    </Card>
  );
}

export interface RelativeItemsSummaryTexts {
  items: string;
  description: string;
}
interface RelativeItemsSummaryProps extends RelativeItemPieChartBaseProps {
  items: Record<RelativeItems, number>;
  texts: Record<RelativeItems, string>;
  summaryTexts: RelativeItemsSummaryTexts;
  allFinished: boolean;
}
const getChartConfig = (texts: Record<RelativeItems, string>) =>
  relativeItems.reduce(
    (acc, cur, i) => ({
      ...acc,
      [cur]: {
        label: texts[cur],
        color: `hsl(var(--chart-${i + 1}))`,
      },
    }),
    {},
  ) satisfies ChartConfig;

export function RelativeItemsSummary({
  items,
  texts,
  strokeWith = 6,
  innerRadius = 55,
  summaryTexts,
  allFinished,
  ...props
}: RelativeItemsSummaryProps) {
  const debouncedFinish = useDebounce(allFinished, 250);

  usePreloadDynamicComponents(DynamicRelativeItemPieChart, allFinished);

  const chartData = useMemo(
    () =>
      relativeItems.map((item) => ({
        item,
        value: items[item],
        fill: `var(--color-${item})`,
      })),
    [items],
  );

  const totalItems = useMemo(
    () => Object.values(items).reduce((acc, cur) => acc + cur, 0),
    [items],
  );

  const chartConfig = getChartConfig(texts);
  return (
    <Card className="max-w-[350px] mx-auto shadow h-full overflow-hidden">
      <CardHeader>
        <CardTitle className="capitalize">{summaryTexts.items}</CardTitle>
        <CardDescription>{summaryTexts.description}</CardDescription>
      </CardHeader>
      <CardContent className="h-full">
        {!debouncedFinish ? (
          <Loader className="mx-auto" />
        ) : (
          <DynamicRelativeItemPieChart
            chartConfig={chartConfig}
            chartData={chartData}
            itemsText={summaryTexts.items}
            totalItems={totalItems}
            strokeWith={strokeWith}
            innerRadius={innerRadius}
            {...props}
          />
        )}
      </CardContent>
    </Card>
  );
}
