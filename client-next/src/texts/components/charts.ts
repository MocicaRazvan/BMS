"use server";
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
import { TopUsersTexts } from "@/components/charts/top-users";
import { getCreationFilterTexts } from "@/texts/components/list";
import { TopChartWrapperTexts } from "@/components/charts/top-chart-wrapper";
import { TopPlansTexts } from "@/components/charts/top-plans";
import {
  PlanCharacteristicTexts,
  PlanCharacteristicWrapperTexts,
} from "@/components/charts/plan-charctersitic";
import { TopTrainersTexts } from "@/components/charts/top-trainers";
import { PredictionChartTexts } from "@/components/charts/prediction-chart";
import { TopViewedPostsTexts } from "@/components/charts/top-viewed-posts";
import { LinkedChartTexts } from "@/components/charts/linked-chart";

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
  const [t, creationFilterTexts] = await Promise.all([
    getTranslations("components.charts.GeographyChartTexts"),
    getCreationFilterTexts(),
  ]);
  return {
    resetLabel: t("resetLabel"),
    centerLabel: t("centerLabel"),
    zoomOutLabel: t("zoomOutLabel"),
    zoomInLabel: t("zoomInLabel"),
    creationFilterTexts,
    selectLabels: {
      [CountrySummaryType.COUNT]: t("selectLabels.count"),
      [CountrySummaryType.TOTAL_AMOUNT]: t("selectLabels.totalAmount"),
    },
  };
}

