"use client";

import { useSearchParams } from "next/navigation";
import { useCallback, useMemo, useState } from "react";
import { parseStringToBoolean } from "@/lib/utils";
import { Button } from "@/components/ui/button";

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

  return { updateFieldSearch, fieldCriteria, field, fieldCriteriaCallBack };
}
