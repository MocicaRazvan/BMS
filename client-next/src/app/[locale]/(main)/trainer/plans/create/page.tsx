import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import { getPlanFormTexts } from "@/texts/components/forms";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import PlanForm from "@/components/forms/plan-form";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.CreatePlan",
      "/trainer/plans/create",
      locale,
    )),
  };
}
export default async function CreatePlanPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [user, planFormTexts] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getPlanFormTexts("create"),
  ]);

  return (
    <main className="flex items-center justify-center px-6 py-10">
      <Suspense fallback={<LoadingSpinner />}>
        <PlanForm
          authUser={user}
          {...planFormTexts}
          path={"/plans/createWithImages"}
          type="create"
        />
      </Suspense>
    </main>
  );
}
