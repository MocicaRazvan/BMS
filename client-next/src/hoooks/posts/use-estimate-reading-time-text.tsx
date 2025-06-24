"use client";

import { useMemo } from "react";
import { estimateReadingTime } from "@/lib/reading-time/estimator";
import { Locale } from "@/navigation/navigation";

export default function useEstimateReadingTimeText(
  locale: Locale,
  postBody: string | undefined,
) {
  return useMemo(
    () => (!postBody ? "" : estimateReadingTime(postBody, 200, locale).text),
    [postBody, locale],
  );
}
