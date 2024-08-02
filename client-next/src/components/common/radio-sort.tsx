"use client";

import { SortingOption } from "@/hoooks/useList";
import { Fragment, SetStateAction, useCallback } from "react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { ArrowDown, ArrowUp } from "lucide-react";

export interface RadioSortTexts {
  noSort: string;
}

export interface RadioSortProps extends RadioSortTexts {
  sortingOptions: SortingOption[];
  sort: SortingOption[];
  sortValue: string;
  setSort: (value: SetStateAction<SortingOption[]>) => void;
  setSortValue: (value: SetStateAction<string>) => void;
  callback?: () => void;
}

export default function RadioSort({
  setSort,
  sortValue,
  sort,
  sortingOptions,
  setSortValue,
  noSort,
  callback,
}: RadioSortProps) {
  const handleValueChange = useCallback(
    (val: string) => {
      if (val === sortValue) {
        setSort([]);
        setSortValue("");
        callback?.();
        return;
      }
      // val = property-direction
      const [property, direction] = val.split("-");
      const sortOption = sortingOptions.find(
        (option) =>
          option.property === property && option.direction === direction,
      );
      if (sortOption) {
        setSort([sortOption]);
        setSortValue(val);
        callback?.();
      }
    },
    [callback, setSort, setSortValue, sortValue, sortingOptions],
  );

  if (sortingOptions?.length === 0) return null;
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline">{`${sort[0]?.text || noSort} `}</Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56 overflow-y-auto max-h-[250px]">
        <DropdownMenuRadioGroup
          value={sortValue}
          onValueChange={handleValueChange}
        >
          {sortingOptions.map(({ property, direction, text }, i) => (
            <Fragment key={`${property}-${direction}`}>
              <DropdownMenuRadioItem
                value={`${property}-${direction}`}
                icon={direction === "asc" ? <ArrowUp /> : <ArrowDown />}
                iconClassnames={"w-4 h-4 fill-current"}
              >
                {text}
              </DropdownMenuRadioItem>
              {i !== sortingOptions.length - 1 && <DropdownMenuSeparator />}
            </Fragment>
          ))}
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
