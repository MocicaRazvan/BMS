import { EditorContent, useEditor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import EditorToolbar from "./toolbar";
import { cn } from "@/lib/utils";
import DOMPurify from "dompurify";

interface Props {
  descritpion: string;
  onChange: (value: string) => void;
  placeholder?: string;
  sticky?: boolean;
}

export default function Editor({
  descritpion,
  onChange,
  placeholder,
  sticky,
}: Props) {
  const editor = useEditor({
    extensions: [StarterKit],
    content: descritpion,
    editorProps: {
      attributes: {
        class: cn(
          "prose max-w-none [&_ol]:list-decimal [&_ul]:list-disc dark:prose-invert",
          "rounded-md border min-h-[140px] border-input bg-background ring-offset-2 px-2",
        ),
      },
    },
    onUpdate({ editor }) {
      onChange(DOMPurify.sanitize(editor.getHTML()));
    },
  });
  if (!editor) return null;
  return (
    <div className="flex flex-col justify-center min-h-[250px] ">
      <EditorToolbar editor={editor} sticky={sticky} />
      <EditorContent editor={editor} placeholder={placeholder} />
    </div>
  );
}
