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
import ArchiveQueueUpdateProvider from "@/context/archive-queue-update-context";
import { getArchiveQueueTitleForPrefixes } from "@/texts/components/common";

interface Props {
  params: { locale: Locale };
  children: ReactNode;
}

export default async function AdminLayout({
  params: { locale },
  children,
}: Props) {
  unstable_setRequestLocale(locale);
  const [user, texts, queueTexts] = await Promise.all([
    getUserWithMinRole("ROLE_ADMIN"),
    getSidebarLayoutTexts(
      "admin",
      adminGroupLabels,
      adminLabels,
      adminSubLabels,
    ),
    getArchiveQueueTitleForPrefixes(),
  ]);
  return (
    <SidebarToggleProvider>
      <SidebarPanelLayout {...texts} mappingKey="admin" authUser={user}>
        <ArchiveQueueUpdateProvider authUser={user} texts={queueTexts}>
          {children}
        </ArchiveQueueUpdateProvider>
      </SidebarPanelLayout>
    </SidebarToggleProvider>
  );
}
