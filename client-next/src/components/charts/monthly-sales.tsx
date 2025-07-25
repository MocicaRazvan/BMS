"use client";

import { format, subMonths, subYears } from "date-fns";
import { useLocale } from "next-intl";
import { useEffect, useMemo, useState } from "react";
import {
  DateRangeParams,
  DateRangePicker,
  DateRangePickerTexts,
} from "@/components/ui/date-range-picker";
import { TotalAmountCountOrdersTexts } from "@/components/charts/totalAmount-count-ordres";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import { MonthlyOrderSummary } from "@/types/dto";
import { ro } from "date-fns/locale";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { Separator } from "@/components/ui/separator";
import {
  PlanCharacteristicColors,
  PlanCharacteristicWrapper,
  PlanCharacteristicWrapperTexts,
} from "@/components/plans/plan-charctersitic-wrapper";
import {
  PredictionChart,
  PredictionChartTexts,
} from "@/components/charts/prediction-chart";
import {
  CountTotalAmountRadioOptionsType,
  DropDownMenuCountTotalAmountSelect,
  TrendLineButton,
} from "@/components/charts/totalAmount-count-orders-inputs";
import dynamic from "next/dynamic";
import { Skeleton } from "@/components/ui/skeleton";

const now = new Date();
const oneMonthAgo = subMonths(now, 1);
const oneYearAgo = subYears(now, 1);

const dateFormat = "dd-MM-yyyy";
const formattedNow = format(now, dateFormat);
const formattedOneMonthAgo = format(oneMonthAgo, dateFormat);
const formattedOneYearAgo = format(oneYearAgo, dateFormat);

const DynamicTotalAmountCountOrders = dynamic(
  () =>
    import("@/components/charts/totalAmount-count-ordres").then(
      (mod) => mod.TotalAmountCountOrders,
    ),
  {
    ssr: false,
    loading: () => (
      <div className="w-full py-16">
        <Skeleton className="aspect-auto h-[450px] w-full" />
      </div>
    ),
  },
);

const DynamicTotalAmountOrdersSingleBarChart = dynamic(
  () =>
    import("@/components/charts/totalAmount-count-ordres").then(
      (mod) => mod.TotalAmountOrdersSingleBarChart,
    ),
  {
    ssr: false,
    loading: () => (
      <div className="w-full py-16">
        <Skeleton className="aspect-auto h-[450px] w-full" />
      </div>
    ),
  },
);

