import { Locale } from "@/navigation";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SinglePostPageContent, {
  SinglePostPageTexts,
} from "@/app/[locale]/(main)/(user)/posts/single/[id]/page-content";
import { unstable_setRequestLocale } from "next-intl/server";
import { getAdminPostPageTexts } from "@/texts/pages";
import LoadingSpinner from "@/components/common/loading-spinner";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import ScrollProgress from "@/components/common/scroll-progress";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

interface Props {
  params: { locale: Locale; id: string };
}
export interface AdminPostPageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  header: string;
  title: string;
  menuTexts: SidebarMenuTexts;
  singlePostPageTexts: SinglePostPageTexts;
  findInSiteTexts: FindInSiteTexts;
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.SinglePost",
    "/admin/posts/single/" + id,
    locale,
  );
}

export default async function AdminPostPage({ params: { locale, id } }: Props) {
  unstable_setRequestLocale(locale);

  const [texts] = await Promise.all([getAdminPostPageTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        ...texts,
        mappingKey: "admin",
      }}
    >
      <ScrollProgress />
      <div className="w-full bg-background ">
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-5">
            <SinglePostPageContent {...texts.singlePostPageTexts} />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
