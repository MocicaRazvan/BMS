"use client";

import { useMemo } from "react";
import { estimateReadingTime } from "@/lib/reading-time/estimator";
import { useLocale } from "next-intl";
import { Locale } from "@/navigation/navigation";

export default function useEstimateReadingTimeText(
  postBody: string | undefined,
) {
  const locale = useLocale() as Locale;
  return useMemo(
    () => (!postBody ? "" : estimateReadingTime(postBody, 200, locale).text),
    [postBody, locale],
  );
}
