import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { AdminMenuTexts } from "@/components/admin/menu-list";
import { KanbanBoardTexts } from "@/components/kanban/kanban-board";
import Heading from "@/components/common/heading";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import { getAdminKanbanTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import KanbanBoardWrapper from "@/components/kanban/kanban-board-wrapper";

interface Props {
  params: { locale: Locale };
}

export interface AdminKanbanTexts {
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: AdminMenuTexts;
  kanbanBoardTexts: KanbanBoardTexts;
}

export default async function AdminKanban({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);

  const [texts, authUser] = await Promise.all([
    getAdminKanbanTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);

  return (
    <AdminContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,
        authUser,
        menuTexts: texts.menuTexts,
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
    </AdminContentLayout>
  );
}
