import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import LoadingSpinner from "@/components/common/loading-spinner";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getUserWithMinRole } from "@/lib/user";
import { getAdminPageDuplicateIngredientTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import { IngredientFormTexts } from "@/components/forms/ingredient-form";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import AdminPageDuplicateIngredientContent from "@/app/[locale]/admin/ingredients/duplicate/[id]/page-content";

interface Props {
  params: {
    id: string;
    locale: Locale;
  };
}

export interface AdminPageDuplicateIngredientTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  ingredientFormTexts: IngredientFormTexts;
  title: string;
  menuTexts: SidebarMenuTexts;
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.DuplicateIngredient",
    "/admin/ingredients/duplicate/" + id,
    locale,
  );
}

export default async function AdminPageDuplicateIngredient({
  params: { id, locale },
}: Props) {
  unstable_setRequestLocale(locale);
  const [authUser, texts] = await Promise.all([
    getUserWithMinRole("ROLE_ADMIN"),
    getAdminPageDuplicateIngredientTexts(),
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
        <AdminPageDuplicateIngredientContent
          id={id}
          authUser={authUser}
          ingredientFormTexts={texts.ingredientFormTexts}
        />
      </Suspense>
    </SidebarContentLayout>
  );
}
