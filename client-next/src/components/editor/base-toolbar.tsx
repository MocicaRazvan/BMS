import React from "react";
import {
  ToggleGroup as ToggleGroupPrimitive,
  Toolbar as ToolbarPrimitive,
} from "@radix-ui/react-toolbar";

import { cn } from "@/lib/utils";

interface ToolbarProps {
  children: React.ReactNode;
  className?: string;
  sticky?: boolean;
}

const Toolbar = ({ children, className, sticky = true }: ToolbarProps) => {
  return (
    <ToolbarPrimitive
      className={cn(
        "inset-x-0 top-14 z-50 my-2 rounded-sm bg-secondary/40 px-4 py-2 backdrop-blur-lg",
        className,
        sticky && "sticky"
      )}
    >
      {children}
    </ToolbarPrimitive>
  );
};

const ToggleGroup = ToggleGroupPrimitive;

ToggleGroup.displayName = ToggleGroupPrimitive.displayName;

export { Toolbar, ToggleGroup };
