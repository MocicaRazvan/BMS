import { Locale } from "@/navigation";
import SingleSubscriptionPageContent from "@/app/[locale]/(main)/(user)/subscriptions/single/[id]/page-content";
import { unstable_setRequestLocale } from "next-intl/server";
import { getSingleSubscriptionTexts } from "@/texts/pages";
import { getUser } from "@/lib/user";

interface Props {
  params: { locale: Locale; id: string };
}

export default async function SingleSubscriptionPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts, user] = await Promise.all([
    getSingleSubscriptionTexts(),
    getUser(),
  ]);

  return <SingleSubscriptionPageContent id={id} authUser={user} {...texts} />;
}
