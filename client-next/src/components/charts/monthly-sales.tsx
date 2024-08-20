"use client";

import { WithUser } from "@/lib/user";
import { format, subDays, subYears } from "date-fns";
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
  TotalAmountCountOrdersData,
  TotalAmountCountOrdersTexts,
  TotalAmountOrdersSingleBarChart,
  TrendLineButton,
} from "@/components/charts/totalAmount-count-ordres";
import useFetchStream from "@/hoooks/useFetchStream";
import { MonthlyOrderSummary } from "@/types/dto";
import { ro } from "date-fns/locale";
import useClientNotFound from "@/hoooks/useClientNotFound";
const now = new Date();
const oneMonthAgo = subDays(now, 30);
const oneYearAgo = subYears(now, 1);

const dateFormat = "dd-MM-yyyy";
const formattedNow = format(now, dateFormat);
const formattedOneMonthAgo = format(oneMonthAgo, dateFormat);
const formattedOneYearAgo = format(oneYearAgo, dateFormat);

export interface MonthlySalesTexts {
  totalAmountCountOrdersTexts: TotalAmountCountOrdersTexts;
  dateRangePickerTexts: DateRangePickerTexts;
}
interface Props extends WithUser, MonthlySalesTexts {
  path: string;
}
export default function MonthlySales({
  authUser,
  dateRangePickerTexts,
  totalAmountCountOrdersTexts,
  path,
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

  const formattedData: TotalAmountCountOrdersData[] = useMemo(
    () =>
      messages.map((i) => ({
        count: i.count * 10,
        totalAmount: Math.floor(i.totalAmount),
        date: format(new Date(i.year, i.month - 1), "MM-yyyy"),
      })),
    [JSON.stringify(messages)],
  );

  useEffect(() => {
    setShowTrendLine(areaRadioOption !== "both");
  }, [areaRadioOption]);

  const countFormattedData = useMemo(
    () => formattedData.map((i) => ({ ...i, count: i.count / 10 })),
    [formattedData],
  );

  const dateRangePicker = useMemo(
    () => (
      <DateRangePicker
        hiddenPresets={[
          "today",
          "yesterday",
          "last7",
          "last14",
          "thisWeek",
          "lastWeek",
        ]}
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

  return (
    <div className="w-full h-ful space-y-10 pt-10 md:space-y-14">
      <div>
        <div className="flex items-center justify-between w-full flex-wrap  ">
          {dateRangePicker}
          <div className="mt-2 md:mt-0 flex items-center justify-start gap-4">
            {TrendLineBtn}
            <DropDownMenuCountTotalAmountSelect
              {...totalAmountCountOrdersTexts}
              onRadioOptionChange={setAreaRadioOption}
              radioOption={areaRadioOption}
            />
          </div>
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
        />
      </div>
      <div>
        {dateRangePicker}
        <TotalAmountOrdersSingleBarChart
          data={formattedData}
          dataAvailable={isFinished}
          {...totalAmountCountOrdersTexts}
          fieldKey={"totalAmount"}
        />
      </div>
      <div>
        {dateRangePicker}
        <TotalAmountOrdersSingleBarChart
          data={countFormattedData}
          dataAvailable={isFinished}
          {...totalAmountCountOrdersTexts}
          countLabel={totalAmountCountOrdersTexts.countLabel.slice(3)}
          fieldKey={"count"}
        />
      </div>
    </div>
  );
}
