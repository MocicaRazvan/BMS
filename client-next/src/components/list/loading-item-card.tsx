import { cn } from "@/lib/utils";
import { Skeleton } from "@/components/ui/skeleton";
import { HTMLAttributes } from "react";

interface Props extends HTMLAttributes<HTMLDivElement> {}

export default function LoadingItemCard({ className, ...rest }: Props) {
  return (
    <div
      className={cn(
        "flex items-center justify-center gap-2 border rounded-xl p-4 w-full  hover:shadow-lg transition-all duration-300 shadow-foreground ",
        className,
      )}
      {...rest}
    >
      <Skeleton className="w-full h-full flex-1" />
    </div>
  );
}
