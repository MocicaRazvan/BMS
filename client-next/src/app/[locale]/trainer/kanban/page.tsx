import { Locale } from "@/navigation";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { KanbanBoardTexts } from "@/components/kanban/kanban-board";
import { unstable_setRequestLocale } from "next-intl/server";
import { getTrainerKanbanPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import KanbanBoardWrapper from "@/components/kanban/kanban-board-wrapper";
import { getUser } from "@/lib/user";
import { Metadata } from "next";
import { getIntlMetadata, getMetadataValues } from "@/texts/metadata";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

interface Props {
  params: { locale: Locale };
}

export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("trainer.Kanban", "/trainer/kanban", locale);
}

export interface TrainerKanbanPageTexts {
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  kanbanBoardTexts: KanbanBoardTexts;
  findInSiteTexts: FindInSiteTexts;
}

export default async function TrainerKanban({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);

  const [authUser, texts] = await Promise.all([
    getUser(),
    getTrainerKanbanPageTexts(),
  ]);
  const metadataValues = await getMetadataValues(authUser, locale);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,
        authUser,
        menuTexts: texts.menuTexts,
        mappingKey: "admin",
        findInSiteTexts: texts.findInSiteTexts,
        metadataValues,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading {...texts} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full ">
            <KanbanBoardWrapper
              authUser={authUser}
              {...texts.kanbanBoardTexts}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
