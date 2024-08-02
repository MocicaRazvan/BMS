"use client";
import React from "react";
import { Editor } from "@tiptap/react";
import {
  Bold,
  Code,
  Italic,
  List,
  ListOrdered,
  Minus,
  Quote,
  Redo,
  Strikethrough,
  Undo,
} from "lucide-react";

import { ToggleGroup } from "../ui/toggle-group";
import { Toggle } from "./toggle";
import { Toolbar } from "./base-toolbar";
import { FormatType } from "./format-type";

interface EditorToolbarProps {
  editor: Editor;
  sticky?: boolean;
}

const EditorToolbar = ({ editor, sticky }: EditorToolbarProps) => {
  return (
    <Toolbar
      className="m-0 z-10 flex items-center md:justify-between px-0 md:px-2 py-2 md:flex-row flex-col justify-center "
      aria-label="Formatting options"
      sticky={sticky}
    >
      <ToggleGroup className="flex flex-row items-center" type="multiple">
        <Toggle
          size="icon"
          className="mr-1"
          tooltip="Bold"
          onPressedChange={() => editor.chain().focus().toggleBold().run()}
          disabled={!editor.can().chain().focus().toggleBold().run()}
          pressed={editor.isActive("bold")}
        >
          <Bold className="h-4 w-4" />
        </Toggle>

        <Toggle
          size="icon"
          className="mr-1"
          tooltip="Italic"
          onPressedChange={() => editor.chain().focus().toggleItalic().run()}
          disabled={!editor.can().chain().focus().toggleItalic().run()}
          pressed={editor.isActive("italic")}
          value="italic"
        >
          <Italic className="h-4 w-4" />
        </Toggle>

        <Toggle
          size="icon"
          className="mr-1"
          tooltip="Strike through"
          onPressedChange={() => editor.chain().focus().toggleStrike().run()}
          disabled={!editor.can().chain().focus().toggleStrike().run()}
          pressed={editor.isActive("strike")}
        >
          <Strikethrough className="h-4 w-4" />
        </Toggle>

        <Toggle
          size="icon"
          className="mr-1"
          tooltip="Bullet list"
          onPressedChange={() =>
            editor.chain().focus().toggleBulletList().run()
          }
          pressed={editor.isActive("bulletList")}
        >
          <List className="h-4 w-4" />
        </Toggle>

        <Toggle
          size="icon"
          className="mr-1"
          tooltip="Numbered list"
          onPressedChange={() =>
            editor.chain().focus().toggleOrderedList().run()
          }
          pressed={editor.isActive("orderedList")}
        >
          <ListOrdered className="h-4 w-4" />
        </Toggle>

        <Toggle
          size="icon"
          className="mr-1"
          tooltip="Code block"
          onPressedChange={() => editor.chain().focus().toggleCodeBlock().run()}
          pressed={editor.isActive("codeBlock")}
        >
          <Code className="h-4 w-4" />
        </Toggle>

        <Toggle
          size="icon"
          className="mr-1"
          tooltip="Block quote"
          onPressedChange={() =>
            editor.chain().focus().toggleBlockquote().run()
          }
          pressed={editor.isActive("blockquote")}
        >
          <Quote className="h-4 w-4" />
        </Toggle>

        <Toggle
          size="icon"
          className="mr-1"
          tooltip="Horizontal rule"
          onPressedChange={() =>
            editor.chain().focus().setHorizontalRule().run()
          }
        >
          <Minus className="h-4 w-4" />
        </Toggle>

        <div className="hidden md:block">
          <FormatType editor={editor} />
        </div>
      </ToggleGroup>

      <ToggleGroup className="flex flex-row items-center" type="multiple">
        <div className="block md:hidden">
          <FormatType editor={editor} />
        </div>
        <Toggle
          size="icon"
          className="mr-1"
          onPressedChange={() => editor.chain().focus().undo().run()}
          disabled={!editor.can().chain().focus().undo().run()}
        >
          <Undo className="h-4 w-4" />
        </Toggle>

        <Toggle
          size="icon"
          className="mr-1"
          onPressedChange={() => editor.chain().focus().redo().run()}
          disabled={!editor.can().chain().focus().redo().run()}
        >
          <Redo className="h-4 w-4" />
        </Toggle>
      </ToggleGroup>
    </Toolbar>
  );
};

export default EditorToolbar;
