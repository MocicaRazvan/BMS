"use client";

import TopChartWrapper, {
  TopChartWrapperTexts,
} from "@/components/charts/top-chart-wrapper";
import { TopTrainersSummary, TopUsersSummary } from "@/types/dto";
import React from "react";
import { useLocale } from "next-intl";
import { Locale } from "@/navigation";
import TopTrainers, {
  TopTrainersTexts,
} from "@/components/charts/top-trainers";

interface Props {
  texts: TopTrainersTexts;
}
export default function PageContent({ texts }: Props) {
  const locale = useLocale();
  return <TopTrainers texts={texts} locale={locale as Locale} />;
}
