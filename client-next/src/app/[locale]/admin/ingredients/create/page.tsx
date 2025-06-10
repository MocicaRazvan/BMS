import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getThemeSwitchTexts } from "@/texts/components/nav";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import AdminIngredientsCreatePageContent from "@/app/[locale]/admin/ingredients/create/page-content";
import { IngredientFormTexts } from "@/components/forms/ingredient-form";
import { getAdminIngredientsCreatePageTexts } from "@/texts/pages";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

export interface AdminIngredientsCreatePageTexts {
  ingredientForm: IngredientFormTexts;
  title: string;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}
interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.CreateIngredient",
    "/admin/ingredients/create",
    locale,
  );
}
export default async function AdminIngredientsCreatePage({
  params: { locale },
}: Props) {
  unstable_setRequestLocale(locale);

  const [themeSwitchTexts, texts] = await Promise.all([
    getThemeSwitchTexts(),
    getAdminIngredientsCreatePageTexts(),
  ]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts,
        menuTexts: texts.menuTexts,
        mappingKey: "admin",
        findInSiteTexts: texts.findInSiteTexts,
      }}
    >
      <AdminIngredientsCreatePageContent texts={texts.ingredientForm} />
    </SidebarContentLayout>
  );
}
