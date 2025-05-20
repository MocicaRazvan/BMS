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
import { ArrowDown, ArrowUp, ArrowUpDown } from "lucide-react";
import { useSearchParams } from "next/navigation";
import { cn, parseSortString } from "@/lib/utils";
import { Separator } from "@/components/ui/separator";

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
  clasName?: string;
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

const useRadioSortButton = ({
  sortingProperty,
  radioArgs,
  showNone,
}: {
  sortingProperty: string;
  radioArgs: UseRadioSortArgs;
  showNone: boolean;
}) => {
  const sortParam = useSearchParams().get("sort");
  const splitParams = sortParam?.split(":") || [];

  const paramKey = splitParams?.at(0);
  const paramValue = splitParams?.at(1);
  const curSO = radioArgs.sortingOptions.filter(
    (s) => s.property.toLowerCase() === sortingProperty.toLowerCase(),
  );
  const { handleValueChange, isDefaultSort } = useRadioSort(radioArgs);
  const canBeDefault = useMemo(
    () =>
      curSO.some((s) => s.property === "createdAt" && s.direction === "desc"),
    [curSO],
  );
  const isDefaultActive = isDefaultSort && canBeDefault;
  const [toggleSort, setToggleSort] = useState<SortingOption["direction"]>(
    () => {
      if (
        (paramKey === sortingProperty && paramValue === "desc") ||
        paramValue === "asc"
      ) {
        return paramValue;
      }
      if (showNone) {
        return "none";
      } else {
        return "desc";
      }
    },
  );

  useEffect(() => {
    if (paramKey !== sortingProperty) {
      setToggleSort("none");
    } else if (
      paramKey === "createdAt" &&
      paramValue === "desc" &&
      canBeDefault
    ) {
      setToggleSort("desc");
    }
  }, [canBeDefault, paramKey, paramValue, sortingProperty]);

  if (curSO.length !== 2) return null;

  const handleButtonClick = () => {
    if (toggleSort === "none") {
      handleValueChange(`${sortingProperty}-desc`);
      setToggleSort("desc");
    } else if (toggleSort === "desc") {
      handleValueChange(`${sortingProperty}-asc`);
      setToggleSort("asc");
    } else if (toggleSort === "asc") {
      if (isDefaultActive || !showNone) {
        handleValueChange(`${sortingProperty}-desc`);
        setToggleSort("desc");
      } else {
        handleValueChange(undefined);
        setToggleSort("none");
      }
    }
  };

  const handleManualClick = (direction: SortingOption["direction"]) => {
    if (direction === "none" || direction === toggleSort) {
      if (showNone) {
        handleValueChange(undefined);
        setToggleSort("none");
      } else {
        handleValueChange(`${sortingProperty}-desc`);
        setToggleSort("desc");
      }
    } else {
      handleValueChange(`${sortingProperty}-${direction}`);
      setToggleSort(direction);
    }
  };

  return {
    toggleSort,
    handleButtonClick,
    handleManualClick,
    canBeDefault,
    isDefaultActive,
  };
};

interface RadioSortButtonProps extends ButtonProps {
  sortingProperty: string;
  radioArgs: UseRadioSortArgs;
  children: ReactNode;
  showNone?: boolean;
}
export const RadioSortButton = ({
  sortingProperty,
  radioArgs,
  children,
  className,
  showNone = true,
  ...args
}: RadioSortButtonProps) => {
  const radioSortButtonHook = useRadioSortButton({
    sortingProperty,
    radioArgs,
    showNone,
  });
  if (!radioSortButtonHook) return children;
  const { toggleSort, handleButtonClick } = radioSortButtonHook;
  return (
    <Button
      onClick={handleButtonClick}
      // disabled={isDefaultActive}
      variant="ghost"
      className={cn(
        "flex items-center justify-between gap-1 px-1.5 py-1",
        className,
      )}
      {...args}
    >
      {children}
      <div className="w-4 h-4 flex items-center justify-center">
        {toggleSort === "asc" && <ArrowUp />}
        {toggleSort === "desc" && <ArrowDown />}
        {toggleSort === "none" && <ArrowUpDown />}
      </div>
    </Button>
  );
};

