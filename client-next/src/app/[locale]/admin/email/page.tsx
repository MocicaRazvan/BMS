import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import AdminEmail, { AdminEmailTexts } from "@/components/forms/admin-email";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getUserWithMinRole } from "@/lib/user";
import { getAdminEmailPageTexts } from "@/texts/pages";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import { Metadata } from "next";
import { getIntlMetadata, getMetadataValues } from "@/texts/metadata";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

interface Props {
  params: { locale: Locale };
}

export interface AdminEmailPageTexts {
  adminEmail: AdminEmailTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("admin.Email", "/admin/email", locale);
}

export default async function AdminEmailPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [authUser, texts] = await Promise.all([
    getUserWithMinRole("ROLE_ADMIN"),
    getAdminEmailPageTexts(),
  ]);
  const metadataValues = await getMetadataValues(authUser, locale);

  return (
    <SidebarContentLayout
      navbarProps={{
        authUser,
        ...texts,
        mappingKey: "admin",
        metadataValues,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading {...texts} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full w-full">
            <AdminEmail {...texts.adminEmail} authUser={authUser} />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
