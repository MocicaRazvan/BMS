import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SingeRecipePageContent, {
  SingleRecipePageTexts,
} from "@/app/[locale]/trainer/recipes/single/[id]/page-content";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getAdminRecipePageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import ScrollProgress from "@/components/common/scroll-progress";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

export interface AdminRecipePageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  header: string;
  title: string;
  menuTexts: SidebarMenuTexts;
  singleRecipePageTexts: SingleRecipePageTexts;
  findInSiteTexts: FindInSiteTexts;
}
interface Props {
  params: { locale: Locale; id: string };
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.SingleRecipe",
    "/admin/recipes/single/" + id,
    locale,
  );
}

export default async function AdminRecipePage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);

  const [texts] = await Promise.all([getAdminRecipePageTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        ...texts,
        mappingKey: "admin",
      }}
    >
      <ScrollProgress />
      <div className="w-full bg-background ">
        <div className="mt-5">
          <SingeRecipePageContent {...texts.singleRecipePageTexts} id={id} />
        </div>
      </div>
    </SidebarContentLayout>
  );
}
