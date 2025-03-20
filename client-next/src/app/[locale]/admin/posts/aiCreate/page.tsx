import { Locale } from "@/navigation";
import { Metadata } from "next";
import { getIntlMetadata, getMetadataValues } from "@/texts/metadata";
import { unstable_setRequestLocale } from "next-intl/server";
import {
  getFindInSiteTexts,
  getThemeSwitchTexts,
} from "@/texts/components/nav";
import { getUserWithMinRole } from "@/lib/user";
import LoadingSpinner from "@/components/common/loading-spinner";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getAdminAIPostsCreate } from "@/texts/pages";
import AdminAIPostsCreateContent, {
  AdminAIPostsCreateContentTexts,
} from "@/app/[locale]/admin/posts/aiCreate/page-content";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

interface Props {
  params: { locale: Locale };
}

export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
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
}: Props) {
  unstable_setRequestLocale(locale);
  const [themeSwitchTexts, authUser, texts, findInSiteTexts] =
    await Promise.all([
      getThemeSwitchTexts(),
      getUserWithMinRole("ROLE_ADMIN"),
      getAdminAIPostsCreate(),
      getFindInSiteTexts(),
    ]);
  const metadataValues = await getMetadataValues(authUser, locale);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts,
        authUser,
        menuTexts: texts.menuTexts,
        mappingKey: "admin",
        findInSiteTexts,
        metadataValues,
      }}
    >
      <Suspense fallback={<LoadingSpinner />}>
        <div className="w-full flex items-center justify-center">
          <AdminAIPostsCreateContent {...texts} authUser={authUser} />
        </div>
      </Suspense>
    </SidebarContentLayout>
  );
}
