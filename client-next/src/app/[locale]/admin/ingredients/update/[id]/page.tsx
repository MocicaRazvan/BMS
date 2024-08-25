import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import LoadingSpinner from "@/components/common/loading-spinner";
import { getThemeSwitchTexts, ThemeSwitchTexts } from "@/texts/components/nav";
import { getUserWithMinRole } from "@/lib/user";
import {
  getAdminIngredientsCreatePageTexts,
  getAdminPageUpdateIngredientTexts,
} from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import { IngredientFormTexts } from "@/components/forms/ingredient-form";
import AdminIngredientsPageContent from "@/app/[locale]/admin/ingredients/update/[id]/page-content";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

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
  const [authUser, texts] = await Promise.all([
    getUserWithMinRole("ROLE_ADMIN"),
    getAdminPageUpdateIngredientTexts(),
  ]);
  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,
        authUser,
        menuTexts: texts.menuTexts,
        mappingKey: "admin",
      }}
    >
      <Suspense fallback={<LoadingSpinner />}>
        <AdminIngredientsPageContent
          id={id}
          authUser={authUser}
          ingredientFormTexts={texts.ingredientFormTexts}
        />
      </Suspense>
    </SidebarContentLayout>
  );
}
