import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import LoadingSpinner from "@/components/common/loading-spinner";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getAdminPageUpdateIngredientTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import { IngredientFormTexts } from "@/components/forms/ingredient-form";
import AdminIngredientsPageContent from "@/app/[locale]/admin/ingredients/update/[id]/page-content";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: {
    id: string;
    locale: Locale;
  };
}

export interface AdminPageUpdateIngredientTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  ingredientFormTexts: IngredientFormTexts;
  title: string;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.UpdateIngredient",
    "/admin/ingredients/update/" + id,
    locale,
  );
}

export default async function AdminPageUpdateIngredient({
  params: { id, locale },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getAdminPageUpdateIngredientTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,

        menuTexts: texts.menuTexts,
        mappingKey: "admin",
        findInSiteTexts: texts.findInSiteTexts,
      }}
    >
      <Suspense fallback={<LoadingSpinner />}>
        <AdminIngredientsPageContent
          id={id}
          ingredientFormTexts={texts.ingredientFormTexts}
        />
      </Suspense>
    </SidebarContentLayout>
  );
}
