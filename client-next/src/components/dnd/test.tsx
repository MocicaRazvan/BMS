// "use client";
//
// import useFetchStream from "@/hoooks/useFetchStream";
// import { DailyOrderSummary, MonthlyOrderSummary } from "@/types/dto";
// import { format, subDays } from "date-fns";
// import { useLocale } from "next-intl";
// import { useMemo, useState } from "react";
// import {
//   DateRangeParams,
//   DateRangePicker,
//   DateRangePickerTexts,
// } from "@/components/ui/date-range-picker";
// import {
//   CountTotalAmountRadioOptionsType,
//   DropDownMenuCountTotalAmountSelect,
//   TotalAmountCountOrders,
//   TotalAmountCountOrdersData,
//   TotalAmountCountOrdersTexts,
// } from "@/components/charts/totalAmount-count-ordres";
// import { ro } from "date-fns/locale";
//
// const now = new Date();
// const oneMonthAgo = subDays(now, 30);
//
// const dateFormat = "dd-MM-yyyy";
// const formattedNow = format(now, dateFormat);
// const formattedOneMonthAgo = format(oneMonthAgo, dateFormat);
//
// interface Props {
//   totalAmountCountOrdersTexts: TotalAmountCountOrdersTexts;
//   dateRangePickerTexts: DateRangePickerTexts;
// }
// export default function Test({
//   totalAmountCountOrdersTexts,
//   dateRangePickerTexts,
// }: Props) {
//   const locale = useLocale();
//
//   const [areaRadioOption, setAreaRadioOption ] =
//     useState<CountTotalAmountRadioOptionsType>("count");
//   const [dateRange, setDateRange] = useState<DateRangeParams>({
//     from: formattedOneMonthAgo,
//     to: formattedNow,
//   });
//   const { messages, error, isFinished } = useFetchStream<DailyOrderSummary>({
//     path: "/orders/admin/countAndAmount/daily",
//     authToken: true,
//     method: "GET",
//     queryParams: {
//       ...dateRange,
//     },
//   });
//
//   const formattedData: TotalAmountCountOrdersData[] = useMemo(
//     () =>
//       messages.map((i) => ({
//         count: i.count,
//         totalAmount: Math.floor(i.totalAmount),
//         date: format(new Date(i.year, i.month - 1, i.day), dateFormat),
//       })),
//     [JSON.stringify(messages)],
//   );
//
//   const dateRangePicker = useMemo(
//     () => (
//       <DateRangePicker
//         // hiddenPresets={[
//         //   "today",
//         //   "yesterday",
//         //   "last7",
//         //   "last14",
//         //   "thisWeek",
//         //   "lastWeek",
//         // ]}
//         onUpdate={({ range: { from, to } }) =>
//           setDateRange({
//             from: format(from, dateFormat),
//             to: format(to || from, dateFormat),
//           })
//         }
//         align="center"
//         locale={locale === "ro" ? ro : undefined}
//         defaultPreset={"lastMonth"}
//         showCompare={false}
//         {...dateRangePickerTexts}
//       />
//     ),
//     [dateRangePickerTexts, locale],
//   );
//
//   return (
//     <div className="w-full h-ful space-y-10 pt-10 md:space-y-14">
//       <div>
//         <div className="flex items-center justify-between w-full flex-wrap">
//           {dateRangePicker}
//           <DropDownMenuCountTotalAmountSelect
//             {...totalAmountCountOrdersTexts}
//             onRadioOptionChange={setAreaRadioOption}
//             radioOption={areaRadioOption}
//             showBoth={false}
//             countLabel={totalAmountCountOrdersTexts.countLabel.slice(3)}
//           />
//         </div>
//         <TotalAmountCountOrders
//           data={formattedData}
//           dataAvailable={isFinished}
//           {...totalAmountCountOrdersTexts}
//           showCount={areaRadioOption === "count"}
//           showTotalAmount={areaRadioOption === "totalAmount"}
//           countLabel={totalAmountCountOrdersTexts.countLabel.slice(3)}
//         />
//       </div>
//     </div>
//   );
// }
