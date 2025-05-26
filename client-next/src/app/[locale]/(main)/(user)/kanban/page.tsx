import { LocaleProps } from "@/navigation";
import { KanbanBoardTexts } from "@/components/kanban/kanban-board";
import { getKanbanPageTexts } from "@/texts/pages";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUser } from "@/lib/user";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import KanbanBoardWrapper from "@/components/kanban/kanban-board-wrapper";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.Kanban", "/kanban", locale)),
  };
}
export interface KanbanPageTexts {
  title: string;
  header: string;
  kanbanBoardTexts: KanbanBoardTexts;
}

export default async function KanbanPage({ params: { locale } }: LocaleProps) {
  unstable_setRequestLocale(locale);

  const [texts, user] = await Promise.all([getKanbanPageTexts(), getUser()]);

  return (
    <div className="space-y-10  w-full transition-all py-5 px-4 max-w-[1350px] mx-auto ">
      <Heading {...texts} />
      <Suspense fallback={<LoadingSpinner />}>
        <div>
          <KanbanBoardWrapper authUser={user} {...texts.kanbanBoardTexts} />
        </div>
      </Suspense>
    </div>
  );
}
