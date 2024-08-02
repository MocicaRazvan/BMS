"use client";

import { useSearchParams } from "next/navigation";
import { useCallback, useMemo, useState } from "react";
import { parseStringToBoolean } from "@/lib/utils";
import { Button } from "@/components/ui/button";

export interface UseApprovedFilterTexts {
  approved: string;
  notApproved: string;
  all: string;
}

export default function useApprovedFilter({
  approved: approvedText,
  notApproved,
  all,
}: UseApprovedFilterTexts) {
  const currentSearchParams = useSearchParams();
  const approvedSearch = currentSearchParams.get("approved");
  const [approved, setApproved] = useState<boolean | null>(
    parseStringToBoolean(approvedSearch),
  );

  const updateApprovedSearch = useCallback(
    (searchParams: URLSearchParams) => {
      if (approved !== null) {
        searchParams.set("approved", approved.toString());
      } else {
        searchParams.delete("approved");
      }
    },
    [approved],
  );

  const approveCriteria = useMemo(
    () => (
      <Button
        variant={"outline"}
        className="w-28"
        onClick={() =>
          setApproved((prev) => (prev === null ? true : !prev ? null : false))
        }
      >
        {approved === null ? all : approved ? approvedText : notApproved}
      </Button>
    ),
    [all, approved, approvedText, notApproved],
  );

  return { updateApprovedSearch, approveCriteria, approved };
}
