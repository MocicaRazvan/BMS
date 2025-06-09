import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { unstable_setRequestLocale } from "next-intl/server";
import {
  getFindInSiteTexts,
  getThemeSwitchTexts,
} from "@/texts/components/nav";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getAdminAIPostsCreate } from "@/texts/pages";
import AdminAIPostsCreateContent, {
  AdminAIPostsCreateContentTexts,
} from "@/app/[locale]/admin/posts/aiCreate/page-content";
import { LocaleProps } from "@/navigation";
import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.CreatePostAI",
    "/admin/posts/aiCreate",
    locale,
  );
}
export interface AdminAIPostsCreateTexts
  extends AdminAIPostsCreateContentTexts {
  title: string;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}
export default async function AdminAIPostsCreate({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const [themeSwitchTexts, texts, findInSiteTexts] = await Promise.all([
    getThemeSwitchTexts(),
    getAdminAIPostsCreate(),
    getFindInSiteTexts(),
  ]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts,
        menuTexts: texts.menuTexts,
        mappingKey: "admin",
        findInSiteTexts,
      }}
    >
      <div className="w-full flex items-center justify-center">
        <AdminAIPostsCreateContent {...texts} />
      </div>
    </SidebarContentLayout>
  );
}
