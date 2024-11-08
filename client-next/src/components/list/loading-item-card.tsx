import { cn } from "@/lib/utils";
import { Skeleton } from "@/components/ui/skeleton";

export default function LoadingItemCard() {
  return (
    <div
      className={cn(
        "flex items-center justify-center gap-2 border rounded-xl p-4 w-full  hover:shadow-lg transition-all duration-300 shadow-foreground h-[565px] ",
      )}
    >
      <Skeleton className="w-full h-full flex-1" />
    </div>
  );
}
