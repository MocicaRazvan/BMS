"use client";
import { AnimatePresence, motion } from "framer-motion";
import { useEffect, useState } from "react";

interface BlurItemProps {
  text: string;
  duration?: number;
  delay?: number;
  startScale?: number;
}
export function BlurIn({
  text,
  duration = 1,
  delay = 0,
  startScale = 1,
}: BlurItemProps) {
  return (
    <motion.p
      initial={{ filter: "blur(20px)", opacity: 0 }}
      animate={{ filter: "blur(0px)", opacity: 1, scale: [startScale, 1] }}
      transition={{ duration, delay, ease: "easeOut" }}
    >
      {text}
    </motion.p>
  );
}
export function BlurOut({ text, duration = 1, delay = 0 }: BlurItemProps) {
  return (
    <motion.p
      initial={{ filter: "blur(0px)", opacity: 1 }}
      animate={{ filter: "blur(0px)", opacity: 1 }}
      exit={{ filter: "blur(20px)", opacity: 0 }}
      transition={{ duration, delay, ease: "easeIn" }}
    >
      {text}
    </motion.p>
  );
}

interface BlurInOutTexts {
  inText: string;
  outText: string;
  duration?: number;
  changeTime?: number;
  inStartScale?: number;
}

export function BlurInOut({
  inText,
  outText,
  duration = 1,
  changeTime = 3,
  inStartScale = 1,
}: BlurInOutTexts) {
  const [changeText, setChangeText] = useState(false);

  useEffect(() => {
    const timeout = setTimeout(() => {
      setChangeText(true);
    }, changeTime * 1000);
    return () => clearTimeout(timeout);
  }, [changeTime]);

  return (
    <AnimatePresence mode="wait">
      {!changeText ? (
        <BlurOut key={outText + "out"} text={outText} duration={duration} />
      ) : (
        <BlurIn
          key={inText + "in"}
          text={inText}
          duration={duration}
          startScale={inStartScale}
        />
      )}
    </AnimatePresence>
  );
}
