"use client";
import Image from "next/image";
import { ImageProps } from "next/image";
import { ComponentProps, useState } from "react";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";
export default function CustomImage({
  src,
  // quality,
  // width,
  // height,
  // fill,
  thumblinator = false,
  ...rest
}: ComponentProps<typeof Image> & {
  thumblinator?: boolean;
}) {
  const [isLoaded, setIsLoaded] = useState(false);
  // let newSrc = src;
  // if (
  //   thumblinator &&
  //   typeof src === "string" &&
  //   src.startsWith(process.env.NEXT_PUBLIC_SPRING!)
  // ) {
  //   newSrc = `${src}?quality=${quality || 75}`;
  //   if (width) {
  //     const intWidth = convertToInt(width);
  //     newSrc = `${newSrc}&width=${intWidth}`;
  //   }
  //   if (height) {
  //     const intHeight = convertToInt(height);
  //     newSrc = `${newSrc}&height=${intHeight}`;
  //   }
  // }
  if (
    thumblinator &&
    typeof src === "string" &&
    (src.startsWith(process.env.NEXT_PUBLIC_SPRING!) ||
      src.startsWith(process.env.NEXT_PUBLIC_SPRING_CLIENT!))
  ) {
    rest.loader = imageLoader;
  }

  return (
    <div
      className={cn(
        "relative w-full h-full",
        rest.width && "w-[${width}px]",
        rest.height && "h-[${height}px]",
      )}
    >
      <Skeleton
        className={`w-full h-full absolute top-0 left-0 ${isLoaded ? "hidden" : "block"}`}
      />
      <Image
        {...rest}
        src={src}
        // width={!fill ? width : undefined}
        // height={!fill ? height : undefined}
        // fill={fill}
        // quality={100}
        onLoadingComplete={() => setIsLoaded(true)}
        className={cn(
          `transition-opacity duration-500 ease-in-out ${isLoaded ? "opacity-100" : "opacity-0"}`,
          rest.className,
        )}
      />
    </div>
  );
}

const convertToInt = (value: number | `${number}`) =>
  typeof value !== "number" ? Math.floor(parseFloat(value)) : Math.floor(value);

export const imageLoader: ImageProps["loader"] = ({ src, width, quality }) =>
  `${src.replace(process.env.NEXT_PUBLIC_SPRING!, process.env.NEXT_PUBLIC_SPRING_CLIENT!)}?width=${width}&q=${quality || 75}`;
