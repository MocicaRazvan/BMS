import { IngredientPieChartTexts } from "@/components/charts/ingredient-macros-pie-chart";
import { getTranslations } from "next-intl/server";
import {
  RelativeItems,
  RelativeItemsSummaryTexts,
  RelativeItemTexts,
} from "@/components/charts/relative-item";
import { TotalAmountCountOrdersTexts } from "@/components/charts/totalAmount-count-ordres";
import { GeographyChartTexts } from "@/components/charts/geography-chart";
import { CountrySummaryType } from "@/types/dto";
import { MonthlySalesTexts } from "@/components/charts/monthly-sales";
import { getDateRangePickerTexts } from "@/texts/components/ui";
import { DailySalesTexts } from "@/components/charts/daily-sales";
export async function getIngredientPieChartTexts(): Promise<IngredientPieChartTexts> {
  const t = await getTranslations("components.charts.IngredientPieChartTexts");
  return {
    macroLabel: t("macroLabel"),
    proteinLabel: t("proteinLabel"),
    fatLabel: t("fatLabel"),
    carbohydratesLabel: t("carbohydratesLabel"),
    caloriesLabel: t("caloriesLabel"),
    saltLabel: t("saltLabel"),
  };
}

export async function getRelativeItemTexts(
  type: RelativeItems,
): Promise<RelativeItemTexts> {
  const t = await getTranslations("components.charts.RelativeItemTexts");
  const intlType = t("type." + type);
  return {
    type: intlType,
    description: t("description", { type: intlType }),
    decrease: t("decrease"),
    increase: t("increase"),
    month: t("month"),
  };
}

export async function getAllRelativeItemTexts(): Promise<
  Record<RelativeItems, RelativeItemTexts>
> {
  const items: RelativeItems[] = [
    "posts",
    "orders",
    "recipes",
    "plans",
    "comments",
  ];
  const texts = await Promise.all(
    items.map((type) => getRelativeItemTexts(type)),
  );
  return items.reduce(
    (acc, item, idx) => {
      acc[item] = texts[idx];
      return acc;
    },
    {} as Record<RelativeItems, RelativeItemTexts>,
  );
}

export async function getRelativeItemsSummaryTexts(): Promise<RelativeItemsSummaryTexts> {
  const t = await getTranslations(
    "components.charts.RelativeItemsSummaryTexts",
  );
  return {
    items: t("items"),
    description: t("description"),
  };
}
export async function getTotalAmountCountOrdersTexts(
  type: "orders" | "plans",
): Promise<TotalAmountCountOrdersTexts> {
  const t = await getTranslations(
    "components.charts.TotalAmountCountOrdersTexts",
  );
  const intlType = t("type." + type);
  return {
    totalAmountLabel: t("totalAmountLabel", { type: intlType }),
    countLabel: t("countLabel", { type: intlType }),
    bothLabel: t("bothLabel"),
    averageCountLabel: t("averageCountLabel", { type: intlType }),
    averageTotalAmountLabel: t("averageTotalAmountLabel", { type: intlType }),
    hideTrendLineLabel: t("hideTrendLineLabel"),
    showTrendLineLabel: t("showTrendLineLabel"),
    trendLineLabel: t("trendLineLabel"),
  };
}
export async function getGeographyChartTexts(): Promise<GeographyChartTexts> {
  const t = await getTranslations("components.charts.GeographyChartTexts");
  return {
    resetLabel: t("resetLabel"),
    centerLabel: t("centerLabel"),
    zoomOutLabel: t("zoomOutLabel"),
    zoomInLabel: t("zoomInLabel"),
    selectLabels: {
      [CountrySummaryType.COUNT]: t("selectLabels.count"),
      [CountrySummaryType.TOTAL_AMOUNT]: t("selectLabels.totalAmount"),
    },
  };
}

export async function getMonthlySalesTexts(
  type: "orders" | "plans",
): Promise<MonthlySalesTexts> {
  const [dateRangePickerTexts, totalAmountCountOrdersTexts] = await Promise.all(
    [getDateRangePickerTexts(), getTotalAmountCountOrdersTexts(type)],
  );
  return {
    dateRangePickerTexts,
    totalAmountCountOrdersTexts,
  };
}

export async function getDailySalesTexts(
  type: "orders" | "plans",
): Promise<DailySalesTexts> {
  const [dateRangePickerTexts, totalAmountCountOrdersTexts] = await Promise.all(
    [getDateRangePickerTexts(), getTotalAmountCountOrdersTexts(type)],
  );
  return {
    dateRangePickerTexts,
    totalAmountCountOrdersTexts,
  };
}
