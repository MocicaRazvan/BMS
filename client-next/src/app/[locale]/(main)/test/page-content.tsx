"use client";

import TopChartWrapper, {
  TopChartWrapperTexts,
} from "@/components/charts/top-chart-wrapper";
import { TopTrainersSummary, TopUsersSummary } from "@/types/dto";
import React, { useEffect, useState } from "react";
import { useLocale } from "next-intl";
import { Locale } from "@/navigation";
import TopTrainers, {
  TopTrainersTexts,
} from "@/components/charts/top-trainers";
import {
  getCsrfNextAuth,
  getCsrfNextAuthHeader,
} from "@/actions/get-csr-next-auth";
import { getCsrfToken } from "next-auth/react";
import useCsrfToken from "@/hoooks/useCsrfToken";
import { Button } from "@/components/ui/button";

interface Props {
  texts: TopTrainersTexts;
}
export default function PageContent({ texts }: Props) {
  const [state, setState] = useState(false);
  const locale = useLocale();
  const { csrfRawToken, addTokenConditionally } = useCsrfToken();
  useEffect(() => {
    if (csrfRawToken) {
      fetch("/api/test/post", {
        method: "POST",
        headers: {
          ...addTokenConditionally(),
        },
      })
        .then((r) => r.json())
        .then((r) => console.log("/api/test/post Response", r))
        .catch((e) => console.error("/api/test/post Error", e));
    }
  }, [addTokenConditionally, csrfRawToken]);
  return (
    <div>
      <Button onClick={() => setState((prev) => !prev)}>Click me</Button>
    </div>
  );
}
