"use server";
import { getTranslations } from "next-intl/server";
import { CheckoutSchemaTexts } from "@/types/forms";

export async function getCheckoutSchemaTexts(
  price: number,
): Promise<CheckoutSchemaTexts> {
  const t = await getTranslations("zod.CheckoutSchemaTexts");
  return {
    confirmPrice: t(
      "confirmPrice",
      { price },
      {
        number: {
          currency: {
            style: "currency",
            currency: "EUR",
          },
        },
      },
    ),
  };
}
