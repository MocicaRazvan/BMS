import { Locale } from "@/navigation";
import { ReactNode } from "react";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import SidebarPanelLayout from "@/components/sidebar/sidebar-panel-layout";
import { SidebarToggleProvider } from "@/context/sidebar-toggle";
import { getSidebarLayoutTexts } from "@/texts/components/sidebar";

import {
  adminGroupLabels,
  adminLabels,
  adminSubLabels,
} from "@/components/sidebar/menu-list";

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
    getSidebarLayoutTexts(
      "admin",
      adminGroupLabels,
      adminLabels,
      adminSubLabels,
    ),
  ]);
  return (
    <SidebarToggleProvider>
      <SidebarPanelLayout {...texts} mappingKey={"admin"} authUser={user}>
        {children}
      </SidebarPanelLayout>
    </SidebarToggleProvider>
  );
}
