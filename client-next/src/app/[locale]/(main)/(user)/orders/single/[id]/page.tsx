import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUser } from "@/lib/user";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import SingleOrderPageContent from "@/app/[locale]/(main)/(user)/orders/single/[id]/page-content";
import { getSingleOrderPageContentTexts } from "@/texts/pages";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: { locale: Locale; id: string };
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "user.SingleOrder",
      "/orders/single/" + id,
      locale,
    )),
  };
}

export default async function SingleOrder({ params: { locale, id } }: Props) {
  unstable_setRequestLocale(locale);

  const [user, texts] = await Promise.all([
    getUser(),
    getSingleOrderPageContentTexts(),
  ]);

  return (
    <Suspense fallback={<LoadingSpinner />}>
      <SingleOrderPageContent id={id} authUser={user} {...texts} />
    </Suspense>
  );
}
