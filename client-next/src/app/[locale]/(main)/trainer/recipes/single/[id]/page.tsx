import { getUserWithMinRole } from "@/lib/user";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import SingeRecipePageContent from "@/app/[locale]/(main)/trainer/recipes/single/[id]/page-content";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getSingleRecipePageTexts } from "@/texts/pages";
interface Props {
  params: { locale: Locale; id: string };
}
export default async function SingleRecipePage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);

  const [user, texts] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getSingleRecipePageTexts(),
  ]);

  return (
    <div>
      <Suspense fallback={<LoadingSpinner />}>
        <SingeRecipePageContent authUser={user} id={id} {...texts} />
      </Suspense>
    </div>
  );
}
