"use client";
import { ro } from "date-fns/locale";
import { format, subMonths } from "date-fns";
import { useLocale } from "next-intl";
import { useMemo, useState } from "react";
import {
  TotalAmountCountOrdersData,
  TotalAmountCountOrdersTexts,
} from "@/components/charts/totalAmount-count-ordres";
import {
  DateRangeParams,
  DateRangePicker,
  DateRangePickerTexts,
} from "@/components/ui/date-range-picker";
import useFetchStream from "@/hoooks/useFetchStream";
import { DailyOrderSummary } from "@/types/dto";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { Separator } from "@/components/ui/separator";
import dynamic from "next/dynamic";
import { Skeleton } from "@/components/ui/skeleton";
import { TrendLineButton } from "@/components/charts/totalAmount-count-orders-inputs";

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

export interface DailySalesTexts {
  totalAmountCountOrdersTexts: TotalAmountCountOrdersTexts;
  dateRangePickerTexts: DateRangePickerTexts;
}

interface Props extends DailySalesTexts {
  path: string;
  countColorIndex?: number;
  totalAmountColorIndex?: number;
  hideTotalAmount?: boolean;
}

const now = new Date();
const oneMonthAgo = subMonths(now, 1);

const dateFormat = "dd-MM-yyyy";
const formattedNow = format(now, dateFormat);
const formattedOneMonthAgo = format(oneMonthAgo, dateFormat);

export default function DailySales({
  dateRangePickerTexts,
  totalAmountCountOrdersTexts,
  path,
  countColorIndex = 1,
  totalAmountColorIndex = 6,
  hideTotalAmount = false,
}: Props) {
  const { navigateToNotFound } = useClientNotFound();

  const locale = useLocale();

  const [showOrdersTrendLine, setShowOrdersTrendLine] = useState(true);
  const [showAmountTrendLine, setShowAmountTrendLine] = useState(true);

  const [dateRange, setDateRange] = useState<DateRangeParams>({
    from: formattedOneMonthAgo,
    to: formattedNow,
  });
  const { messages, error, isFinished } = useFetchStream<DailyOrderSummary>({
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
        count: i.count,
        totalAmount: Math.floor(i.totalAmount),
        date: format(new Date(i.year, i.month - 1, i.day), dateFormat),
      })),
    [JSON.stringify(messages)],
  );

  const dateRangePicker = useMemo(
    () => (
      <DateRangePicker
        onUpdate={({ range: { from, to } }) =>
          setDateRange({
            from: format(from, dateFormat),
            to: format(to || from, dateFormat),
          })
        }
        hiddenPresets={["today", "yesterday", "lastWeek"]}
        align="center"
        locale={locale === "ro" ? ro : undefined}
        defaultPreset={"thisMonth"}
        showCompare={false}
        {...dateRangePickerTexts}
      />
    ),
    [dateRangePickerTexts, locale],
  );

  if (error?.status) {
    return navigateToNotFound();
  }

  return (
    <div className="w-full h-ful space-y-10 pt-10 md:space-y-14">
      <div>
        <div className="flex flex-col md:flex-row gap-5 md:gap-2 items-center justify-between w-full flex-wrap">
          {dateRangePicker}
          <h2 className="text-xl font-bold tracking-tighter md:text-2xl">
            {totalAmountCountOrdersTexts.countLabel}
          </h2>
          <div className="md:min-w-80 flex  items-center justify-end">
            {formattedData.length > 1 && (
              <TrendLineButton
                {...totalAmountCountOrdersTexts}
                showTrendLine={showOrdersTrendLine}
                onShowTrendLineChange={setShowOrdersTrendLine}
              />
            )}
          </div>
        </div>
        <DynamicTotalAmountCountOrders
          data={formattedData}
          dataAvailable={isFinished}
          {...totalAmountCountOrdersTexts}
          showTotalAmount={false}
          countLabel={totalAmountCountOrdersTexts.countLabel}
          showTrendLine={showOrdersTrendLine}
          chartName={totalAmountCountOrdersTexts.countLabel}
          countColorIndex={countColorIndex}
          totalAmountColorIndex={totalAmountColorIndex}
        />
      </div>
      {!hideTotalAmount && (
        <>
          <Separator />
          <div>
            <div className="flex flex-col md:flex-row gap-5 md:gap-2 items-center justify-between w-full flex-wrap">
              {dateRangePicker}
              <h2 className="text-xl font-bold tracking-tighter md:text-2xl">
                {totalAmountCountOrdersTexts.totalAmountLabel}
              </h2>
              <div className="md:min-w-80 flex items-center justify-end">
                {formattedData.length > 1 && (
                  <TrendLineButton
                    {...totalAmountCountOrdersTexts}
                    showTrendLine={showAmountTrendLine}
                    onShowTrendLineChange={setShowAmountTrendLine}
                  />
                )}
              </div>
            </div>
            <DynamicTotalAmountCountOrders
              data={formattedData}
              dataAvailable={isFinished}
              {...totalAmountCountOrdersTexts}
              showCount={false}
              countLabel={totalAmountCountOrdersTexts.countLabel}
              showTrendLine={showAmountTrendLine}
              chartName={totalAmountCountOrdersTexts.totalAmountLabel}
              countColorIndex={countColorIndex}
              totalAmountColorIndex={totalAmountColorIndex}
            />
          </div>
        </>
      )}
    </div>
  );
}
