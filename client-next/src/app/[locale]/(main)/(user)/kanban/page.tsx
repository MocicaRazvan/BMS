import { LocaleProps } from "@/navigation/navigation";
import { KanbanBoardTexts } from "@/components/kanban/kanban-board";
import { getKanbanPageTexts } from "@/texts/pages";
import { unstable_setRequestLocale } from "next-intl/server";
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

  const [texts] = await Promise.all([getKanbanPageTexts()]);

  return (
    <div className="space-y-10 w-full py-5 px-4 max-w-[1350px] mx-auto min-h-screen">
      <Heading {...texts} />
      <div>
        <KanbanBoardWrapper {...texts.kanbanBoardTexts} />
      </div>
    </div>
  );
}
