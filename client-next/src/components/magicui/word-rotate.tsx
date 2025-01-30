"use client";

import { AnimatePresence, motion, MotionProps } from "framer-motion";
import { useEffect, useState } from "react";

import { cn } from "@/lib/utils";

interface BaseWordProps {
  motionProps?: MotionProps;
  className?: string;
  wrapperClassName?: string;
}

interface WordRotateProps extends BaseWordProps {
  words: string[];
  duration?: number;
}

const defaultMotionPros = {
  initial: { opacity: 0, y: -50 },
  animate: { opacity: 1, y: 0 },
  exit: { opacity: 0, y: 50 },
  transition: { duration: 0.3, ease: "easeOut" },
};

export function WordRotate({
  words,
  duration = 2500,
  motionProps = defaultMotionPros,
  className,
  wrapperClassName,
}: WordRotateProps) {
  const [index, setIndex] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setIndex((prevIndex) => (prevIndex + 1) % words.length);
    }, duration);

    // Clean up interval on unmount
    return () => clearInterval(interval);
  }, [words, duration]);

  return (
    <div className={cn("overflow-hidden py-2", wrapperClassName)}>
      <AnimatePresence mode="wait">
        <motion.h1
          key={words[index]}
          className={cn(className)}
          {...motionProps}
        >
          {words[index]}
        </motion.h1>
      </AnimatePresence>
    </div>
  );
}

interface StaticWordSwapProps extends BaseWordProps {
  firstWord: string;
  secondWord: string;
  delay?: number;
}

export function StaticFirstWordRotate({
  firstWord,
  secondWord,
  delay = 2500,
  motionProps = defaultMotionPros,
  className,
  wrapperClassName,
}: StaticWordSwapProps) {
  const [showSecondWord, setShowSecondWord] = useState(false);

  useEffect(() => {
    const timeout = setTimeout(() => {
      setShowSecondWord(true);
    }, delay);

    return () => clearTimeout(timeout);
  }, [delay]);

  return (
    <div className={cn("overflow-hidden py-2", wrapperClassName)}>
      {!showSecondWord ? (
        <h1 className={cn(className)}>{firstWord}</h1>
      ) : (
        <motion.h1 className={cn(className)} {...motionProps}>
          {secondWord}
        </motion.h1>
      )}
    </div>
  );
}
