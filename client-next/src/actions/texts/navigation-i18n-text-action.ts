"use server";
import { getUseNavigationGuardI18nTexts } from "@/texts/components/forms";

export default async function getUseNavigationGuardI18nTextsAction() {
  return await getUseNavigationGuardI18nTexts();
}
