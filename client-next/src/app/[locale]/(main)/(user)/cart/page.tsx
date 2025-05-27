import { LocaleProps } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import CartPageContent from "@/app/[locale]/(main)/(user)/cart/page-content";
import { getCartPageContentTexts } from "@/texts/pages";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.Cart", "/cart", locale)),
  };
}
export default async function CartPage({ params: { locale } }: LocaleProps) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getCartPageContentTexts()]);

  return (
    <Suspense fallback={<LoadingSpinner />}>
      <CartPageContent {...texts} />
    </Suspense>
  );
}
