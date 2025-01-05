"use client";

import React, { memo, useEffect, useState } from "react";
import { isDeepEqual } from "@/lib/utils";

interface Props {
  containerRef?: React.RefObject<HTMLDivElement>;
  className?: string;
  height?: number;
  topItemId?: string;
}

const ScrollProgress = memo(
  ({
    containerRef,
    className = "",
    height = 4,
    topItemId = "top-item",
  }: Props) => {
    const [scrollProgress, setScrollProgress] = useState(0);
    const [topItemDimensions, setTopItemDimensions] = useState({
      offsetTop: 0,
      offsetLeft: 0,
    });

    useEffect(() => {
      const navElement = document.querySelector(`#${topItemId}`);
      if (navElement) {
        const rect = navElement.getBoundingClientRect();
        setTopItemDimensions({
          offsetTop: navElement.clientHeight,
          offsetLeft: rect.left,
        });
      }

      const handleScroll = () => {
        const target = containerRef?.current || document.documentElement;

        const { scrollTop, scrollHeight, clientHeight } = target;
        const totalHeight = scrollHeight - clientHeight;
        const progress = (scrollTop / totalHeight) * 100;

        setScrollProgress(progress);
      };

      const target = containerRef?.current || window;

      target.addEventListener("scroll", handleScroll);
      target.addEventListener("resize", handleScroll);

      const resizeObserver = new ResizeObserver(() => {
        handleScroll();
      });

      resizeObserver.observe(document.body);

      return () => {
        target.removeEventListener("scroll", handleScroll);
        target.removeEventListener("resize", handleScroll);
        resizeObserver.disconnect();
      };
    }, [containerRef]);

    return (
      <div
        className={`fixed  left-0 w-full bg-primary  z-[49] ${className} 
      backdrop-blur supports-[backdrop-filter]:bg-primary/80 transition-all duration-300 ease-in-out`}
        style={{
          top: `${topItemDimensions.offsetTop}px`,
          width: `${scrollProgress}%`,
          height: `${height}px`,
          left: `${topItemDimensions.offsetLeft}px`,
          // transition: "width 0.3s ease",
          // zIndex: 99999,
        }}
      />
    );
  },
  isDeepEqual,
);

ScrollProgress.displayName = "ScrollProgress";

export default ScrollProgress;
