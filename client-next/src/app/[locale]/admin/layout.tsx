import { Locale } from "@/navigation";
import { ReactNode } from "react";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import AdminPanelLayout from "@/components/admin/admin-panel-layout";
import { SidebarToggleProvider } from "@/context/sidebar-toggle";
import {
  getAdminLayoutTexts,
  getAdminMenuTexts,
} from "@/texts/components/admin";

interface Props {
  params: { locale: Locale };
  children: ReactNode;
}

export default async function AdminLayout({
  params: { locale },
  children,
}: Props) {
  unstable_setRequestLocale(locale);
  const [user, texts] = await Promise.all([
    getUserWithMinRole("ROLE_ADMIN"),
    getAdminLayoutTexts(),
  ]);
  return (
    <SidebarToggleProvider>
      <AdminPanelLayout {...texts}>{children}</AdminPanelLayout>
    </SidebarToggleProvider>
  );
}
