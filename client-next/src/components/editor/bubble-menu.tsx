"use client";
import { BubbleMenu, type Editor } from "@tiptap/react";
import {
  TextAlignmentGroup,
  TextAlignmentGroupTexts,
  TextStyleGroup,
  TextStyleGroupTexts,
} from "@/components/editor/editor-allignements";
import { v4 as uuidv4 } from "uuid";
import { useMemo, useRef } from "react";

export function TipTapBubbleMenu({
  editor,
  textAlignmentGroupTexts,
  textStyleGroupTexts,
}: {
  editor: Editor;
  textStyleGroupTexts: TextStyleGroupTexts;
  textAlignmentGroupTexts: TextAlignmentGroupTexts;
}) {
  const uniqueId = useMemo(() => `bubble-menu-${uuidv4()}`, []);
  const timeoutRef = useRef<NodeJS.Timeout>();
  return (
    <BubbleMenu
      editor={editor}
      tippyOptions={{
        duration: 200,
        interactive: false,
        // animateFill: true,
        // animation: "fade",
        // delay: [1000, 0],
        // interactive: true,
        onMount: (instance) => {
          instance.popper.style.opacity = "0";
          instance.popper.style.pointerEvents = "none";
        },
        onShow: (instance) => {
          timeoutRef.current = setTimeout(() => {
            instance.popper.style.opacity = "1";
            instance.popper.style.transition = "opacity 200ms ease-in-out";
            instance.popper.style.pointerEvents = "auto";
            instance.setProps({ interactive: true });
          }, 1000);
        },
        onDestroy() {
          if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
          }
        },
      }}
      pluginKey={uniqueId}
      className="bg-background space-y-0.5 rounded p-1"
    >
      <TextStyleGroup editor={editor} texts={textStyleGroupTexts} />
      <TextAlignmentGroup editor={editor} texts={textAlignmentGroupTexts} />
    </BubbleMenu>
  );
}
