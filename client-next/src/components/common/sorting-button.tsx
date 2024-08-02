"use client";

import { Button } from "@/components/ui/button";
import { SortDirection } from "@/types/fetch-utils";
import { ArrowDown, ArrowUp } from "lucide-react";
import { Dispatch, SetStateAction, useCallback } from "react";
import { SortingOption } from "@/hoooks/useList";

interface Props<T extends string | number | symbol> {
  sort: SortingOption[];
  setSort: Dispatch<SetStateAction<SortingOption[]>>;
  field: T;
}

export default function SortingButton<T extends string | number | symbol>({
  sort,
  setSort,
  field,
}: Props<T>) {
  const handleSortChange = useCallback(() => {
    setSort((prev) => {
      if (sort.length === 0)
        return [
          {
            property: field.toString(),
            direction: "asc",
            text: field.toString() + " asc",
          },
        ];
      else {
        const exists = prev.find((item) => item.property === field.toString());
        if (exists) {
          if (exists.direction === "desc") return [];

          const direction = exists?.direction === "asc" ? "desc" : "asc";
          return [
            {
              ...exists,
              direction,
              text: field.toString() + " " + direction,
            },
          ];
        }
        return prev;
      }
    });
  }, [setSort, sort.length, field]);
  const text =
    field.toString() +
    " " +
    (sort.find((item) => item.property === field.toString())?.direction ||
      "none");
  return (
    <Button onClick={() => handleSortChange()} variant="outline">
      {text}
    </Button>
  );
}
