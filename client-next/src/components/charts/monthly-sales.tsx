"use client";

import { WithUser } from "@/lib/user";
import { format, subMonths, subYears } from "date-fns";
import { useLocale } from "next-intl";
import { useEffect, useMemo, useState } from "react";
import {
  DateRangeParams,
  DateRangePicker,
  DateRangePickerTexts,
} from "@/components/ui/date-range-picker";
import {
  CountTotalAmountRadioOptionsType,
  DropDownMenuCountTotalAmountSelect,
  TotalAmountCountOrders,
  TotalAmountCountOrdersTexts,
  TotalAmountOrdersSingleBarChart,
  TrendLineButton,
} from "@/components/charts/totalAmount-count-ordres";
import useFetchStream from "@/hoooks/useFetchStream";
import { MonthlyOrderSummary } from "@/types/dto";
import { ro } from "date-fns/locale";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { Separator } from "@/components/ui/separator";
import {
  PlanCharacteristicColors,
  PlanCharacteristicWrapper,
  PlanCharacteristicWrapperTexts,
} from "@/components/charts/plan-charctersitic";

const now = new Date();
const oneMonthAgo = subMonths(now, 1);
const oneYearAgo = subYears(now, 1);

const dateFormat = "dd-MM-yyyy";
const formattedNow = format(now, dateFormat);
const formattedOneMonthAgo = format(oneMonthAgo, dateFormat);
const formattedOneYearAgo = format(oneYearAgo, dateFormat);

export interface MonthlySalesTexts {
  totalAmountCountOrdersTexts: TotalAmountCountOrdersTexts;
  dateRangePickerTexts: DateRangePickerTexts;
  planCharacteristicWrapperTexts: PlanCharacteristicWrapperTexts;
}
interface Props extends WithUser, MonthlySalesTexts {
  path: string;
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
  authUser,
  dateRangePickerTexts,
  totalAmountCountOrdersTexts,
  path,
  countColorIndex = 1,
  totalAmountColorIndex = 6,
  hideTotalAmount = false,
  characteristicProps = undefined,
  planCharacteristicWrapperTexts,
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
    queryParams: {
      ...dateRange,
    },
  });

  const formattedData = useMemo(
    () =>
      messages.map((i) => ({
        count: i.count,
        count10: 10 * i.count,
        totalAmount: Math.floor(i.totalAmount),
        date: format(new Date(i.year, i.month - 1), "MM-yyyy"),
      })),
    [JSON.stringify(messages)],
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
        <TotalAmountCountOrders
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
            showTotalAmount ? "count10" : "count"
          }
          extraChartConfig={{
            count10: {
              label: "10*" + totalAmountCountOrdersTexts.countLabel,
              color: `hsl(var(--chart-${countColorIndex}))`,
            },
          }}
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
            <TotalAmountOrdersSingleBarChart
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
        <TotalAmountOrdersSingleBarChart
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
