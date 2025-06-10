import { Locale } from "@/navigation/navigation";
import { ReactNode } from "react";
import { unstable_setRequestLocale } from "next-intl/server";
import SidebarPanelLayout from "@/components/sidebar/sidebar-panel-layout";
import { SidebarToggleProvider } from "@/context/sidebar-toggle";
import { getSidebarLayoutTexts } from "@/texts/components/sidebar";

import {
  adminGroupLabels,
  adminLabels,
  adminSubLabels,
} from "@/components/sidebar/menu-list";
import ArchiveQueueUpdateProvider from "@/context/archive-queue-update-context";
import { getArchiveQueueTitleForPrefixes } from "@/texts/components/common";
import { AuthUserMinRoleProvider } from "@/context/auth-user-min-role-context";

interface Props {
  params: { locale: Locale };
  children: ReactNode;
}

export default async function AdminLayout({
  params: { locale },
  children,
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts, queueTexts] = await Promise.all([
    getSidebarLayoutTexts(
      "admin",
      adminGroupLabels,
      adminLabels,
      adminSubLabels,
    ),
    getArchiveQueueTitleForPrefixes(),
  ]);
  return (
    <AuthUserMinRoleProvider minRole="ROLE_ADMIN">
      <SidebarToggleProvider>
        <SidebarPanelLayout {...texts} mappingKey="admin">
          <ArchiveQueueUpdateProvider texts={queueTexts}>
            {children}
          </ArchiveQueueUpdateProvider>
        </SidebarPanelLayout>
      </SidebarToggleProvider>
    </AuthUserMinRoleProvider>
  );
}
