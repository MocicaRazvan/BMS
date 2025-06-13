"use client";

import { useTheme } from "next-themes";

const blurColors = {
  light: "hsl(180,1%,77%)",
  dark: "hsl(217.2, 32.6%, 17.5%)",
} as const;

function hslToBlurredDataURL(hsl: string): string {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="10" height="10"><defs><filter id="blur"><feGaussianBlur stdDeviation="4.25"/><feComponentTransfer><feFuncA type="linear" slope="0.85"/></feComponentTransfer></filter></defs><rect width="10" height="10" fill="${hsl}" filter="url(#blur)"/></svg>`;

  return `data:image/svg+xml;base64,${window.btoa(svg)}`;
}
function getBlurColor(theme?: string): string {
  const color =
    blurColors[theme as keyof typeof blurColors] ?? blurColors.light;
  return hslToBlurredDataURL(color);
}
export default function useGetCustomImageBlur() {
  const { theme } = useTheme();
  return {
    blurData: getBlurColor(theme),
  };
}
