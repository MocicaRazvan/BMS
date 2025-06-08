"use client";
import { Editor } from "@tiptap/react";
import { ToggleGroup } from "@/components/ui/toggle-group";
import { Toggle } from "@/components/editor/toggle";
import {
  AlignCenter,
  AlignJustify,
  AlignLeft,
  AlignRight,
  Bold,
  Code,
  Italic,
  List,
  ListOrdered,
  Minus,
  Quote,
  Redo,
  Strikethrough,
  Underline,
  Undo,
} from "lucide-react";
import React from "react";

export interface TextStyleGroupTexts {
  bold: string;
  italic: string;
  underline: string;
  strike: string;
  bulletList: string;
  orderedList: string;
  codeBlock: string;
  blockQuote: string;
  horizontalRule: string;
}

interface WithEditor {
  editor: Editor;
}

export const TextStyleGroup = ({
  editor,
  texts,
}: WithEditor & {
  texts: TextStyleGroupTexts;
}) => (
  <ToggleGroup className="flex flex-row items-center" type="multiple">
    <Toggle
      size="icon"
      className="mr-1"
      tooltip={texts.bold}
      onPressedChange={() => editor.chain().focus().toggleBold().run()}
      disabled={!editor.can().chain().focus().toggleBold().run()}
      pressed={editor.isActive("bold")}
    >
      <Bold className="h-4 w-4" />
    </Toggle>

    <Toggle
      size="icon"
      className="mr-1"
      tooltip={texts.italic}
      onPressedChange={() => editor.chain().focus().toggleItalic().run()}
      disabled={!editor.can().chain().focus().toggleItalic().run()}
      pressed={editor.isActive("italic")}
    >
      <Italic className="h-4 w-4" />
    </Toggle>
    <Toggle
      size="icon"
      className="mr-1"
      tooltip={texts.underline}
      onPressedChange={() => editor.chain().focus().toggleUnderline().run()}
      disabled={!editor.can().chain().focus().toggleUnderline().run()}
      pressed={editor.isActive("underline")}
    >
      <Underline className="h-4 w-4" />
    </Toggle>

    <Toggle
      size="icon"
      className="mr-1"
      tooltip={texts.strike}
      onPressedChange={() => editor.chain().focus().toggleStrike().run()}
      disabled={!editor.can().chain().focus().toggleStrike().run()}
      pressed={editor.isActive("strike")}
    >
      <Strikethrough className="h-4 w-4" />
    </Toggle>

    <Toggle
      size="icon"
      className="mr-1"
      tooltip={texts.bulletList}
      onPressedChange={() => editor.chain().focus().toggleBulletList().run()}
      pressed={editor.isActive("bulletList")}
    >
      <List className="h-4 w-4" />
    </Toggle>

    <Toggle
      size="icon"
      className="mr-1"
      tooltip={texts.orderedList}
      onPressedChange={() => editor.chain().focus().toggleOrderedList().run()}
      pressed={editor.isActive("orderedList")}
    >
      <ListOrdered className="h-4 w-4" />
    </Toggle>

    <Toggle
      size="icon"
      className="mr-1"
      tooltip={texts.codeBlock}
      onPressedChange={() => editor.chain().focus().toggleCodeBlock().run()}
      pressed={editor.isActive("codeBlock")}
    >
      <Code className="h-4 w-4" />
    </Toggle>

    <Toggle
      size="icon"
      className="mr-1"
      tooltip={texts.blockQuote}
      onPressedChange={() => editor.chain().focus().toggleBlockquote().run()}
      pressed={editor.isActive("blockquote")}
    >
      <Quote className="h-4 w-4" />
    </Toggle>

    <Toggle
      size="icon"
      className="mr-1"
      tooltip={texts.horizontalRule}
      onPressedChange={() => editor.chain().focus().setHorizontalRule().run()}
    >
      <Minus className="h-4 w-4" />
    </Toggle>
  </ToggleGroup>
);

export interface TextAlignmentGroupTexts {
  alignLeft: string;
  alignCenter: string;
  alignRight: string;
  alignJustify: string;
}
export const TextAlignmentGroup = ({
  editor,
  texts,
}: WithEditor & {
  texts: TextAlignmentGroupTexts;
}) => (
  <ToggleGroup className="flex flex-row items-center" type="multiple">
    <Toggle
      size="icon"
      onPressedChange={() => editor.chain().focus().setTextAlign("left").run()}
      disabled={!editor.can().chain().focus().setTextAlign("left").run()}
      pressed={editor.isActive({ textAlign: "left" })}
      tooltip={texts.alignLeft}
      className="mr-1"
    >
      <AlignLeft className="h-4 w-4" />
    </Toggle>
    <Toggle
      size="icon"
      onPressedChange={() =>
        editor.chain().focus().setTextAlign("center").run()
      }
      disabled={!editor.can().chain().focus().setTextAlign("center").run()}
      pressed={editor.isActive({ textAlign: "center" })}
      tooltip={texts.alignCenter}
      className="mr-1"
    >
      <AlignCenter className="h-4 w-4" />
    </Toggle>
    <Toggle
      size="icon"
      className="mr-1"
      onPressedChange={() => editor.chain().focus().setTextAlign("right").run()}
      disabled={!editor.can().chain().focus().setTextAlign("right").run()}
      pressed={editor.isActive({ textAlign: "right" })}
      tooltip={texts.alignRight}
    >
      <AlignRight className="h-4 w-4" />
    </Toggle>
    <Toggle
      size="icon"
      className="mr-1"
      onPressedChange={() =>
        editor.chain().focus().setTextAlign("justify").run()
      }
      disabled={!editor.can().chain().focus().setTextAlign("justify").run()}
      pressed={editor.isActive({ textAlign: "justify" })}
      tooltip={texts.alignJustify}
    >
      <AlignJustify className="h-4 w-4" />
    </Toggle>
  </ToggleGroup>
);

export interface UndoRedoGroupTexts {
  undo: string;
  redo: string;
}
export const UndoRedoGroup = ({
  editor,
  texts,
}: WithEditor & {
  texts: UndoRedoGroupTexts;
}) => (
  <ToggleGroup className="flex flex-row items-center" type="multiple">
    <Toggle
      size="icon"
      className="mr-1"
      onPressedChange={() => editor.chain().focus().undo().run()}
      disabled={!editor.can().chain().focus().undo().run()}
      tooltip={texts.undo}
    >
      <Undo className="h-4 w-4" />
    </Toggle>

    <Toggle
      size="icon"
      className="mr-1"
      onPressedChange={() => editor.chain().focus().redo().run()}
      disabled={!editor.can().chain().focus().redo().run()}
      tooltip={texts.redo}
    >
      <Redo className="h-4 w-4" />
    </Toggle>
  </ToggleGroup>
);

export interface TextAlignmentsTexts {
  textStyleGroupTexts: TextStyleGroupTexts;
  textAlignmentGroupTexts: TextAlignmentGroupTexts;
  undoRedoGroupTexts: UndoRedoGroupTexts;
}
