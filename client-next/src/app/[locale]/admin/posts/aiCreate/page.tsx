import { Locale } from "@/navigation";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { unstable_setRequestLocale } from "next-intl/server";
import { getThemeSwitchTexts } from "@/texts/components/nav";
import { getUserWithMinRole } from "@/lib/user";
import LoadingSpinner from "@/components/common/loading-spinner";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getAdminAIPostsCreate } from "@/texts/pages";
import AdminAIPostsCreateContent, {
  AdminAIPostsCreateContentTexts,
} from "@/app/[locale]/admin/posts/aiCreate/page-content";

interface Props {
  params: { locale: Locale };
}

export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.CreatePostAI",
    "admin/posts/aiCreate",
    locale,
  );
}
export interface AdminAIPostsCreateTexts
  extends AdminAIPostsCreateContentTexts {
  title: string;
  menuTexts: SidebarMenuTexts;
}
export default async function AdminAIPostsCreate({
  params: { locale },
}: Props) {
  unstable_setRequestLocale(locale);
  const [themeSwitchTexts, authUser, texts] = await Promise.all([
    getThemeSwitchTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
    getAdminAIPostsCreate(),
  ]);
  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts,
        authUser,
        menuTexts: texts.menuTexts,
        mappingKey: "admin",
      }}
    >
      <Suspense fallback={<LoadingSpinner />}>
        <AdminAIPostsCreateContent {...texts} authUser={authUser} />
      </Suspense>
    </SidebarContentLayout>
  );
}
