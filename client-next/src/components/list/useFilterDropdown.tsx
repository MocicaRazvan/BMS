"use client";
import { useSearchParams } from "next/navigation";
import { useCallback, useMemo, useState } from "react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";

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
  const currentSearchParams = useSearchParams();
  const fieldSearch = currentSearchParams.get(fieldKey);
  const [field, setField] = useState<FilterDropdownItem>(
    items.find(({ value }) => value === fieldSearch) || {
      label: noFilterLabel,
      value: "",
    },
  );

  const fieldDropdownFilterQueryParam = useMemo(
    () => ({ [fieldKey]: field.value }),
    [field, fieldKey],
  );

  const updateFieldDropdownFilter = useCallback(
    (searchParams: URLSearchParams) => {
      if (field.value) {
        searchParams.set(fieldKey, field.value);
      } else {
        searchParams.delete(fieldKey);
      }
    },
    [field, fieldKey],
  );

  const handleChange = useCallback(
    (value: string) => {
      setField((prev) =>
        prev.value === value
          ? {
              label: noFilterLabel,
              value: "",
            }
          : items.find((i) => i.value === value) || prev,
      );
    },
    [items, noFilterLabel],
  );

  const filedFilterCriteria = useMemo(
    () => (
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
          </DropdownMenuRadioGroup>
        </DropdownMenuContent>
      </DropdownMenu>
    ),
    [field.label, field.value, handleChange, items],
  );
  const filedFilterCriteriaCallback = useCallback(
    (callback: () => void) => (
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="outline">{`${field.label}`}</Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent className="w-56">
          <DropdownMenuRadioGroup
            value={field.value}
            onValueChange={(e) => {
              handleChange(e);
              callback();
            }}
          >
            {items.map(({ label, value }) => (
              <DropdownMenuRadioItem value={value} key={value}>
                {label}
              </DropdownMenuRadioItem>
            ))}
          </DropdownMenuRadioGroup>
        </DropdownMenuContent>
      </DropdownMenu>
    ),
    [field.label, field.value, handleChange, items],
  );

  return {
    updateFieldDropdownFilter,
    filedFilterCriteria,
    fieldDropdownFilterQueryParam,
    value: field.value,
    filedFilterCriteriaCallback,
  };
}
