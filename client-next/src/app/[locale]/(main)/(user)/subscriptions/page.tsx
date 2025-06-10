import SubscriptionsPageContent from "@/app/[locale]/(main)/(user)/subscriptions/page-content";
import { unstable_setRequestLocale } from "next-intl/server";
import { getSubscriptionsPageContentTexts } from "@/texts/pages";
import { getSortingOptions } from "@/types/constants";
import { sortingPlansSortingOptionsKeys } from "@/texts/components/list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { LocaleProps } from "@/navigation/navigation";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.Subscriptions", "/subscriptions", locale)),
  };
}

export default async function SubscriptionsPage({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);

  const [texts] = await Promise.all([getSubscriptionsPageContentTexts()]);

  const plansOptions = getSortingOptions(
    sortingPlansSortingOptionsKeys,
    texts.sortingPlansSortingOptions,
  );

  return <SubscriptionsPageContent options={plansOptions} {...texts} />;
}
