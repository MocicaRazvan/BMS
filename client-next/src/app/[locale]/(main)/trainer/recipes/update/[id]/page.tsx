import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import { getRecipeFormTexts } from "@/texts/components/forms";
import { Suspense } from "react";
import Loader from "@/components/ui/spinner";
import UpdateRecipePageContent from "@/app/[locale]/(main)/trainer/recipes/update/[id]/page-content";
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
      "trainer.UpdateRecipe",
      "/trainer/recipes/update/" + id,
      locale,
    )),
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
    <Suspense
      fallback={
        <div className={"w-full flex items-center justify-center"}>
          <Loader />
        </div>
      }
    >
      <UpdateRecipePageContent
        id={id}
        authUser={user}
        {...recipeFormTexts}
        path={`/recipes/updateWithVideos/${id}`}
      />
    </Suspense>
  );
}
