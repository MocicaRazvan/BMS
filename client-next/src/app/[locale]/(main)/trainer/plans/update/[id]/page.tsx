import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import { getPlanFormTexts } from "@/texts/components/forms";
import Loader from "@/components/ui/spinner";
import { Suspense } from "react";
import UpdatePlanPageContent from "@/app/[locale]/(main)/trainer/plans/update/[id]/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: {
    locale: Locale;
    id: string;
  };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.UpdatePlan",
      "/trainer/plans/update/" + id,
      locale,
    )),
  };
}

export default async function UpdatePlanPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [user, planFormTexts] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getPlanFormTexts("update"),
  ]);

  return (
    <Suspense fallback={<Loader />}>
      <UpdatePlanPageContent
        authUser={user}
        id={id}
        {...planFormTexts}
        path={`/plans/updateWithImages/${id}`}
      />
    </Suspense>
  );
}
