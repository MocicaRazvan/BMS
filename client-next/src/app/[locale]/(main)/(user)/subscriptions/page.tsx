import { Locale } from "@/navigation";
import SubscriptionsPageContent from "@/app/[locale]/(main)/(user)/subscriptions/page-content";
import { unstable_setRequestLocale } from "next-intl/server";
import { getSubscriptionsPageContentTexts } from "@/texts/pages";
import { getUser } from "@/lib/user";
import { getSortingOptions } from "@/lib/constants";
import { sortingPlansSortingOptionsKeys } from "@/texts/components/list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: { locale: Locale };
}

export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.Subscriptions", "/subscriptions", locale)),
  };
}

export default async function SubscriptionsPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);

  const [texts, user] = await Promise.all([
    getSubscriptionsPageContentTexts(),
    getUser(),
  ]);

  const plansOptions = getSortingOptions(
    sortingPlansSortingOptionsKeys,
    texts.sortingPlansSortingOptions,
  );

  return (
    <SubscriptionsPageContent
      options={plansOptions}
      authUser={user}
      {...texts}
    />
  );
}
