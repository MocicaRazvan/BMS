import { unstable_setRequestLocale } from "next-intl/server";
import { getApprovedPlansTexts } from "@/texts/pages";
import { getSortingOptions } from "@/types/constants";
import { sortingPlansSortingOptionsKeys } from "@/texts/components/list";
import PlanApprovedPageContent from "@/app/[locale]/(main)/(user)/plans/approved/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { LocaleProps } from "@/navigation/navigation";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.ApprovedPlans", "/plans/approved", locale)),
  };
}
export default async function PlanApprovedPage({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getApprovedPlansTexts()]);
  const plansOptions = getSortingOptions(
    sortingPlansSortingOptionsKeys,
    texts.sortingPlansSortingOptions,
  );

  return <PlanApprovedPageContent options={plansOptions} {...texts} />;
}
