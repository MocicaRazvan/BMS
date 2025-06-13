"use client";
import Image, { ImageProps } from "next/image";
import { ComponentProps, useMemo } from "react";

import useGetCustomImageBlur from "@/hoooks/images/use-get-custom-image-blur";

const springUrl = process.env.NEXT_PUBLIC_SPRING!;
const springClientUrl = process.env.NEXT_PUBLIC_SPRING_CLIENT!;

export default function CustomImage({
  src,
  thumblinator = false,
  alt = "image",
  ...rest
}: ComponentProps<typeof Image> & {
  thumblinator?: boolean;
  alt?: string;
}) {
  const { blurData } = useGetCustomImageBlur();
  const loader = useMemo(
    () => (isLoaderCompatible(thumblinator, src) ? imageLoader : rest.loader),
    [rest.loader, src, thumblinator],
  );

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
