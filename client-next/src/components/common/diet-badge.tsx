import { DietType } from "@/types/dto";
import { cn } from "@/lib/utils";
import React, { HTMLAttributes } from "react";

interface Props extends HTMLAttributes<HTMLDivElement> {
  dietType: DietType;
  pClassName?: string;
}
const colorMap = {
  VEGAN: "success",
  OMNIVORE: "secondary",
  VEGETARIAN: "accent",
};
export default function DietBadge({ dietType, pClassName, ...rest }: Props) {
  return (
    <div {...rest}>
      <p
        className={cn(
          `px-4 py-2 bg-${colorMap[dietType]} text-${colorMap[dietType]}-foreground rounded-full font-bold text-lg`,
          pClassName,
        )}
      >
        {dietType}
      </p>
    </div>
  );
}
