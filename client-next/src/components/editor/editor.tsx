import { EditorContent, useEditor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import Typography from "@tiptap/extension-typography";
import TextAlign from "@tiptap/extension-text-align";
import Underline from "@tiptap/extension-underline";
import EditorToolbar, { EditorToolbarTexts } from "./toolbar";
import { cn } from "@/lib/utils";
import DOMPurify from "dompurify";
import { ClassValue } from "clsx";
import { CustomSpan } from "@/components/editor/custom-span";
import { EditorView } from "prosemirror-view";
import { TipTapBubbleMenu } from "@/components/editor/bubble-menu";
import { TextAlignmentsTexts } from "@/components/editor/editor-allignements";

export interface EditorTexts {
  editorToolbarTexts: EditorToolbarTexts;
  textAlignmentsTexts: TextAlignmentsTexts;
}
interface Props {
  descritpion: string;
  onChange: (value: string) => void;
  placeholder?: string;
  sticky?: boolean;
  texts: EditorTexts;
  editorContentWrapperClassname?: ClassValue;
  useEmojis?: boolean;
  separatorClassname?: ClassValue;
  editorClassname?: ClassValue;
}

function handleFiles(dt: DataTransfer, view: EditorView) {
  Array.from(dt.files).forEach((file) => {
    if (file.type !== "text/plain" && file.type !== "text/html") return;
    const reader = new FileReader();
    reader.onload = () => {
      const raw = reader.result as string;
      const sanitized = DOMPurify.sanitize(raw);
      view.dispatch(
        view.state.tr.replaceSelectionWith(view.state.schema.text(sanitized)),
      );
    };
    reader.readAsText(file);
  });
}

export default function Editor({
  descritpion,
  onChange,
  placeholder,
  sticky,
  texts,
  editorContentWrapperClassname,
  useEmojis = true,
  separatorClassname = "",
  editorClassname = "",
}: Props) {
  const editor = useEditor({
    extensions: [
      StarterKit,
      CustomSpan,
      Typography,
      Underline,
      TextAlign.configure({
        types: ["heading", "paragraph"],
        defaultAlignment: "left",
      }),
    ],
    content: descritpion,
    editorProps: {
      attributes: {
        class: cn(
          "prose max-w-none [&_ol]:list-decimal [&_ul]:list-disc dark:prose-invert",
          "rounded-md border min-h-[140px] border-input bg-background ring-offset-2 px-2 py-1",
        ),
      },
      handleDrop: (view, event, slice, moved) => {
        const dt = event.dataTransfer;
        if (!dt || !dt.files || dt.files.length === 0) {
          return false;
        }
        event.preventDefault();
        handleFiles(dt, view);
        return true;
      },
      handlePaste: (view, event) => {
        const dt = event.clipboardData;
        if (!dt || !dt.files || dt.files.length === 0) {
          return false;
        }
        event.preventDefault();
        handleFiles(dt, view);
        return true;
      },
    },
    onUpdate({ editor }) {
      onChange(
        DOMPurify.sanitize(editor.getHTML(), {
          FORBID_TAGS: ["img", "video", "audio", "iframe", "script", "style"],
        }),
      );
    },
  });
  if (!editor) return null;
  return (
    <div className="flex flex-col justify-center min-h-[200px]">
      <TipTapBubbleMenu
        editor={editor}
        textAlignmentGroupTexts={
          texts.textAlignmentsTexts.textAlignmentGroupTexts
        }
        textStyleGroupTexts={texts.textAlignmentsTexts.textStyleGroupTexts}
      />
      <EditorToolbar
        editor={editor}
        sticky={sticky}
        useEmojis={useEmojis}
        textAlignmentsTexts={texts.textAlignmentsTexts}
        {...texts.editorToolbarTexts}
      />
      <div className={cn("h-1", separatorClassname)} />
      <div className={cn(editorContentWrapperClassname)}>
        <EditorContent
          editor={editor}
          placeholder={placeholder}
          className={cn(editorClassname)}
        />
      </div>
    </div>
  );
}
