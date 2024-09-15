import { SingleDayTexts } from "@/components/days/single-day";
import { getTranslations } from "next-intl/server";
import { getNutritionalTableTexts } from "@/texts/components/common";
import { getIngredientPieChartTexts } from "@/texts/components/charts";
import { DayTypeBadgeTexts } from "@/components/days/day-type-badge";
import { DayType, dayTypes } from "@/types/dto";
import { DaysListTexts } from "@/components/days/days-list";

export async function getSingleDayTexts(): Promise<SingleDayTexts> {
  const [t, nutritionalTableTexts, ingredientPieChartTexts, dayBadgeTexts] =
    await Promise.all([
      getTranslations("components.days.SingleDayTexts"),
      getNutritionalTableTexts(),
      getIngredientPieChartTexts(),
      getDayTypeBadgeTexts(),
    ]);
  return {
    meals: t("meals"),
    nutritionalTableTexts,
    ingredientPieChartTexts,
    showIngredients: t("showIngredients"),
    dayBadgeTexts,
  };
}

export async function getDayTypeBadgeTexts(): Promise<DayTypeBadgeTexts> {
  const [t] = await Promise.all([
    getTranslations("components.days.DayTypeBadgeTexts"),
  ]);
  return {
    labels: dayTypes.reduce(
      (acc, cur) => ({ ...acc, [cur]: t(cur) }),
      {} as Record<DayType, string>,
    ),
  };
}

export async function getDaysListTexts(): Promise<DaysListTexts> {
  const [t] = await Promise.all([
    getTranslations("components.days.DaysListTexts"),
  ]);
  return {
    header: t("header"),
  };
}
