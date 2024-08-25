import { getTranslations } from "next-intl/server";
import { ElementHeaderTexts } from "@/components/common/element-header";
import { RadioSortTexts } from "@/components/common/radio-sort";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { getIngredientTableColumnTexts } from "@/texts/components/table";

export async function getElementHeaderTexts(): Promise<ElementHeaderTexts> {
  const t = await getTranslations("components.common.ElementHeaderTexts");
  return {
    notApproved: t("notApproved"),
    approved: t("approved"),
  };
}

export async function getRadioSortTexts(): Promise<RadioSortTexts> {
  const t = await getTranslations("components.common.RadioSortTexts");
  return {
    noSort: t("noSort"),
  };
}

export async function getNutritionalTableTexts(): Promise<NutritionalTableTexts> {
  const [ingredientColumnTexts, t] = await Promise.all([
    getIngredientTableColumnTexts(),
    getTranslations("components.common.NutritionalTableTexts"),
  ]);

  return {
    tableCaption: t("tableCaption"),
    ingredientColumnTexts,
  };
}
