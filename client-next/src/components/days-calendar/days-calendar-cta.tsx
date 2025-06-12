import { Notebook } from "lucide-react";
import { Link } from "@/navigation/navigation";
import { cn } from "@/lib/utils";
import { MouseEventHandler } from "react";

export interface DaysCalendarCTATexts {
  header: string;
  className?: string;
  size?: number;
}
export default function DaysCalendarCTA({
  header,
  className,
  size = 24,
  onClick,
}: DaysCalendarCTATexts & {
  onClick?: MouseEventHandler<HTMLAnchorElement>;
}) {
  return (
    <Link
      onClick={onClick}
      href="/daysCalendar"
      className={cn(
        "flex items-center justify-start gap-2 mt-5 font-semibold text-muted-foreground text-xl hover:underline",
        className,
      )}
    >
      {header}
      <Notebook size={size} />
    </Link>
  );
}
