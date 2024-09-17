"use client";
import * as React from "react";
import { useEffect } from "react";
import confetti from "canvas-confetti";

import Lottie from "react-lottie-player";
import orderCompleted from "../../../../../../public/lottie/orderCompleted.json";
import { motion } from "framer-motion";

export default function OrderCompletePageContent() {
  const showConfetti = () => {
    const duration = 3 * 1000;
    const animationEnd = Date.now() + duration;
    const defaults = { startVelocity: 30, spread: 360, ticks: 60, zIndex: 0 };

    const randomInRange = (min: number, max: number) =>
      Math.random() * (max - min) + min;

    const interval = window.setInterval(() => {
      const timeLeft = animationEnd - Date.now();

      if (timeLeft <= 0) {
        return clearInterval(interval);
      }

      const particleCount = 50 * (timeLeft / duration);
      confetti({
        ...defaults,
        particleCount,
        origin: { x: randomInRange(0.1, 0.3), y: Math.random() - 0.2 },
      });
      confetti({
        ...defaults,
        particleCount,
        origin: { x: randomInRange(0.7, 0.9), y: Math.random() - 0.2 },
      });
    }, 250);
  };

  useEffect(() => {
    showConfetti();
  }, []);

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.5 }}
    >
      <Lottie
        loop={false}
        animationData={orderCompleted}
        play
        className="md:w-1/3 md:h-1/3 mx-auto"
      />
    </motion.div>
  );
}
