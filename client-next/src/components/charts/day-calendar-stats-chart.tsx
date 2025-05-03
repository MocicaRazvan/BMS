"use client";

import { DayCalendarTrackingStats, dayTypes } from "@/types/dto";
import {
  Bar,
  BarChart,
  CartesianGrid,
  LabelList,
  XAxis,
  YAxis,
} from "recharts";
import {
  ChartConfig,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { useMemo } from "react";
import { getColorsByDayType } from "@/context/day-calendar-context";
import chunk from "lodash/chunk";
import { Separator } from "@/components/ui/separator";
import { useMedia } from "react-use";

interface Props {
  data: DayCalendarTrackingStats[];
}
const colors = getColorsByDayType();
const chartConfig = dayTypes.reduce((acc, cur) => {
  acc[cur] = {
    label: cur,
    color: colors[cur],
  };
  return acc;
}, {} as ChartConfig);

export default function DayCalendarStatsChart({ data }: Props) {
  const isSmall = useMedia("(max-width: 850px)", false);
  const chunkSize = isSmall ? 1 : 3;
  const { chartData, chunkedData, globalMax } = useMemo(() => {
    const entries: Record<string, number | string>[] = [];
    let max = 0;

    data.forEach((d) => {
      const entry: Record<string, number | string> = {
        key: `${d.month}-${d.year}`,
      };
      dayTypes.forEach((type) => {
        const val = d.typeCounts[type] ?? 0;
        entry[type] = val;
        if (val > max) max = val;
      });
      entries.push(entry);
    });

    const chunked = chunk(entries, chunkSize);
    return { chartData: entries, chunkedData: chunked, globalMax: max };
  }, [chunkSize, data]);

  return (
    <div className="size-full space-y-3 md:space-y-5">
      {chunkedData.map((curData, i) => (
        <div
          className="size-full space-y-3 md:space-y-5"
          key={i + "day-stats-chart"}
        >
          <ChartContainer
            config={chartConfig}
            className="aspect-auto h-[350px] w-full "
          >
            <BarChart
              accessibilityLayer
              data={curData}
              margin={{
                top: 30,
              }}
            >
              <CartesianGrid vertical={false} />
              <XAxis
                dataKey="key"
                tickLine={false}
                tickMargin={10}
                axisLine={false}
              />
              <YAxis
                domain={[0, globalMax]}
                axisLine={false}
                tickLine={false}
              />
              <ChartTooltip
                cursor={false}
                content={<ChartTooltipContent indicator="dot" />}
              />
              {dayTypes.map((d) => (
                <Bar
                  key={d + "bar"}
                  dataKey={d}
                  fill={`var(--color-${d})`}
                  radius={4}
                >
                  <LabelList
                    position="top"
                    offset={12}
                    className="fill-foreground"
                    fontSize={12}
                  />
                </Bar>
              ))}
            </BarChart>
          </ChartContainer>
          {i < chunkedData.length - 1 && <Separator />}
        </div>
      ))}
    </div>
  );
}
