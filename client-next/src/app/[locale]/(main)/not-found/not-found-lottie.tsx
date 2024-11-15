"use client";
import notFoundLottie from "../../../../../public/lottie/notFoundLottie.json";
import Lottie from "react-lottie-player";
import { memo } from "react";

const NotFoundLottie = memo(() => {
  return (
    <Lottie
      animationData={notFoundLottie}
      loop={false}
      className="md:w-1/3 md:h-1/3 mx-auto"
      play
    />
  );
});

NotFoundLottie.displayName = "NotFoundLottie";
export default NotFoundLottie;
