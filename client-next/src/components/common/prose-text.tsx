interface Props {
  html: string | undefined;
}

export default function ProseText({ html }: Props) {
  return (
    <div
      className="prose max-w-none [&_ol]:list-decimal [&_ul]:list-disc dark:prose-invert text-wrap"
      dangerouslySetInnerHTML={{ __html: html ?? "" }}
    />
  );
}
