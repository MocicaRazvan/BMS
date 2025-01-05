import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

export default function LoadingDialogAnchor({
  className = "",
}: {
  className?: string;
}) {
  return (
    <div className="h-12 w-full p-2 flex items-center justify-start">
      <Skeleton className={cn("w-2/3 h-full", className)} />
    </div>
  );
}
