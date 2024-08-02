import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUser } from "@/lib/user";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import SingleOrderPageContent from "@/app/[locale]/(main)/(user)/orders/single/[id]/page-content";
import { getSingleOrderPageContentTexts } from "@/texts/pages";

interface Props {
  params: { locale: Locale; id: string };
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
