import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getRecipeFormTexts } from "@/texts/components/forms";
import { getUserWithMinRole } from "@/lib/user";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import RecipeForm from "@/components/forms/recipe-form";
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
      "trainer.CreateRecipe",
      "/trainer/recipes/create",
      locale,
    )),
  };
}

export default async function CreateRecipePage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [user, recipeFormTexts] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getRecipeFormTexts("create"),
  ]);

  return (
    <main className="flex items-center justify-center px-6 py-10">
      <Suspense fallback={<LoadingSpinner />}>
        <RecipeForm
          authUser={user}
          {...recipeFormTexts}
          path={"/recipes/createWithVideos"}
          type="create"
        />
      </Suspense>
    </main>
  );
}
