"use server";
import { FormatTypeTexts } from "@/components/editor/format-type";
import { getTranslations } from "next-intl/server";
import { EditorEmojiPickerTexts } from "@/components/editor/editor-emoji-picker";
import { EditorToolbarTexts } from "@/components/editor/toolbar";
import { EditorTexts } from "@/components/editor/editor";
import {
  TextAlignmentGroupTexts,
  TextAlignmentsTexts,
  TextStyleGroupTexts,
  UndoRedoGroupTexts,
} from "@/components/editor/editor-allignements";

export async function getFormatTypeTexts(): Promise<FormatTypeTexts> {
  const t = await getTranslations("components.editor.FormatTypeTexts");
  return {
    paragraph: t("paragraph"),
  };
}
export async function getEditorEmojiPickerTexts(): Promise<EditorEmojiPickerTexts> {
  const t = await getTranslations("components.editor.EditorEmojiPickerTexts");
  return {
    searchPlaceholder: t("searchPlaceholder"),
  };
}

export async function getEditorToolbarTexts(): Promise<EditorToolbarTexts> {
  const [formatTypeTexts, editorEmojiPickerTexts] = await Promise.all([
    getFormatTypeTexts(),
    getEditorEmojiPickerTexts(),
  ]);
  return {
    formatTypeTexts,
    editorEmojiPickerTexts,
  };
}

export async function getTextStyleGroupTexts(): Promise<TextStyleGroupTexts> {
  const t = await getTranslations("components.editor.TextStyleGroup");
  return {
    bold: t("bold"),
    italic: t("italic"),
    underline: t("underline"),
    strike: t("strike"),
    bulletList: t("bulletList"),
    orderedList: t("orderedList"),
    codeBlock: t("codeBlock"),
    blockQuote: t("blockQuote"),
    horizontalRule: t("horizontalRule"),
  };
}

export async function getTextAlignmentGroupTexts(): Promise<TextAlignmentGroupTexts> {
  const t = await getTranslations("components.editor.TextAlignmentGroup");
  return {
    alignCenter: t("alignCenter"),
    alignJustify: t("alignJustify"),
    alignLeft: t("alignLeft"),
    alignRight: t("alignRight"),
  };
}

export async function getUndoRedoGroupTexts(): Promise<UndoRedoGroupTexts> {
  const t = await getTranslations("components.editor.UndoRedoGroup");
  return {
    undo: t("undo"),
    redo: t("redo"),
  };
}

export async function getTextAlignmentsTexts(): Promise<TextAlignmentsTexts> {
  const [textStyleGroupTexts, textAlignmentGroupTexts, undoRedoGroupTexts] =
    await Promise.all([
      getTextStyleGroupTexts(),
      getTextAlignmentGroupTexts(),
      getUndoRedoGroupTexts(),
    ]);
  return {
    textStyleGroupTexts,
    textAlignmentGroupTexts,
    undoRedoGroupTexts,
  };
}

export async function getEditorTexts(): Promise<EditorTexts> {
  const [editorToolbarTexts, textAlignmentsTexts] = await Promise.all([
    getEditorToolbarTexts(),
    getTextAlignmentsTexts(),
  ]);
  return {
    editorToolbarTexts,
    textAlignmentsTexts,
  };
}
