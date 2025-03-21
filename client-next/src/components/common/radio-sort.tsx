"use client";

import { SortingOption } from "@/hoooks/useList";
import {
  Fragment,
  ReactNode,
  SetStateAction,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button, ButtonProps } from "@/components/ui/button";
import { ArrowDown, ArrowUp } from "lucide-react";
import { useSearchParams } from "next/navigation";
import { parseSortString } from "@/lib/utils";

export interface RadioSortTexts {
  noSort: string;
}

export interface UseRadioSortArgs {
  sortingOptions: SortingOption[];
  sortValue: string;
  setSort: (value: SetStateAction<SortingOption[]>) => void;
  setSortValue: (value: SetStateAction<string>) => void;
  callback?: () => void;
  useDefaultSort?: boolean;
  filterKey: string;
}

export interface RadioSortProps extends RadioSortTexts, UseRadioSortArgs {
  sort: SortingOption[];
}

export const useRadioSort = ({
  useDefaultSort,
  setSortValue,
  sortValue,
  sortingOptions,
  setSort,
  filterKey,
  callback,
}: UseRadioSortArgs) => {
  const useDefaultSortRef = useRef(useDefaultSort);
  const currentSearchParams = useSearchParams();
  const [isDefaultSort, setIsDefaultSort] = useState(false);

  useEffect(() => {
    if (useDefaultSortRef.current && sortingOptions.length > 0) {
      useDefaultSortRef.current = false;
      const sortString = currentSearchParams.get("sort");
      const sortQ = parseSortString(sortString, sortingOptions);
      const curFilter = currentSearchParams.get(filterKey);

      if (curFilter === "" && (!sortQ || !sortQ.length)) {
        const defaultOption = sortingOptions.find(
          (o) => o.property === "createdAt" && o.direction === "desc",
        );
        console.log("DEFAULT OPTION ", defaultOption);
        if (defaultOption) {
          setSort([defaultOption]);
          setSortValue("createdAt-desc");
          setIsDefaultSort(true);
          callback?.();
        }
      }
    }
  }, [sortingOptions.length]);

  useEffect(() => {
    const sortString = currentSearchParams.get("sort");
    const sortQ = parseSortString(sortString, sortingOptions);
    const sortIsDefault =
      sortQ.length === 1 &&
      sortQ[0].property === "createdAt" &&
      sortQ[0].direction === "desc";
    if ((sortValue === "" || sortValue === "createdAt-desc") && sortIsDefault) {
      setIsDefaultSort(true);
      setSortValue("createdAt-desc");
    }
  }, [currentSearchParams, setSortValue, sortValue, sortingOptions.length]);

  const handleValueChange = useCallback(
    (val: string | undefined) => {
      if (isDefaultSort) {
        setIsDefaultSort(false);
      }
      if (val === sortValue || val === undefined) {
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
    [
      callback,
      isDefaultSort,
      setSort,
      setSortValue,
      sortValue,
      JSON.stringify(sortingOptions),
    ],
  );

  return {
    handleValueChange,
    isDefaultSort,
    sortValue,
  };
};

interface RadioSortButtonProps extends ButtonProps {
  sortingProperty: string;
  radioArgs: UseRadioSortArgs;
  children: ReactNode;
}
export const RadioSortButton = ({
  sortingProperty,
  radioArgs,
  children,
  ...args
}: RadioSortButtonProps) => {
  const curSO = radioArgs.sortingOptions.filter(
    (s) => s.property.toLowerCase() === sortingProperty.toLowerCase(),
  );
  const { handleValueChange, isDefaultSort, sortValue } =
    useRadioSort(radioArgs);
  const canBeDefault = useMemo(
    () =>
      curSO.some((s) => s.property === "createdAt" && s.direction === "desc"),
    [curSO],
  );
  const isDefaultActive = isDefaultSort && canBeDefault;
  const [toggleSort, setToggleSort] = useState<SortingOption["direction"]>(
    isDefaultSort ? "desc" : "none",
  );

  useEffect(() => {
    if (isDefaultSort) {
      setToggleSort("desc");
    }
  }, [isDefaultSort]);

  if (curSO.length !== 2) return null;
  const handleButtonClick = () => {
    if (toggleSort === "asc") {
      handleValueChange(`${sortingProperty}-desc`);
      setToggleSort("desc");
    } else if (toggleSort === "desc") {
      handleValueChange(undefined);
      setToggleSort("none");
    } else {
      handleValueChange(`${sortingProperty}-asc`);
      setToggleSort("asc");
    }
  };

  return (
    <Button
      onClick={handleButtonClick}
      disabled={isDefaultActive}
      variant="ghost"
      {...args}
    >
      {children}
      {toggleSort === "asc" && <ArrowUp />}
      {toggleSort === "desc" && <ArrowDown />}
    </Button>
  );
};

export default function RadioSort({
  setSort,
  sortValue,
  sort,
  sortingOptions,
  setSortValue,
  noSort,
  callback,
  useDefaultSort = true,
  filterKey,
}: RadioSortProps) {
  const { handleValueChange, isDefaultSort } = useRadioSort({
    useDefaultSort,
    setSortValue,
    sortValue,
    sortingOptions,
    setSort,
    callback,
    filterKey,
  });
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
                disabled={
                  isDefaultSort &&
                  property === "createdAt" &&
                  direction === "desc"
                }
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
