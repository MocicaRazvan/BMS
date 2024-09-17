import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import { getRecipeFormTexts } from "@/texts/components/forms";
import { Suspense } from "react";
import UpdateRecipePageContent from "@/app/[locale]/trainer/recipes/update/[id]/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getUpdateRecipePageTexts } from "@/texts/pages";
import LoadingSpinner from "@/components/common/loading-spinner";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";

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
export interface UpdateRecipePageTexts {
  recipeFormTexts: Awaited<ReturnType<typeof getRecipeFormTexts>>;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
}

export default async function UpdateRecipePage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [authUser, { recipeFormTexts, ...rest }] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getUpdateRecipePageTexts(),
  ]);
  return (
    <SidebarContentLayout
      navbarProps={{
        title: recipeFormTexts.baseFormTexts.header,
        ...rest,
        authUser,
        mappingKey: "trainer",
      }}
    >
      <main className="flex items-center justify-center px-6 py-10">
        <Suspense fallback={<LoadingSpinner />}>
          <UpdateRecipePageContent
            id={id}
            authUser={authUser}
            {...recipeFormTexts}
            path={`/recipes/updateWithVideos/${id}`}
          />
        </Suspense>
      </main>{" "}
    </SidebarContentLayout>
  );
}
