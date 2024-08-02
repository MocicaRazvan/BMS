interface Props {
  title: string;
  header: string;
  className?: string;
}

export default function Heading({ title, header, className }: Props) {
  return (
    <div className={className}>
      <h1 className="text-2xl lg:text-3xl font-bold tracking-tight">{title}</h1>
      <p className="text-gray-500 dark:text-gray-400">{header}</p>
    </div>
  );
}
