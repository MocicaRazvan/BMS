"use client";
import { useSearchParams } from "next/navigation";
import {
  Dispatch,
  SetStateAction,
  useCallback,
  useMemo,
  useState,
} from "react";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Label } from "@/components/ui/label";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { ClassValue } from "clsx";

export interface FilterDropdownItem {
  label: string;
  value: string;
}

export interface UseFilterDropdownTexts {
  noFilterLabel: string;
  labels: {
    [key: string]: string;
  };
}

interface Args {
  items: FilterDropdownItem[];
  fieldKey: string;
  noFilterLabel: string;
}

export default function useFilterDropdown({
  items,
  fieldKey,
  noFilterLabel,
}: Args) {
  const trimmedFieldKey = useMemo(() => fieldKey.trim(), [fieldKey]);
  const currentSearchParams = useSearchParams();
  const fieldSearch = currentSearchParams.get(trimmedFieldKey);
  const [field, setField] = useState<FilterDropdownItem>(
    items.find(({ value }) => value === fieldSearch) || {
      label: noFilterLabel,
      value: "",
    },
  );

  const fieldDropdownFilterQueryParam = useMemo(
    () => ({ [trimmedFieldKey]: field.value }),
    [field, trimmedFieldKey],
  );

  const updateFieldDropdownFilter = useCallback(
    (searchParams: URLSearchParams) => {
      if (field.value) {
        searchParams.set(trimmedFieldKey, field.value);
      } else {
        searchParams.delete(trimmedFieldKey);
      }
    },
    [field, trimmedFieldKey],
  );

  return {
    updateFieldDropdownFilter,
    fieldDropdownFilterQueryParam,
    value: field.value,
    field,
    setField,
    items,
  };
}
interface CriteriaWithCallbackProps extends Args {
  setGlobalFilter: Dispatch<SetStateAction<FilterDropdownItem>>;
  callback: () => void;
}

const useFieldWithParams = ({
  fieldKey,
  items,
  noFilterLabel,
  setGlobalFilter,
  callback,
  emptyValue,
}: CriteriaWithCallbackProps & {
  emptyValue: string;
}) => {
  const currentSearchParams = useSearchParams();
  const fieldSearch = currentSearchParams.get(fieldKey.trim());
  const field = useMemo(
    () =>
      items.find(({ value }) => value === fieldSearch) || {
        label: noFilterLabel,
        value: "",
      },
    [fieldSearch, JSON.stringify(items), noFilterLabel],
  );
  const handleChange = useCallback(
    (value: string) => {
      setGlobalFilter((prev) =>
        value === emptyValue
          ? {
              label: noFilterLabel,
              value: "",
            }
          : items.find((i) => i.value === value) || prev,
      );
      callback();
    },
    [callback, JSON.stringify(items), noFilterLabel, setGlobalFilter],
  );
  return {
    field,
    handleChange,
  };
};

interface RadioFieldFilterCriteriaCallbackProps
  extends CriteriaWithCallbackProps {
  className?: ClassValue;
}

export function RadioFieldFilterCriteriaCallback({
  items,
  fieldKey,
  noFilterLabel,
  className,
  ...rest
}: RadioFieldFilterCriteriaCallbackProps) {
  const { field, handleChange } = useFieldWithParams({
    ...rest,
    fieldKey,
    items,
    noFilterLabel,
    emptyValue: "null",
  });

  return (
    <RadioGroup
      defaultValue={field.value || "null"}
      className={cn("px-2 py-1.5 gap-4", className)}
      onValueChange={handleChange}
    >
      {items.map(({ label, value }) => (
        <Label
          htmlFor={fieldKey + value}
          className="flex items-center space-x-2 rounded hover:bg-muted cursor-pointer px-4 py-2 gap-2"
          key={value}
        >
          <RadioGroupItem
            value={value}
            id={fieldKey + value}
            className="ring-none outline-none border-none"
          />
          <p>{label}</p>
        </Label>
      ))}
      <Label
        htmlFor={fieldKey + "null"}
        className="flex items-center space-x-2 rounded hover:bg-muted cursor-pointer px-4 py-2 gap-2"
      >
        <RadioGroupItem
          value={"null"}
          id={fieldKey + "null"}
          className="ring-none outline-none border-none"
        />
        <p>{noFilterLabel}</p>
      </Label>
    </RadioGroup>
  );
}
export function DropDownFieldFilterCriteriaCallback({
  items,
  fieldKey,
  noFilterLabel,
  ...rest
}: CriteriaWithCallbackProps) {
  const { field, handleChange } = useFieldWithParams({
    ...rest,
    fieldKey,
    items,
    noFilterLabel,
    emptyValue: "",
  });

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline">{`${field.label}`}</Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56">
        <DropdownMenuRadioGroup
          value={field.value}
          onValueChange={handleChange}
        >
          {items.map(({ label, value }) => (
            <DropdownMenuRadioItem value={value} key={value}>
              {label}
            </DropdownMenuRadioItem>
          ))}
          <DropdownMenuRadioItem value={""}>
            {noFilterLabel}
          </DropdownMenuRadioItem>
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
