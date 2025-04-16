"use client";

import * as TooltipPrimitive from "@radix-ui/react-tooltip";
import { ComponentPropsWithoutRef, PropsWithChildren, useState } from "react";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Check, Clipboard } from "lucide-react";

interface Props
  extends Omit<
    ComponentPropsWithoutRef<typeof TooltipPrimitive.Content>,
    "children"
  > {
  text: string;
  triggerClassName?: string;
}

export default function OverflowTextTooltip({
  text,
  triggerClassName,
  ...props
}: Props) {
  const [isCopied, setIsCopied] = useState(false);
  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <p
            className={cn(
              "max-w-16 text-nowrap overflow-x-hidden text-ellipsis",
              triggerClassName,
            )}
          >
            {text}
          </p>
        </TooltipTrigger>
        <TooltipContent {...props}>
          <div className="flex items-center justify-around gap-2">
            <span className="max-w-56 text-wrap">{text}</span>
            <Button
              variant="outline"
              size="icon"
              className="h-7 w-7"
              onClick={() => {
                navigator.clipboard.writeText(text).then(() => {
                  setIsCopied(true);
                  setTimeout(() => setIsCopied(false), 1000);
                });
              }}
            >
              {isCopied ? (
                <Check size="16" className="text-success" />
              ) : (
                <Clipboard size="16" />
              )}
            </Button>
          </div>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}
interface LengthProps
  extends ComponentPropsWithoutRef<typeof TooltipPrimitive.Content> {
  text: string;
  maxLength: number;
}

export function OverflowLengthTextTooltip({
  text,
  maxLength,
  children,
  ...props
}: LengthProps) {
  if (text.length <= maxLength) {
    return children;
  }
  return (
    <TooltipProvider>
      <Tooltip delayDuration={1000}>
        <TooltipTrigger asChild>{children}</TooltipTrigger>
        <TooltipContent {...props} className="max-w-xs z-10">
          <p>{text}</p>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}
