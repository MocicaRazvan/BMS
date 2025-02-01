import { cn } from "@/lib/utils";
import React from "react";
import { PlanResponse } from "@/types/dto";

const colorMap = {
  VEGAN: "success",
  OMNIVORE: "secondary",
  VEGETARIAN: "accent",
};
export default function PlanType({ type }: { type: PlanResponse["type"] }) {
  return (
    <p
      className={cn(
        `px-3 py-1 bg-${colorMap[type]} text-${colorMap[type]}-foreground rounded-full font-bold text-center`,
      )}
    >
      {type}
    </p>
  );
}