const directionToArrow = (
  direction: SortingOption["direction"],
  className = "",
) => {
  if (direction === "asc") return <ArrowUp className={className} />;
  if (direction === "desc") return <ArrowDown className={className} />;
  return <ArrowUpDown className={className} />;
};

export type RadioButtonGroupProps = Omit<RadioSortButtonProps, "children"> & {
  showNone?: boolean;
  className?: string;
};

export const RadioSortButtonGroup = ({
  sortingProperty,
  radioArgs,
  className,
  showNone = true,
  ...args
}: RadioButtonGroupProps) => {
  const radioSortButtonHook = useRadioSortButton({
    sortingProperty,
    radioArgs,
    showNone,
  });
  if (!radioSortButtonHook) return null;
  const { toggleSort, handleManualClick } = radioSortButtonHook;
  const directions = ["asc", "desc"];
  if (showNone) directions.push("none");
  return (
    <div className={cn("flex flex-col items-center justify-center", className)}>
      {directions.map((direction) => (
        <Button
          onClick={() =>
            handleManualClick(direction as SortingOption["direction"])
          }
          // disabled={isDefaultActive}
          variant="ghost"
          className={cn(
            "flex items-center justify-between w-full font-normal",
            toggleSort === direction && "bg-muted",
          )}
          key={direction + sortingProperty}
          {...args}
        >
          <p className="capitalize">{direction}</p>
          {directionToArrow(direction as SortingOption["direction"], "w-4 h-4")}
        </Button>
      ))}
    </div>
  );
};

export interface RadioSortDropDownWithExtraProps extends RadioButtonGroupProps {
  trigger: ReactNode;
  extraContent?: ReactNode;
  dummy?: boolean;
  sortClassName?: string;
}

export function RadioSortDropDownWithExtra({
  sortingProperty,
  radioArgs,
  className,
  trigger,
  showNone = true,
  extraContent,
  sortClassName,
  ...args
}: RadioSortDropDownWithExtraProps) {
  const [isOpened, setIsOpened] = useState(false);
  const radioSortButtonHook = useRadioSortButton({
    sortingProperty,
    radioArgs,
    showNone,
  });
  if (!radioSortButtonHook) return null;
  const { toggleSort, handleManualClick, isDefaultActive } =
    radioSortButtonHook;
  const directions = ["asc", "desc"];
  if (showNone) directions.push("none");
  return (
    <DropdownMenu open={isOpened} onOpenChange={setIsOpened}>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          className={cn(
            "flex items-center justify-between gap-1 px-1.5 py-1",
            className,
          )}
        >
          {trigger}
          <div className="w-4 h-4 flex items-center justify-center">
            {toggleSort === "asc" && <ArrowUp />}
            {toggleSort === "desc" && <ArrowDown />}
            {toggleSort === "none" && <ArrowUpDown />}
          </div>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className={cn("px-1 pb-1.5", className)}>
        <div className={cn("space-y-1 w-full", sortClassName)}>
          {directions.map((direction) => (
            <Button
              onClick={() =>
                handleManualClick(direction as SortingOption["direction"])
              }
              disabled={direction === "desc" && isDefaultActive}
              // disabled={isDefaultActive}
              variant="ghost"
              className={cn(
                "flex items-center justify-between w-full font-normal ",
                toggleSort === direction && "bg-muted",
              )}
              key={direction + sortingProperty}
              {...args}
            >
              <p className="capitalize">{direction}</p>
              {directionToArrow(
                direction as SortingOption["direction"],
                "w-4 h-4",
              )}
            </Button>
          ))}
        </div>
        {extraContent && <Separator className="w-full my-2" />}

        {extraContent}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
export function RadioSortDropDownWithExtraDummy({
  trigger,
  extraContent,
  className,
}: {
  trigger: ReactNode;
  extraContent?: ReactNode;
  className?: string;
}) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          className={cn(
            "flex items-center justify-between gap-1 px-1.5 py-1",
            className,
          )}
        >
          {trigger}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className={cn("px-1 py-1.5", className)}>
        {extraContent}
      </DropdownMenuContent>
    </DropdownMenu>
  );
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
  filterKey,
  clasName,
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
      <DropdownMenuContent
        className={cn("w-56 overflow-y-auto max-h-[250px]", clasName)}
      >
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
