"use client";

import noResultsLottie from "@/../public/lottie/noResults.json";
import Lottie, { LottieComponentProps } from "lottie-light-react";

export default function NoResultsLottie(
  props: Omit<LottieComponentProps, "animationData">,
) {
  return <Lottie animationData={noResultsLottie} {...props} />;
}
