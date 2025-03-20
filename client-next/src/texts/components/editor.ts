"use server";
import { FormatTypeTexts } from "@/components/editor/format-type";
import { getTranslations } from "next-intl/server";
import { EditorEmojiPickerTexts } from "@/components/editor/editor-emoji-picker";
import { EditorToolbarTexts } from "@/components/editor/toolbar";
import { EditorTexts } from "@/components/editor/editor";

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

export async function getEditorTexts(): Promise<EditorTexts> {
  const [editorToolbarTexts] = await Promise.all([getEditorToolbarTexts()]);
  return {
    editorToolbarTexts,
  };
}
