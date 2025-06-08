"use client";

import emptyChartLottie from "@/../public/lottie/emptyChart.json";
import Lottie, { LottieComponentProps } from "lottie-light-react";

export default function EmptyChartLottie(
  props: Omit<LottieComponentProps, "animationData">,
) {
  return <Lottie animationData={emptyChartLottie} {...props} />;
}
