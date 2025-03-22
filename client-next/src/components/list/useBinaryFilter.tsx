"use client";

import { useSearchParams } from "next/navigation";
import { useCallback, useMemo, useState } from "react";
import { parseStringToBoolean } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Label } from "@/components/ui/label";

export interface UseBinaryTexts {
  trueText: string;
  falseText: string;
  all: string;
}

interface Args extends UseBinaryTexts {
  fieldKey: string;
}

export default function useBinaryFilter({
  fieldKey,
  trueText,
  falseText,
  all,
}: Args) {
  const currentSearchParams = useSearchParams();
  const fieldSearch = currentSearchParams.get(fieldKey);
  const [field, setField] = useState<boolean | null>(
    parseStringToBoolean(fieldSearch),
  );

  const updateFieldSearch = useCallback(
    (searchParams: URLSearchParams) => {
      if (field !== null) {
        searchParams.set(fieldKey, field.toString());
      } else {
        searchParams.delete(fieldKey);
      }
    },
    [field, fieldKey],
  );

  const fieldCriteria = useMemo(
    () => (
      <Button
        variant={"outline"}
        onClick={() =>
          setField((prev) => (prev === null ? true : !prev ? null : false))
        }
      >
        {field === null ? all : field ? trueText : falseText}
      </Button>
    ),
    [all, falseText, field, trueText],
  );
  const fieldCriteriaCallBack = useCallback(
    (callback: () => void) => (
      <Button
        variant={"outline"}
        onClick={() => {
          setField((prev) => (prev === null ? true : !prev ? null : false));
          callback();
        }}
      >
        {field === null ? all : field ? trueText : falseText}
      </Button>
    ),
    [all, falseText, field, trueText],
  );

  const fieldCriteriaRadioCallback = useCallback(
    (callback: () => void) => (
      <RadioGroup
        defaultValue="null"
        className="px-2 py-1.5 gap-4"
        onValueChange={(value) => {
          setField(value === "null" ? null : value === "true");
          callback();
        }}
      >
        <Label
          htmlFor={fieldKey + "-null"}
          className="flex items-center space-x-2 rounded hover:bg-muted cursor-pointer px-4 py-2 gap-2"
        >
          <RadioGroupItem
            value="null"
            id={fieldKey + "-null"}
            className="ring-none outline-none border-none"
          />
          <p>{all}</p>
        </Label>

        <Label
          htmlFor={fieldKey + "-true"}
          className="flex items-center space-x-2 rounded hover:bg-muted cursor-pointer px-4 py-2 gap-2"
        >
          <RadioGroupItem
            value="true"
            id={fieldKey + "-true"}
            className="ring-none outline-none border-none rounded hover:bg-muted cursor-pointer"
          />
          <p>{trueText}</p>
        </Label>

        <Label
          htmlFor={fieldKey + "-false"}
          className="flex items-center space-x-2 rounded hover:bg-muted cursor-pointer px-4 py-2 gap-2"
        >
          <RadioGroupItem
            value="false"
            id={fieldKey + "-false"}
            className="ring-none outline-none border-none "
          />
          <p>{falseText}</p>
        </Label>
      </RadioGroup>
    ),
    [all, falseText, trueText, fieldKey],
  );

  return {
    updateFieldSearch,
    fieldCriteria,
    field,
    fieldCriteriaCallBack,
    fieldCriteriaRadioCallback,
  };
}
