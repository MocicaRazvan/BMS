import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import AdminEmail, { AdminEmailTexts } from "@/components/forms/admin-email";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { AdminMenuTexts } from "@/components/admin/menu-list";
import { getUserWithMinRole } from "@/lib/user";
import { getAdminEmailPageTexts } from "@/texts/pages";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import { Suspense } from "react";

interface Props {
  params: { locale: Locale };
}

export interface AdminEmailPageTexts {
  adminEmail: AdminEmailTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: AdminMenuTexts;
}

export default async function AdminEmailPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [authUser, texts] = await Promise.all([
    getUserWithMinRole("ROLE_ADMIN"),
    getAdminEmailPageTexts(),
  ]);
  return (
    <AdminContentLayout
      navbarProps={{
        authUser,
        ...texts,
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
    </AdminContentLayout>
  );
}
