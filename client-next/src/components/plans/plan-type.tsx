import { cn } from "@/lib/utils";
import React from "react";
import { PlanResponse } from "@/types/dto";
import { ClassValue } from "clsx";

const colorMap = {
  VEGAN: "success",
  OMNIVORE: "secondary",
  VEGETARIAN: "accent",
};
export default function PlanType({
  type,
  className,
}: {
  type: PlanResponse["type"];
  className?: ClassValue;
}) {
  return (
    <p
      className={cn(
        `px-3 py-1 bg-${colorMap[type]} text-${colorMap[type]}-foreground rounded-full font-bold text-center`,
        className,
      )}
    >
      {type}
    </p>
  );
}
