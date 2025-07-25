import { useEffect, useRef, useState } from "react";

export function useDebounceWithCallBack<T>(
  value: T,
  delay?: number,
  callBack?: (value: T) => void,
): T {
  const isFirstRun = useRef(true);
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedValue(value);
      if (isFirstRun.current) {
        isFirstRun.current = false;
      } else {
        callBack?.(value);
      }
    }, delay || 500);

    return () => {
      clearTimeout(timer);
    };
  }, [JSON.stringify(value), delay, callBack]);

  return debouncedValue;
}
