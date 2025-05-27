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
import { AuthUserMinRoleProvider } from "@/context/auth-user-min-role-context";

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
    <AuthUserMinRoleProvider minRole="ROLE_TRAINER">
      <SidebarToggleProvider>
        <SidebarPanelLayout {...texts} mappingKey={"trainer"}>
          {children}
        </SidebarPanelLayout>
      </SidebarToggleProvider>
    </AuthUserMinRoleProvider>
  );
}