export async function getMonthlySalesTexts(
  type: "orders" | "plans",
): Promise<MonthlySalesTexts> {
  const [
    dateRangePickerTexts,
    totalAmountCountOrdersTexts,
    planCharacteristicWrapperTexts,
    predictionTexts,
  ] = await Promise.all([
    getDateRangePickerTexts(),
    getTotalAmountCountOrdersTexts(type),
    getPlanCharacteristicWrapperTexts(),
    getPredictionChartTexts(type),
  ]);
  return {
    dateRangePickerTexts,
    totalAmountCountOrdersTexts,
    planCharacteristicWrapperTexts,
    predictionTexts,
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

export async function getTopChartWrapperTexts(): Promise<TopChartWrapperTexts> {
  const [t, dateRangePickerTexts] = await Promise.all([
    getTranslations("components.charts.TopChartWrapperTexts"),
    getDateRangePickerTexts(),
  ]);
  return {
    dateRangePickerTexts,
    topLabel: t("topLabel"),
    periodLabel: t("periodLabel"),
    noResults: t("noResults"),
  };
}

export async function getTopUsersTexts(): Promise<TopUsersTexts> {
  const [t, topChartWrapperTexts] = await Promise.all([
    getTranslations("components.charts.TopUsersTexts"),
    getTopChartWrapperTexts(),
  ]);
  return {
    topChartWrapperTexts,
    userCardTexts: {
      amountPerOrderTitle: t("userCardTexts.amountPerOrderTitle"),
      orders: t("userCardTexts.orders"),
      planDistributionTitle: t("userCardTexts.planDistributionTitle"),
      topBuyer: t("userCardTexts.topBuyer"),
      rank: t("userCardTexts.rank"),
      userAntent: t("userCardTexts.userAntent"),
      plans: t("userCardTexts.plans"),
      totalAmount: t("userCardTexts.totalAmount"),
      type: t("userCardTexts.type"),
      objective: t("userCardTexts.objective"),
    },
    title: t("title"),
    userAmountPerOderChartTexts: {
      amountPerOrder: t("userAmountPerOderChartTexts.amountPerOrder"),
      meanAmountPerOrder: t("userAmountPerOderChartTexts.meanAmountPerOrder"),
    },
  };
}

export async function getTopPlansTexts(): Promise<TopPlansTexts> {
  const [t, topChartWrapperTexts] = await Promise.all([
    getTranslations("components.charts.TopPlansTexts"),
    getTopChartWrapperTexts(),
  ]);
  return {
    topChartWrapperTexts,
    planCardTexts: {
      dietType: t("planCardTexts.dietType"),
      madeBy: t("planCardTexts.madeBy"),
      meanNumberOfPurchases: t("planCardTexts.meanNumberOfPurchases"),
      numberOfPurchases: t("planCardTexts.numberOfPurchases"),
      planAntet: t("planCardTexts.planAntet"),
      ratioLabel: t("planCardTexts.ratioLabel"),
      topPlan: t("planCardTexts.topPlan"),
      rankLabel: t("planCardTexts.rankLabel"),
      ordersCount: t("planCardTexts.ordersCount"),
    },
    title: t("title"),
  };
}

export async function getUseDownloadChartButtonTexts() {
  const t = await getTranslations("components.charts.DownloadChartButton");
  return {
    downloadChart: t("downloadChart"),
  };
}

export async function getPlanCharacteristicTexts(): Promise<PlanCharacteristicTexts> {
  const t = await getTranslations("components.charts.PlanCharacteristicTexts");
  return {
    totalAmountLabel: t("totalAmountLabel"),
    typeLabel: t("typeLabel"),
    objectiveLabel: t("objectiveLabel"),
    countLabel: t("countLabel"),
    averageAmountLabel: t("averageAmountLabel"),
  };
}

export async function getPlanCharacteristicWrapperTexts(): Promise<PlanCharacteristicWrapperTexts> {
  const [planCharacteristicTexts, t] = await Promise.all([
    getPlanCharacteristicTexts(),
    getTranslations("components.charts.PlanCharacteristicWrapperTexts"),
  ]);
  return {
    ...planCharacteristicTexts,
    statisticLabel: t("statisticLabel"),
    typeSelectLabel: t("typeSelectLabel"),
    monthSelectLabel: t("monthSelectLabel"),
    chartName: t("chartName"),
    title: t("title"),
  };
}

export async function getTopTrainersTexts(): Promise<TopTrainersTexts> {
  const [t, topChartWrapperTexts] = await Promise.all([
    getTranslations("components.charts.TopTrainerTexts"),
    getTopChartWrapperTexts(),
  ]);

  return {
    topChartWrapperTexts,
    trainerCardTexts: {
      userAntent: t("trainerCardTexts.userAntent"),
      topTrainer: t("trainerCardTexts.topTrainer"),
      totalAmount: t("trainerCardTexts.totalAmount"),
      countPlans: t("trainerCardTexts.countPlans"),
      totalAmountReference: t("trainerCardTexts.totalAmountReference"),
      countPlansReference: t("trainerCardTexts.countPlansReference"),
      rankLabel: t("trainerCardTexts.rankLabel"),
      dropDownMenuTexts: {
        averageAmountLabel: t(
          "trainerCardTexts.dropDownMenuTexts.averageAmountLabel",
        ),
        countLabel: t("trainerCardTexts.dropDownMenuTexts.countLabel"),
        totalAmountLabel: t(
          "trainerCardTexts.dropDownMenuTexts.totalAmountLabel",
        ),
      },
      typePieChartTitle: t("trainerCardTexts.typePieChartTitle"),
      objectivePieChartTitle: t("trainerCardTexts.objectivePieChartTitle"),
    },
    title: t("title"),
  };
}

export async function getPredictionChartTexts(
  type: "orders" | "plans",
): Promise<PredictionChartTexts> {
  const t = await getTranslations("components.charts.PredictionChartTexts");
  const intlType = t("type." + type);
  return {
    countAreaLabel: t("countAreaLabel", { type: intlType }),
    countLabel: t("countLabel", { type: intlType }),
    totalAmountLabel: t("totalAmountLabel", { type: intlType }),
    disclaimer: t("disclaimer"),
    title: t("title"),
    totalAmountAreaLabel: t("totalAmountAreaLabel"),
    predictionLengthLabel: t("predictionLengthLabel"),
  };
}

export async function getTopViewedPostsTexts(): Promise<TopViewedPostsTexts> {
  const [dateRangePickerTexts, t] = await Promise.all([
    getDateRangePickerTexts(),
    getTranslations("components.charts.TopViewedPostsTexts"),
  ]);
  return {
    dateRangePickerTexts,
    rank: t("rank"),
    title: t("title"),
    topLabel: t("topLabel"),
    viewCount: t("viewCount"),
    noResults: t("noResults"),
    tags: t("tags"),
    periodLabel: t("periodLabel"),
  };
}

export async function getLinkedChartTexts(): Promise<LinkedChartTexts> {
  const t = await getTranslations("components.charts.LinkedChartTexts");
  return {
    title: t("title"),
    nothingFound: t("nothingFound"),
    chartType: t("chartType"),
    aggregates: t("aggregates"),
    dateFormat: t("dateFormat"),
    reset: t("reset"),
    aggregatesPlaceholder: t("aggregatesPlaceholder"),
    selectAggregates: t("selectAggregates"),
    dateFormatPlaceholder: t("dateFormatPlaceholder"),
    dateFormatDropDown: {
      "dd/MM/yyyy": t("dateFormatDropDown.dd/MM/yyyy"),
      "MMM yyyy": t("dateFormatDropDown.MMM_yyyy"),
      "QQQ yyyy": t("dateFormatDropDown.QQQ_yyyy"),
      yyyy: t("dateFormatDropDown.yyyy"),
      "MM/dd/yyyy": t("dateFormatDropDown.MM/dd/yyyy"),
      "yyyy-MM-dd": t("dateFormatDropDown.yyyy-MM-dd"),
    },
  };
}
