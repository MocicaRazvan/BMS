import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUser } from "@/lib/user";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import CartPageContent from "@/app/[locale]/(main)/(user)/cart/page-content";
import { getCartPageContentTexts } from "@/texts/pages";

interface Props {
  params: { locale: Locale };
}
export default async function CartPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [user, texts] = await Promise.all([
    getUser(),
    getCartPageContentTexts(),
  ]);

  return (
    <Suspense fallback={<LoadingSpinner />}>
      <CartPageContent authUser={user} {...texts} />
    </Suspense>
  );
}
