"use client";
import React from "react";
import { Editor } from "@tiptap/react";
import { Toolbar } from "./base-toolbar";
import { FormatType, FormatTypeTexts } from "./format-type";
import EditorEmojiPicker, {
  EditorEmojiPickerTexts,
} from "@/components/editor/editor-emoji-picker";
import {
  TextAlignmentGroup,
  TextAlignmentsTexts,
  TextStyleGroup,
  UndoRedoGroup,
} from "@/components/editor/editor-allignements";

export interface EditorToolbarTexts {
  formatTypeTexts: FormatTypeTexts;
  editorEmojiPickerTexts: EditorEmojiPickerTexts;
}

interface EditorToolbarProps extends EditorToolbarTexts {
  editor: Editor;
  sticky?: boolean;
  useEmojis?: boolean;
  textAlignmentsTexts: TextAlignmentsTexts;
}

const EditorToolbar = ({
  editor,
  sticky,
  editorEmojiPickerTexts,
  formatTypeTexts,
  useEmojis = true,
  textAlignmentsTexts,
}: EditorToolbarProps) => {
  return (
    <Toolbar
      className="m-0 z-10 flex items-center md:justify-between px-2 py-2 md:flex-row flex-col justify-center gap-1.5 md:gap-0"
      aria-label="Formatting options"
      sticky={sticky}
    >
      <div className="flex items-center justify-start flex-wrap">
        <TextStyleGroup
          editor={editor}
          texts={textAlignmentsTexts.textStyleGroupTexts}
        />
        <div className="flex flex-row items-center mr-2.5">
          {useEmojis && (
            <EditorEmojiPicker
              onEmojiSelect={(e) => {
                editor
                  .chain()
                  .focus()
                  .insertContent([
                    {
                      type: "customSpan",
                      attrs: {
                        style:
                          "font-size: 1.5rem; min-width: 1em; display: inline-block;",
                      },
                      content: [{ type: "text", text: `${e}` }],
                    },
                    { type: "text", text: " " },
                  ])
                  .run();
              }}
              texts={editorEmojiPickerTexts}
            />
          )}
          <div className="hidden md:block">
            <FormatType editor={editor} texts={formatTypeTexts} />
          </div>
        </div>
        <TextAlignmentGroup
          editor={editor}
          texts={textAlignmentsTexts.textAlignmentGroupTexts}
        />
      </div>
      <div className="flex md:block items-center justify-between w-full md:w-fit">
        <div className="block md:hidden">
          <FormatType editor={editor} texts={formatTypeTexts} />
        </div>
        <UndoRedoGroup
          editor={editor}
          texts={textAlignmentsTexts.undoRedoGroupTexts}
        />
      </div>
    </Toolbar>
  );
};

export default EditorToolbar;
