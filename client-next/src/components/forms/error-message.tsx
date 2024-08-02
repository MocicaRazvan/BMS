import { cn } from "@/lib/utils";

interface Props {
  message: string;
  show: boolean;
}

export default function ErrorMessage({ message, show }: Props) {
  return (
    <p
      className={cn(
        "font-medium text-destructive text-lg my-1",
        show ? "block" : "hidden",
      )}
    >
      {message}
    </p>
  );
}
