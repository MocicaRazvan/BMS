import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import { getRecipeFormTexts } from "@/texts/components/forms";
import { Suspense } from "react";
import Loader from "@/components/ui/spinner";
import UpdateRecipePageContent from "@/app/[locale]/(main)/trainer/recipes/update/[id]/page-content";

interface Props {
  params: {
    locale: Locale;
    id: string;
  };
}

export default async function UpdateRecipePage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [user, recipeFormTexts] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getRecipeFormTexts("update"),
  ]);
  return (
    <Suspense fallback={<Loader />}>
      <UpdateRecipePageContent
        id={id}
        authUser={user}
        {...recipeFormTexts}
        path={`/recipes/updateWithVideos/${id}`}
      />
    </Suspense>
  );
}
