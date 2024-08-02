import { KanbanTaskCardTexts } from "@/components/kanban/kanban-task-card";
import { getTranslations } from "next-intl/server";
import {
  getAddKanbanColumnTexts,
  getDeleteKanbanItemTexts,
  getDialogKanbanTaskTexts,
} from "@/texts/components/dialog";
import { KanbanColumnContainerTexts } from "@/components/kanban/kanban-column-container";
import { KanbanBoardTexts } from "@/components/kanban/kanban-board";

export async function getKanbanTaskCardTexts(): Promise<KanbanTaskCardTexts> {
  const [t, dialogKanbanTaskTexts, deleteKanbanItemTexts] = await Promise.all([
    getTranslations("components.kanban.KanbanTaskCardTexts"),
    getDialogKanbanTaskTexts("update"),
    getDeleteKanbanItemTexts("task"),
  ]);

  return {
    types: {
      LOW: t("types.LOW"),
      NORMAL: t("types.NORMAL"),
      URGENT: t("types.URGENT"),
    },
    dialogKanbanTaskTexts,
    deleteKanbanItemTexts,
  };
}

export async function getKanbanColumnContainerTexts(): Promise<KanbanColumnContainerTexts> {
  const [t, kanbanCardTaskTexts, deleteKanbanItemTexts, dialogKanbanTaskTexts] =
    await Promise.all([
      getTranslations("components.kanban.KanbanColumnContainerTexts"),
      getKanbanTaskCardTexts(),
      getDeleteKanbanItemTexts("column"),
      getDialogKanbanTaskTexts("create"),
    ]);

  return {
    kanbanCardTaskTexts,
    deleteKanbanItemTexts,
    dialogKanbanTaskTexts,
    tasksLabel: t("tasksLabel"),
  };
}

export async function getKanbanBoardTexts(): Promise<KanbanBoardTexts> {
  const [
    kanbanCardTaskTexts,
    kanbanColumnContainerTexts,
    addKanbanColumnTexts,
  ] = await Promise.all([
    getKanbanTaskCardTexts(),
    getKanbanColumnContainerTexts(),
    getAddKanbanColumnTexts(),
  ]);

  return {
    kanbanCardTaskTexts,
    kanbanColumnContainerTexts,
    addKanbanColumnTexts,
  };
}
