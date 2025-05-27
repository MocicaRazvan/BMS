import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { KanbanBoardTexts } from "@/components/kanban/kanban-board";
import Heading from "@/components/common/heading";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { getAdminKanbanTexts } from "@/texts/pages";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import KanbanBoardWrapper from "@/components/kanban/kanban-board-wrapper";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

interface Props {
  params: { locale: Locale };
}

export interface AdminKanbanTexts {
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  kanbanBoardTexts: KanbanBoardTexts;
  findInSiteTexts: FindInSiteTexts;
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("admin.Kanban", "/admin/kanban", locale);
}

export default async function AdminKanban({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);

  const [texts] = await Promise.all([getAdminKanbanTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,
        menuTexts: texts.menuTexts,
        mappingKey: "admin",
        findInSiteTexts: texts.findInSiteTexts,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading {...texts} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full ">
            <KanbanBoardWrapper {...texts.kanbanBoardTexts} />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
