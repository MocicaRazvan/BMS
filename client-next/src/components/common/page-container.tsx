import { HTMLAttributes, PropsWithChildren } from "react";
import { cn } from "@/lib/utils";

interface Props extends PropsWithChildren<HTMLAttributes<HTMLDivElement>> {}

export default function PageContainer({ children, className, ...rest }: Props) {
  return (
    <section
      className={cn(
        "w-full mx-auto max-w-[1500px] min-h-[calc(100vh-4rem)] flex-col items-center justify-center px-2 md:px-6 py-10 relative",
        className,
      )}
      {...rest}
    >
      {children}
    </section>
  );
}
