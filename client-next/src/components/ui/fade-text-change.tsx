"use client";

import { motion, AnimatePresence } from "framer-motion";
import { useState, useEffect, ComponentProps } from "react";

interface Props extends ComponentProps<typeof motion.p> {
  text: string;
}

export default function FadeTextChange({ text, ...rest }: Props) {
  const [isFirstRender, setIsFirstRender] = useState(true);

  useEffect(() => {
    setIsFirstRender(false);
  }, []);

  return (
    <AnimatePresence mode="wait">
      <motion.p
        {...rest}
        key={text}
        initial={isFirstRender ? undefined : { opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        transition={{ duration: 0.3, ease: "easeInOut" }}
      >
        {text}
      </motion.p>
    </AnimatePresence>
  );
}
