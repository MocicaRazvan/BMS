"use client";

import { useEffect, useRef, useState } from "react";

export function useDebounceWithFirstTrue(value: boolean, delay: number) {
  const [debounced, setDebounced] = useState(value);
  const firstTrue = useRef(false);

  useEffect(() => {
    if (value && !firstTrue.current) {
      firstTrue.current = true;
      setDebounced(true);
      return;
    }
    const timer = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);

  return debounced;
}
