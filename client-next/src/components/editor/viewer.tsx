import { EditorContent, useEditor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import { cn } from "@/lib/utils";

interface Props {
  descritpion: string;
}
export default function Viewer({ descritpion }: Props) {
  const editor = useEditor({
    extensions: [StarterKit],
    content: descritpion,
    editorProps: {
      attributes: {
        class: cn(
          "prose max-w-none [&_ol]:list-decimal [&_ul]:list-disc dark:prose-invert",
          "rounded-md border min-h-[140px] border-input bg-background ring-offset-2",
        ),
      },
    },
  });
  if (!editor) return null;
  return (
    <div className="">
      <EditorContent editor={editor} readOnly={true} />
    </div>
  );
}
