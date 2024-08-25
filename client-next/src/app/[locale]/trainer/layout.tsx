import { ReactNode } from "react";
import { getUserWithMinRole } from "@/lib/user";
import { unstable_setRequestLocale } from "next-intl/server";
import { Locale } from "@/navigation";
import { getSidebarLayoutTexts } from "@/texts/components/sidebar";
import { SidebarToggleProvider } from "@/context/sidebar-toggle";
import {
  trainerGroupLabels,
  trainerLabels,
  trainerSubLabels,
} from "@/components/sidebar/menu-list";
import SidebarPanelLayout from "@/components/sidebar/sidebar-panel-layout";

export default async function TrainerLayout({
  children,
  params: { locale },
}: {
  children: ReactNode;
  params: { locale: Locale };
}) {
  unstable_setRequestLocale(locale);
  const [user, texts] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getSidebarLayoutTexts(
      "trainer",
      trainerGroupLabels,
      trainerLabels,
      trainerSubLabels,
    ),
  ]);

  return (
    <SidebarToggleProvider>
      <SidebarPanelLayout {...texts} mappingKey={"trainer"} authUser={user}>
        {children}
      </SidebarPanelLayout>
    </SidebarToggleProvider>
  );
}
