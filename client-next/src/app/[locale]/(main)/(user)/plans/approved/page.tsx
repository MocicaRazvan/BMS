import { unstable_setRequestLocale } from "next-intl/server";
import { getApprovedPlansTexts } from "@/texts/pages";
import { getSortingOptions } from "@/lib/constants";
import { sortingPlansSortingOptionsKeys } from "@/texts/components/list";
import { getUser } from "@/lib/user";
import PlanApprovedPageContent from "@/app/[locale]/(main)/(user)/plans/approved/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { LocaleProps } from "@/navigation";

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
  const [texts, authUser] = await Promise.all([
    getApprovedPlansTexts(),
    getUser(),
  ]);
  const plansOptions = getSortingOptions(
    sortingPlansSortingOptionsKeys,
    texts.sortingPlansSortingOptions,
  );

  return (
    <PlanApprovedPageContent
      options={plansOptions}
      {...texts}
      authUser={authUser}
    />
  );
}
