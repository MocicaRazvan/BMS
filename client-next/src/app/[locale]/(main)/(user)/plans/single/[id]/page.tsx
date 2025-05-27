import { Locale } from "@/navigation";
import { getUserPlanPageContentTexts } from "@/texts/pages";
import { unstable_setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import UserPlanPageContent from "@/app/[locale]/(main)/(user)/plans/single/[id]/page-content";
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
      "user.SinglePlan",
      "/plans/single/" + id,
      locale,
    )),
  };
}
export default async function UserPlanPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getUserPlanPageContentTexts()]);
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <ScrollProgress />
      <UserPlanPageContent {...texts} />
    </Suspense>
  );
}
