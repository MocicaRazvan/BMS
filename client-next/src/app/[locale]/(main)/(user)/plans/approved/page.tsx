import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getApprovedPlansTexts } from "@/texts/pages";
import { getSortingOptions } from "@/lib/constants";
import { sortingPlansSortingOptionsKeys } from "@/texts/components/list";
import { getUser } from "@/lib/user";
import PlanApprovedPageContent from "@/app/[locale]/(main)/(user)/plans/approved/page-content";

interface Props {
  params: { locale: Locale };
}

export default async function PlanApprovedPage({ params: { locale } }: Props) {
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
