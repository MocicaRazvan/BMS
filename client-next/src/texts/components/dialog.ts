"use server";
import { getTranslations } from "next-intl/server";
import React, { ReactNode } from "react";
import { BaseDialogTexts } from "@/components/dialogs/delete-model";
import { AlertDialogApproveTexts } from "@/components/dialogs/approve-model";
import { AlertDialogMakeTrainerTexts } from "@/components/dialogs/user/make-trainer-alert";
import { AlertDialogToggleDisplayTexts } from "@/components/dialogs/ingredients/ingredient-toggle-display";
import { AddKanbanColumnTexts } from "@/components/dialogs/kanban/add-kanban-column";
import { FormType, getButtonSubmitTexts } from "@/texts/components/forms";
import { DeleteKanbanItemTexts } from "@/components/dialogs/kanban/delete-kanban-item";
import { DialogKanbanTaskTexts } from "@/components/dialogs/kanban/dialog-kanban-task";
import { KanbanTaskType } from "@/types/dto";

export interface DeleteDialogTexts extends BaseDialogTexts {}

export async function getAlertDialogDeleteTexts(
  title: string,
): Promise<DeleteDialogTexts> {
  const t = await getTranslations("components.dialogs.DeleteDialogTexts");
  return {
    anchor: t("anchor"),
    title: t("title"),
    description: t.rich("description", {
      title,
      b: (chunks) =>
        React.createElement(
          "p",
          { className: "font-bold text-lg inline me-2" },
          chunks,
        ),
    }),
    cancel: t("cancel"),
    confirm: t("confirm"),
  };
}

export async function getAlertDialogApproveTexts(
  title: string,
  approved: string,
): Promise<AlertDialogApproveTexts> {
  const t = await getTranslations("components.dialogs.AlertDialogApproveTexts");
  return {
    anchor: t("anchor", { approved }),
    title: t("title"),
    description: t.rich("description", {
      title,
      approved,
      b: (chunks) =>
        React.createElement(
          "p",
          { className: "font-bold text-lg inline me-2" },
          chunks,
        ),
    }),
    cancel: t("cancel"),
    confirm: t("confirm", { approved }),
    toast: t.rich("toast", {
      title,
      approved,
      b: (chunks) =>
        React.createElement(
          "p",
          { className: "font-bold text-lg inline me-2" },
          chunks,
        ),
    }),
  };
}

export async function getAlertDialogMakeTrainerTexts(
  email: string,
): Promise<AlertDialogMakeTrainerTexts> {
  const t = await getTranslations(
    "components.dialogs.AlertDialogMakeTrainerTexts",
  );
  return {
    anchor: t("anchor"),
    title: t("title"),
    description: t("description"),
    cancel: t("cancel"),
    confirm: t("confirm"),
    toast: t.rich("toast", {
      email,
      b: (chunks) =>
        React.createElement(
          "p",
          { className: "font-bold text-lg inline me-2" },
          chunks,
        ),
    }),
  };
}

export async function getAlertDialogToggleDisplayTexts(
  name: string,
  display: string,
): Promise<AlertDialogToggleDisplayTexts> {
  const t = await getTranslations(
    "components.dialogs.AlertDialogToggleDisplayTexts",
  );
  return {
    anchor: t("anchor", { display }),
    title: t("title"),
    description: t.rich("description", {
      name,
      display,
      b: (chunks) =>
        React.createElement(
          "p",
          { className: "font-bold text-lg inline me-2" },
          chunks,
        ),
    }),
    cancel: t("cancel"),
    confirm: t("confirm", { display }),
    toast: t.rich("toast", {
      name,
      display,
      b: (chunks) =>
        React.createElement(
          "p",
          { className: "font-bold text-lg inline me-2" },
          chunks,
        ),
    }),
  };
}

export async function getAddKanbanColumnTexts(): Promise<AddKanbanColumnTexts> {
  const [t, buttonSubmitTexts] = await Promise.all([
    getTranslations("components.dialogs.AddKanbanColumnTexts"),
    getButtonSubmitTexts(),
  ]);
  return {
    addColumn: t("addColumn"),
    title: t("title"),
    description: t("description"),
    titleLabel: t("titleLabel"),
    titlePlaceholder: t("titlePlaceholder"),
    buttonSubmitTexts,
    error: t("error"),
  };
}

export async function getDeleteKanbanItemTexts(
  type: "column" | "task",
): Promise<DeleteKanbanItemTexts> {
  const [t, buttonSubmitTexts] = await Promise.all([
    getTranslations("components.dialogs.DeleteKanbanItemTexts"),
    getButtonSubmitTexts(),
  ]);

  const intlType = t(`type.${type}`);
  return {
    title: t("title"),
    description: t.rich("description", {
      type: intlType,
      b: (chunks) =>
        React.createElement(
          "p",
          { className: "font-bold text-lg inline mx-1" },
          chunks,
        ),
    }),
    buttonSubmitTexts,
    error: t("error"),
    cancel: t("cancel"),
    confirm: t("confirm"),
  };
}

export async function getDialogKanbanTaskTexts(
  type: FormType,
): Promise<DialogKanbanTaskTexts> {
  const [t, buttonSubmitTexts] = await Promise.all([
    getTranslations("components.dialogs.DialogKanbanTaskTexts"),
    getButtonSubmitTexts(),
  ]);
  return {
    title: t("title", { type }),
    description: t("description", { type }),
    buttonSubmitTexts,
    error: t("error", { type }),
    addTask: t("addTask"),
    inputs: ["content", "type"].reduce(
      (acc, key) => ({
        ...acc,
        [key]: {
          label: t(`inputs.${key}.label`),
          placeholder: t(`inputs.${key}.placeholder`),
        },
      }),
      {} as Record<
        "content" | "type",
        {
          label: string;
          placeholder: string;
        }
      >,
    ),
    types: ["LOW", "NORMAL", "URGENT"].reduce(
      (acc, key) => ({ ...acc, [key]: t(`types.${key}`) }),
      {} as Record<KanbanTaskType, string>,
    ),
  };
}
