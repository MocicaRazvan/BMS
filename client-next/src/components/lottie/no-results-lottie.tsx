"use client";

import noResultsLottie from "@/../public/lottie/noResults.json";
import Lottie, { LottieProps } from "react-lottie-player";

export default function NoResultsLottie(
  props: Omit<LottieProps, "animationData">,
) {
  return <Lottie animationData={noResultsLottie} {...props} />;
}
