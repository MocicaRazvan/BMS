"use client";
import { PreloadComponentType } from "@/lib/dynamic-with-preload";
import { useEffect } from "react";

export default function usePreloadDynamicComponents(
  components: PreloadComponentType<any> | PreloadComponentType<any>[],
  condition = true,
) {
  useEffect(() => {
    if (!condition) return;
    const preloadComponents = Array.isArray(components)
      ? components
      : [components];

    const preload = () => {
      if (preloadComponents.length === 0) return;
      preloadComponents.forEach((component) => {
        component.preload().catch((error) => {
          console.error("Error preloading component:", error);
        });
      });
    };
    preload();
  }, [condition]);
}
