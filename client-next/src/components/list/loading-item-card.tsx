import { cn } from "@/lib/utils";
import { Skeleton } from "@/components/ui/skeleton";

export default function LoadingItemCard() {
  return (
    <div
      className={cn(
        "flex flex-col justify-between items-center gap-2 border rounded-xl p-4 w-full  shadow-foreground h-[565px]",
      )}
    >
      <Skeleton className="w-full h-[250px] rounded-lg" />
      <Skeleton className="w-full h-[80px] mt-1.5 rounded-lg" />
      <Skeleton className="w-full h-[200px] mt-1.5 rounded-lg" />
      <hr className="border my-1" />
      <Skeleton className="w-full flex-grow rounded-lg" />
    </div>
  );
}
