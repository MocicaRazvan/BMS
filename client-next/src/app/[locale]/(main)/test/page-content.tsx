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
import {
  PredictionChart,
  PredictionChartTexts,
} from "@/components/charts/prediction-chart";

interface Props {
  texts: PredictionChartTexts;
}
export default function PageContent({ texts }: Props) {
  return (
    <div className="w-full h-full">
      <PredictionChart
        path="/orders/admin/countAndAmount/prediction"
        texts={texts}
      />
    </div>
  );
}
