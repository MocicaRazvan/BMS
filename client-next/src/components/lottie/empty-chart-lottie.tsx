"use client";

import emptyChartLottie from "@/../public/lottie/emptyChart.json";
import Lottie, { LottieProps } from "react-lottie-player";

export default function EmptyChartLottie(
  props: Omit<LottieProps, "animationData">,
) {
  return <Lottie animationData={emptyChartLottie} {...props} />;
}
