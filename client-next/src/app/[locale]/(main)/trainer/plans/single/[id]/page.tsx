import { Locale } from "@/navigation";
import { getUserWithMinRole } from "@/lib/user";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import SingleTrainerPlanPageContent from "@/app/[locale]/(main)/trainer/plans/single/[id]/page-content";
import { getSingleTrainerPlanPageTexts } from "@/texts/pages";
import { unstable_setRequestLocale } from "next-intl/server";
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
      "trainer.SinglePlan",
      "/trainer/plans/single/" + id,
      locale,
    )),
  };
}
export default async function SingleTrainerPlanPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [user, texts] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getSingleTrainerPlanPageTexts(),
  ]);

  return (
    <div>
      <Suspense fallback={<LoadingSpinner />}>
        <SingleTrainerPlanPageContent id={id} authUser={user} {...texts} />
      </Suspense>
    </div>
  );
}