export interface MonthlySalesTexts {
  totalAmountCountOrdersTexts: TotalAmountCountOrdersTexts;
  dateRangePickerTexts: DateRangePickerTexts;
  planCharacteristicWrapperTexts: PlanCharacteristicWrapperTexts;
  predictionTexts: PredictionChartTexts;
}
interface Props extends MonthlySalesTexts {
  path: string;
  predictionPath: string;
  countColorIndex?: number;
  totalAmountColorIndex?: number;
  hideTotalAmount?: boolean;
  characteristicProps?: {
    plansPaths: {
      typePath: string;
      objectivePath: string;
      scatterPath: string;
    };
    colors?: PlanCharacteristicColors;
  };
}
export default function MonthlySales({
  dateRangePickerTexts,
  totalAmountCountOrdersTexts,
  path,
  countColorIndex = 1,
  totalAmountColorIndex = 6,
  hideTotalAmount = false,
  characteristicProps = undefined,
  planCharacteristicWrapperTexts,
  predictionTexts,
  predictionPath,
}: Props) {
  const locale = useLocale();

  const { navigateToNotFound } = useClientNotFound();

  const [showTrendLine, setShowTrendLine] = useState(true);

  const [dateRange, setDateRange] = useState<DateRangeParams>({
    from: formattedOneYearAgo,
    to: formattedNow,
  });

  const [areaRadioOption, setAreaRadioOption] =
    useState<CountTotalAmountRadioOptionsType>("both");

  const { messages, error, isFinished } = useFetchStream<MonthlyOrderSummary>({
    path,
    authToken: true,
    method: "GET",
    queryParams: dateRange,
  });

  const formattedData = useMemo(
    () =>
      messages.map((i) => ({
        count: i.count,
        count100: 100 * i.count,
        totalAmount: Math.floor(i.totalAmount),
        date: format(new Date(i.year, i.month - 1), "MM-yyyy"),
      })),
    [messages],
  );

  useEffect(() => {
    setShowTrendLine(areaRadioOption !== "both");
  }, [areaRadioOption]);

  const dateRangePicker = useMemo(
    () => (
      <DateRangePicker
        hiddenPresets={["today", "yesterday", "lastWeek"]}
        onUpdate={({ range: { from, to } }) =>
          setDateRange({
            from: format(from, dateFormat),
            to: format(to || from, dateFormat),
          })
        }
        align="center"
        locale={locale === "ro" ? ro : undefined}
        defaultPreset={"pastYear"}
        showCompare={false}
        {...dateRangePickerTexts}
      />
    ),
    [dateRangePickerTexts, locale],
  );

  const TrendLineBtn = useMemo(
    () => (
      <TrendLineButton
        onShowTrendLineChange={setShowTrendLine}
        showTrendLine={showTrendLine}
        hide={areaRadioOption === "both"}
        {...totalAmountCountOrdersTexts}
      />
    ),
    [areaRadioOption, showTrendLine, totalAmountCountOrdersTexts],
  );
  if (error?.status) {
    return navigateToNotFound();
  }

  const totalCountAmountChartName =
    areaRadioOption === "both"
      ? totalAmountCountOrdersTexts.totalAmountLabel +
        "&" +
        totalAmountCountOrdersTexts.countLabel
      : areaRadioOption === "totalAmount"
        ? totalAmountCountOrdersTexts.totalAmountLabel
        : totalAmountCountOrdersTexts.countLabel;
  return (
    <div className="w-full h-ful space-y-10 pt-10 md:space-y-14">
      <div>
        <div className="flex items-center justify-between w-full flex-wrap  ">
          {dateRangePicker}
          {formattedData.length > 0 && (
            <div className="mt-2 md:mt-0 flex items-center justify-start gap-4">
              <DropDownMenuCountTotalAmountSelect
                {...totalAmountCountOrdersTexts}
                onRadioOptionChange={setAreaRadioOption}
                radioOption={areaRadioOption}
              />
              {formattedData.length > 1 && TrendLineBtn}
            </div>
          )}
        </div>
        <DynamicTotalAmountCountOrders
          data={formattedData}
          dataAvailable={isFinished}
          {...totalAmountCountOrdersTexts}
          showCount={areaRadioOption === "count" || areaRadioOption === "both"}
          showTotalAmount={
            areaRadioOption === "totalAmount" || areaRadioOption === "both"
          }
          showTrendLine={showTrendLine}
          chartName={totalCountAmountChartName}
          mapCountLabel={(showCount, showTotalAmount) =>
            showTotalAmount ? "count100" : "count"
          }
          extraChartConfig={{
            count100: {
              label: "100*" + totalAmountCountOrdersTexts.countLabel,
              color: `hsl(var(--chart-${countColorIndex}))`,
            },
          }}
          countColorIndex={countColorIndex}
          totalAmountColorIndex={totalAmountColorIndex}
        />
      </div>
      <Separator />
      <div>
        <PredictionChart
          path={predictionPath}
          texts={predictionTexts}
          countColorIndex={countColorIndex}
          totalAmountColorIndex={totalAmountColorIndex}
        />
      </div>
      {!hideTotalAmount && (
        <>
          <Separator />
          <div>
            <div className="flex flex-col md:flex-row items-center justify-between gap-5 md:gap-2">
              {dateRangePicker}
              <h2 className="text-xl font-bold tracking-tighter md:text-2xl">
                {totalAmountCountOrdersTexts.totalAmountLabel}
              </h2>
              <div className="w-80" />
            </div>
            <DynamicTotalAmountOrdersSingleBarChart
              data={formattedData}
              dataAvailable={isFinished}
              {...totalAmountCountOrdersTexts}
              fieldKey={"totalAmount"}
              totalAmountColorIndex={totalAmountColorIndex}
            />
          </div>
        </>
      )}
      <Separator />
      <div>
        <div className="flex flex-col md:flex-row items-center justify-between gap-5 md:gap-2">
          {dateRangePicker}
          <h2 className="text-xl font-bold tracking-tighter md:text-2xl">
            {totalAmountCountOrdersTexts.countLabel}
          </h2>
          <div className="w-1 md:w-80" />
        </div>
        <DynamicTotalAmountOrdersSingleBarChart
          data={formattedData}
          dataAvailable={isFinished}
          {...totalAmountCountOrdersTexts}
          countLabel={totalAmountCountOrdersTexts.countLabel}
          fieldKey={"count"}
          countColorIndex={countColorIndex}
        />
      </div>
      {characteristicProps && (
        <>
          <Separator />
          <div>
            <PlanCharacteristicWrapper
              {...characteristicProps.plansPaths}
              {...characteristicProps.colors}
              {...planCharacteristicWrapperTexts}
            />
          </div>
        </>
      )}
    </div>
  );
}
