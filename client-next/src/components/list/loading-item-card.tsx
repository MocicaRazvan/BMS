import { cn } from "@/lib/utils";
import { Skeleton } from "@/components/ui/skeleton";

export default function LoadingItemCard() {
  return (
    <div
      className={cn(
        "flex flex-col items-center gap-2 border rounded-xl p-4 w-full  shadow-foreground h-[565px] overflow-hidden",
      )}
    >
      <Skeleton className="w-full h-[230px] rounded-lg" />
      <Skeleton className="w-full h-[60px] mt-1 rounded-lg" />
      <Skeleton className="w-full h-[190px] mt-1 rounded-lg" />
      <hr className="border my-1" />
      <Skeleton className="w-full flex-1 rounded-lg" />
    </div>
  );
}
