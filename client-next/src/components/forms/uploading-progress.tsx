"use client";
import { Progress } from "@/components/ui/progress";
import * as ProgressPrimitive from "@radix-ui/react-progress";
import { ComponentPropsWithoutRef } from "react";
import { cn } from "@/lib/utils";

export interface UploadingProgressTexts {
  loadedItems: string;
}

interface Props
  extends UploadingProgressTexts,
    ComponentPropsWithoutRef<typeof ProgressPrimitive.Root> {
  total: number;
  loaded: number;
  indicatorClassName?: string;
}
export default function UploadingProgress({
  total,
  loaded,
  loadedItems,
  ...rest
}: Props) {
  return (
    <div className="space-y-4">
      <Progress
        value={Math.round((loaded / total) * 100)}
        {...rest}
        indicatorClassName={cn(loaded === total && "bg-success")}
      />
      <div className="flex items-center justify-center w-full">
        <p
          className={cn(
            "text-lg font-semibold",
            loaded === total && "text-success",
          )}
        >
          {`${loadedItems} ${loaded}/${total}`}
        </p>
      </div>
    </div>
  );
}
