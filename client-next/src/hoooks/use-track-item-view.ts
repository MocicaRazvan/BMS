"use client";
import { useEffect, useRef } from "react";
import { isbot } from "isbot";
import { fetchStream } from "@/lib/fetchers/fetchStream";

export default function useTrackItemView(
  path: string,
  delayMs = 3000,
  shouldFire = true,
  minScrollPercent = 10,
) {
  const hasSentView = useRef(false);
  const hasStartedTimer = useRef(false);
  const timerRef = useRef<NodeJS.Timeout>();

  useEffect(() => {
    if (hasSentView.current || isbot(navigator.userAgent) || !shouldFire)
      return;

    const sendView = async () => {
      if (!hasSentView.current) {
        const res = await fetchStream({
          path,
          method: "POST",
        });
        console.log("Sending view", path, res);
        hasSentView.current = true;
      }
    };

    const onScroll = () => {
      const scrollTop = window.scrollY || document.documentElement.scrollTop;
      const docHeight =
        document.documentElement.scrollHeight - window.innerHeight;
      const scrollPercent = (scrollTop / docHeight) * 100;

      if (!hasStartedTimer.current && scrollPercent >= minScrollPercent) {
        hasStartedTimer.current = true;
        timerRef.current = setTimeout(sendView, delayMs);
      }
    };

    const cancelTimer = () => clearTimeout(timerRef.current);

    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("pagehide", cancelTimer);
    window.addEventListener("beforeunload", cancelTimer);

    return () => {
      cancelTimer();
      window.removeEventListener("scroll", onScroll);
      window.removeEventListener("pagehide", cancelTimer);
      window.removeEventListener("beforeunload", cancelTimer);
    };
  }, [path, shouldFire, delayMs, minScrollPercent]);
}
