import { SingleDayTexts } from "@/components/days/single-day";
import { getTranslations } from "next-intl/server";
import { getNutritionalTableTexts } from "@/texts/components/common";
import { getIngredientPieChartTexts } from "@/texts/components/charts";
import { DayTypeBadgeTexts } from "@/components/days/day-type-badge";
import { DayType, dayTypes } from "@/types/dto";
import { DaysListTexts } from "@/components/days/days-list";
import { getAnswerFromBodyFormTexts } from "@/texts/components/forms";

export async function getSingleDayTexts(): Promise<SingleDayTexts> {
  const [
    t,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    dayBadgeTexts,
    answerFromBodyFormTexts,
  ] = await Promise.all([
    getTranslations("components.days.SingleDayTexts"),
    getNutritionalTableTexts(),
    getIngredientPieChartTexts(),
    getDayTypeBadgeTexts(),
    getAnswerFromBodyFormTexts(),
  ]);
  return {
    meals: t("meals"),
    nutritionalTableTexts,
    ingredientPieChartTexts,
    showIngredients: t("showIngredients"),
    answerFromBodyFormTexts,
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
  const [t, answerFromBodyFormTexts] = await Promise.all([
    getTranslations("components.days.DaysListTexts"),
    getAnswerFromBodyFormTexts(),
  ]);
  return {
    header: t("header"),
    answerFromBodyFormTexts,
  };
}
