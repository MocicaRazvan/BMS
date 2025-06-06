import { Skeleton } from "@/components/ui/skeleton";
import { HTMLAttributes } from "react";
import { cn } from "@/lib/utils";

const ROWS = 11 as const;

export default function DataTableDynamicSkeleton({
  className,
  ...props
}: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn(
        "w-full h-[82.5vh] rounded-md p-0 flex flex-col gap-3.5",
        className,
      )}
      {...props}
    >
      <div className="w-full h-12 flex items-center justify-between py-2 gap-2.5">
        <Skeleton className="w-1/6 h-full" />
        <Skeleton className="w-1/6 h-full" />
      </div>

      <div className="flex-1 w-full rounded-md border flex flex-col items-center justify-center">
        {Array.from({ length: ROWS }).map((_, index) => (
          <div
            key={index + "DataTableDynamicSkeleton"}
            className="p-2.5 flex items-center justify-center border-b last:border-b-0 w-full  flex-1"
          >
            <Skeleton className="size-full" />
          </div>
        ))}
      </div>
      <div className="w-full h-12 flex items-center justify-between py-2 gap-2.5">
        <Skeleton className="w-1/6 h-full" />
        <Skeleton className="w-1/3 h-full" />
      </div>
    </div>
  );
}
