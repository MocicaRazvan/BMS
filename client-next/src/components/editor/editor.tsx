import { EditorContent, useEditor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import EditorToolbar, { EditorToolbarTexts } from "./toolbar";
import { cn } from "@/lib/utils";
import DOMPurify from "dompurify";
import { ClassValue } from "clsx";
import { CustomSpan } from "@/components/editor/custom-span";

export interface EditorTexts {
  editorToolbarTexts: EditorToolbarTexts;
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
}: Props) {
  const editor = useEditor({
    extensions: [StarterKit, CustomSpan],
    content: descritpion,
    editorProps: {
      attributes: {
        class: cn(
          "prose max-w-none [&_ol]:list-decimal [&_ul]:list-disc dark:prose-invert",
          "rounded-md border min-h-[140px] border-input bg-background ring-offset-2 px-2 py-1",
        ),
      },
    },
    onUpdate({ editor }) {
      onChange(DOMPurify.sanitize(editor.getHTML()));
    },
  });
  if (!editor) return null;
  return (
    <div className="flex flex-col justify-center min-h-[200px] ">
      <EditorToolbar
        editor={editor}
        sticky={sticky}
        useEmojis={useEmojis}
        {...texts.editorToolbarTexts}
      />
      <div className={cn("h-1", separatorClassname)} />
      <div className={cn(editorContentWrapperClassname)}>
        <EditorContent editor={editor} placeholder={placeholder} />
      </div>
    </div>
  );
}
