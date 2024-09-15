import { DayType } from "@/types/dto";
import { Badge, BadgeProps } from "@/components/ui/badge";
import React from "react";
import { cn } from "@/lib/utils";

export interface DayTypeBadgeTexts {
  labels: Record<DayType, string>;
}
interface Props extends DayTypeBadgeTexts, BadgeProps {
  type: DayType;
}
export default function DayTypeBadge({
  type,
  labels,
  className,
  ...props
}: Props) {
  return (
    <Badge {...props} className={cn("text-lg", className)}>
      {labels[type]}
    </Badge>
  );
}
