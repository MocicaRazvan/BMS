"use client";

import { SortingOption } from "@/hoooks/useList";
import {
  Fragment,
  SetStateAction,
  useCallback,
  useEffect,
  useRef,
} from "react";
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
import { useSearchParams } from "next/navigation";
import { parseSortString } from "@/lib/utils";

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
  useDefaultSort?: boolean;
}

export default function RadioSort({
  setSort,
  sortValue,
  sort,
  sortingOptions,
  setSortValue,
  noSort,
  callback,
  useDefaultSort = true,
}: RadioSortProps) {
  const useDefaultSortRef = useRef(useDefaultSort);
  const currentSearchParams = useSearchParams();

  useEffect(() => {
    if (useDefaultSortRef.current && sortingOptions.length > 0) {
      useDefaultSortRef.current = false;
      const sortString = currentSearchParams.get("sort");
      const sortQ = parseSortString(sortString, sortingOptions);

      if (!sortQ || !sortQ.length) {
        const defaultOption = sortingOptions.find(
          (o) => o.property === "createdAt" && o.direction === "desc",
        );
        console.log("DEFAULT OPTION ", defaultOption);
        if (defaultOption) {
          setSort([defaultOption]);
          setSortValue("createdAt-desc");
          callback?.();
        }
      }
    }
  }, [sortingOptions.length]);

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
