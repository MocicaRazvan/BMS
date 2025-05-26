"use client";

import * as React from "react";
import * as AvatarPrimitive from "@radix-ui/react-avatar";
import Image, { StaticImageData } from "next/image";

import { cn } from "@/lib/utils";
import { useMemo } from "react";
import {
  imageLoader,
  isLoaderCompatible,
} from "@/components/common/custom-image";

const Avatar = React.forwardRef<
  React.ElementRef<typeof AvatarPrimitive.Root>,
  React.ComponentPropsWithoutRef<typeof AvatarPrimitive.Root>
>(({ className, ...props }, ref) => (
  <AvatarPrimitive.Root
    ref={ref}
    className={cn(
      "relative flex h-10 w-10 shrink-0 overflow-hidden rounded-full",
      className,
    )}
    {...props}
  />
));
Avatar.displayName = AvatarPrimitive.Root.displayName;

// const AvatarImage = React.forwardRef<
//   React.ElementRef<typeof AvatarPrimitive.Image>,
//   React.ComponentPropsWithoutRef<typeof AvatarPrimitive.Image>
// >(({ className, ...props }, ref) => (
//   <AvatarPrimitive.Image
//     ref={ref}
//     className={cn("aspect-square h-full w-full", className)}
//     {...props}
//   />
// ));

// const AvatarImage = React.forwardRef<
//   React.ImgHTMLAttributes<HTMLImageElement>,
//   React.ImgHTMLAttributes<HTMLImageElement>
// >(({ className, src = DEFAULT_IMAGE_URL, alt = "Avatar", ...props }, ref) => (
//   <div className={cn("relative aspect-square h-full w-full", className)}>
//     <Image
//       ref={ref}
//       src={src}
//       alt={alt}
//       layout="fill"
//       objectFit="cover"
//       className={cn("rounded-full", className)}
//       {...props}
//     />
//   </div>
// ));

const AvatarImage = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement> & {
    src?: string | StaticImageData;
    alt?: string;
    thumblinator?: boolean;
  }
>(({ className, src, alt = "Avatar", thumblinator = true, ...props }, ref) => {
  const [imageError, setImageError] = React.useState(false);

  const loader = useMemo(
    () => (isLoaderCompatible(thumblinator, src) ? imageLoader : undefined),
    [src, thumblinator],
  );

  React.useEffect(() => {
    setImageError(false);
  }, [src]);

  return (
    <div
      ref={ref}
      className={cn("relative aspect-square h-full w-full", className)}
      {...props}
    >
      {src && !imageError ? (
        <Image
          src={src}
          alt={alt}
          layout="fill"
          objectFit="cover"
          className="rounded-full"
          onError={() => setImageError(true)}
          loader={loader}
        />
      ) : (
        <div
          className={cn(
            "flex h-full w-full items-center justify-center bg-muted",
            className,
          )}
        >
          {alt}
        </div>
      )}
    </div>
  );
});
AvatarImage.displayName = "AvatarImage";

const AvatarFallback = React.forwardRef<
  React.ElementRef<typeof AvatarPrimitive.Fallback>,
  React.ComponentPropsWithoutRef<typeof AvatarPrimitive.Fallback>
>(({ className, ...props }, ref) => (
  <AvatarPrimitive.Fallback
    ref={ref}
    className={cn(
      "flex h-full w-full items-center justify-center rounded-full bg-muted",
      className,
    )}
    {...props}
  />
));
AvatarFallback.displayName = AvatarPrimitive.Fallback.displayName;

export { Avatar, AvatarImage, AvatarFallback };
