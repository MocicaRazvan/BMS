"use client";

import { Button, ButtonProps } from "@/components/ui/button";
import { Loader2 } from "lucide-react";
import { ButtonHTMLAttributes, forwardRef, ReactNode } from "react";
import { cn } from "@/lib/utils";

export interface ButtonSubmitTexts {
  submitText: string | ReactNode;
  loadingText: string | ReactNode;
}

type Props = ButtonHTMLAttributes<HTMLButtonElement> &
  Omit<ButtonProps, "size"> & {
    isLoading: boolean;
    disable: boolean;
    buttonSubmitTexts: ButtonSubmitTexts;
    size?: "default" | "sm" | "lg" | "icon" | null | undefined;
  };

const ButtonSubmit = forwardRef<HTMLDivElement, Props>(
  (
    {
      isLoading,
      disable,
      buttonSubmitTexts: { submitText, loadingText },
      size = "lg",
      ...props
    },
    ref,
  ) => {
    return (
      <div className="mt-2" ref={ref}>
        {!isLoading ? (
          <Button
            type="submit"
            size={size}
            disabled={disable}
            {...props}
            className={cn(size === "lg" && "text-lg tracking-tight")}
          >
            {submitText}
          </Button>
        ) : (
          <Button disabled size={size} className="cursor-wait">
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            {loadingText}
          </Button>
        )}
      </div>
    );
  },
);
ButtonSubmit.displayName = "ButtonSubmit";

export default ButtonSubmit;
