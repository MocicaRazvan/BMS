"use client";

import { HTMLAttributes, PropsWithChildren } from "react";
import { cn } from "@/lib/utils";

interface Props extends PropsWithChildren<HTMLAttributes<HTMLDivElement>> {}

export default function PageContainer({ children, className, ...rest }: Props) {
  return (
    <section
      // initial={{ opacity: 0 }}
      // animate={{ opacity: 1 }}
      // transition={{ duration: 0.3, ease: "easeInOut" }}
      className={cn(
        "w-full mx-auto max-w-[1500px] min-h-[calc(100vh-4rem)] flex-col items-center justify-center px-1 md:px-6 py-10 relative",
        className,
      )}
      {...rest}
    >
      {children}
    </section>
  );
}
