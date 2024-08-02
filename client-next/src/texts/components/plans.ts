import { AddToCartBtnTexts } from "@/components/plans/add-to-cart-btn";
import { getTranslations } from "next-intl/server";

export async function getAddToCartBtnTexts(): Promise<AddToCartBtnTexts> {
  const t = await getTranslations("components.plans.AddToCartBtnTexts");
  return {
    addToCart: t("addToCart"),
    finishOrder: t("finishOrder"),
    successDescription: t("successDescription"),
    toastAction: t("toastAction"),
    alreadyBought: t("alreadyBought"),
  };
}
