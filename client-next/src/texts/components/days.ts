import { SingleDayTexts } from "@/components/days/single-day";
import { getTranslations } from "next-intl/server";
import { getNutritionalTableTexts } from "@/texts/components/common";
import { getIngredientPieChartTexts } from "@/texts/components/charts";

export async function getSingleDayTexts(): Promise<SingleDayTexts> {
  const [t, nutritionalTableTexts, ingredientPieChartTexts] = await Promise.all(
    [
      getTranslations("components.days.SingleDayTexts"),
      getNutritionalTableTexts(),
      getIngredientPieChartTexts(),
    ],
  );
  return {
    meals: t("meals"),
    nutritionalTableTexts,
    ingredientPieChartTexts,
  };
}
