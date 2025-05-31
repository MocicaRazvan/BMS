"use client";

import { ComponentPropsWithoutRef, CSSProperties, useState } from "react";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
  TooltipPortal,
} from "@/components/ui/tooltip";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Check, Clipboard } from "lucide-react";
import * as TooltipPrimitive from "@radix-ui/react-tooltip";

interface Props
  extends Omit<
    ComponentPropsWithoutRef<typeof TooltipPrimitive.Content>,
    "children"
  > {
  text: string;
  triggerClassName?: string;
  triggerStyle?: CSSProperties;
}

export default function OverflowTextTooltip({
  text,
  triggerClassName,
  triggerStyle,
  ...props
}: Props) {
  const [isCopied, setIsCopied] = useState(false);
  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <p
            className={cn(
              "max-w-16 text-nowrap overflow-x-hidden text-ellipsis overflow-y-hidden",
              triggerClassName,
            )}
            style={{ ...triggerStyle }}
          >
            {text}
          </p>
        </TooltipTrigger>
        <TooltipPortal>
          <TooltipContent
            {...props}
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
            }}
            onMouseDown={(e) => {
              e.stopPropagation();
            }}
          >
            <div className="flex items-start justify-around gap-2">
              <span
                className="max-w-56 text-wrap select-text"
                style={{ contain: "layout paint" }}
              >
                {text}
              </span>
              <Button
                variant="outline"
                size="icon"
                className="h-7 w-7"
                onClick={(e) => {
                  e.preventDefault();
                  e.stopPropagation();
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
        </TooltipPortal>
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
