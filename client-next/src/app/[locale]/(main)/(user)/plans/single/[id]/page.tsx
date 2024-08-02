import { Locale } from "@/navigation";
import { getUserPlanPageContentTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import { unstable_setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import UserPlanPageContent from "@/app/[locale]/(main)/(user)/plans/single/[id]/page-content";

interface Props {
  params: { locale: Locale };
}

export default async function UserPlanPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [user, texts] = await Promise.all([
    getUserWithMinRole("ROLE_USER"),
    getUserPlanPageContentTexts(),
  ]);
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <UserPlanPageContent authUser={user} {...texts} />
    </Suspense>
  );
}
