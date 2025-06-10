import { Locale } from "@/navigation/navigation";
import SingleSubscriptionPageContent from "@/app/[locale]/(main)/(user)/subscriptions/single/[id]/page-content";
import { unstable_setRequestLocale } from "next-intl/server";
import { getSingleSubscriptionTexts } from "@/texts/pages";
import { getUser } from "@/lib/user";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import ScrollProgress from "@/components/common/scroll-progress";

interface Props {
  params: { locale: Locale; id: string };
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "user.SingleSubscription",
      "/subscriptions/single/" + id,
      locale,
    )),
  };
}

export default async function SingleSubscriptionPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts, user] = await Promise.all([
    getSingleSubscriptionTexts(),
    getUser(),
  ]);

  return (
    <>
      <ScrollProgress />
      <SingleSubscriptionPageContent id={id} authUser={user} {...texts} />
    </>
  );
}
