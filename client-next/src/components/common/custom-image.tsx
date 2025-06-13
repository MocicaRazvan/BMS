"use client";
import Image, { ImageProps } from "next/image";
import { ComponentProps, useMemo } from "react";
import { useTheme } from "next-themes";

const springUrl = process.env.NEXT_PUBLIC_SPRING!;
const springClientUrl = process.env.NEXT_PUBLIC_SPRING_CLIENT!;

const blurImages = {
  light:
    "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAxUlEQVR4AYyOSw7CMAxE7RRaoHAltixZIXHh9kJ8+qMxbyIOgNRRpvPsSVLXdee+7y+cV84b5136eWVi5+Tu+5xzG+5tRBzN7CTJKxPTTCLYAXYe0Wb3g6Akr0xMM2qs2WgAtZvtgU0RvmTuDbxWY8XWhqEtA1U220ryysRorBLmry9RuzL5CbOFt61sLpK8MjHN6OqZ6gkzAwYGpiKzoWQRE3xW48jWCHwB3rztJckrE6Nx5D+GlFKBBE/AQ5IvCzAahy8AAAD//0KAvakAAAAGSURBVAMA6lWcOecma3cAAAAASUVORK5CYII=",
  dark: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAA20lEQVR4AYyKMW7CQBRE5/9d2xCTLl3kmgLlAiihTZkqUu5/hbCA7d3/2VkuwEgjPc0bnT5Op+nw9T3tjz/v+8/ft8PxjyVza65+FOZbFxsX8XGB7QLwypK50fGjcGxWCRsEGRHLC1DGVnLdmqsfNXhvmgdD6Xcet2MMA0vm9nDe6ywSNCMq0Il4WM06lqxAR9c+eDI6uBeLyAas7lI61ZUlc6PjRxWyqMVZEZaz5GvKZWbJ3B6ufiC4dV5uKJ6QwwUIqZVct+bqR6FyFdfUu6Qeei7AP0vmRsfPHQAA//9NiXlFAAAABklEQVQDAAu5ihvg5ZDdAAAAAElFTkSuQmCC",
};

// const blurColors = {
//   light: "hsl(180,1%,77%)",
//   dark: "hsl(217.2, 32.6%, 17.5%)",
// } as const;
//
// function hslToBlurredDataURL(hsl: string): string {
//   const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="10" height="10"><defs><filter id="blur"><feGaussianBlur stdDeviation="4.25"/><feComponentTransfer><feFuncA type="linear" slope="0.85"/></feComponentTransfer></filter></defs><rect width="10" height="10" fill="${hsl}" filter="url(#blur)"/></svg>`;
//
//   return `data:image/svg+xml;base64,${window.btoa(svg)}`;
// }
// function getBlurColor(theme?: string): string {
//   const color = blurColors[theme as keyof typeof blurColors] ?? blurColors.dark;
//   return hslToBlurredDataURL(color);
// }

export default function CustomImage({
  src,
  thumblinator = false,
  alt = "image",
  ...rest
}: ComponentProps<typeof Image> & {
  thumblinator?: boolean;
  alt?: string;
}) {
  const { theme } = useTheme();

  const loader = useMemo(
    () => (isLoaderCompatible(thumblinator, src) ? imageLoader : rest.loader),
    [rest.loader, src, thumblinator],
  );
  const blurData =
    blurImages[theme as keyof typeof blurImages] ?? blurImages.dark;

  return (
    <Image
      alt={alt}
      {...rest}
      src={src}
      placeholder="blur"
      blurDataURL={blurData}
      loader={loader}
    />
  );
}

const convertToInt = (value: number | `${number}`) =>
  typeof value !== "number" ? Math.floor(parseFloat(value)) : Math.floor(value);

export const imageLoader: ImageProps["loader"] = ({ src, width, quality }) => {
  const finalSrc =
    springUrl !== springClientUrl
      ? src.replace(springUrl, springClientUrl)
      : src;
  return `${finalSrc}?width=${width}&q=${quality || 75}`;
};

export function isLoaderCompatible(
  thumblinator: undefined | boolean,
  src: unknown,
) {
  return (
    thumblinator &&
    typeof src === "string" &&
    (src.startsWith(springUrl) || src.startsWith(springClientUrl))
  );
}
