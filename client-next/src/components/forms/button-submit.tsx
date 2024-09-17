"use client";

import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";
import { ButtonHTMLAttributes, ReactNode } from "react";

export interface ButtonSubmitTexts {
  submitText: string | ReactNode;
  loadingText: string | ReactNode;
}

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  isLoading: boolean;
  disable: boolean;
  buttonSubmitTexts: ButtonSubmitTexts;
  size?: "default" | "sm" | "lg" | "icon" | null | undefined;
}

export default function ButtonSubmit({
  isLoading,
  disable,
  buttonSubmitTexts: { submitText, loadingText },
  size = "lg",
  ...props
}: Props) {
  return (
    <div className="mt-2">
      {!isLoading ? (
        <Button type="submit" size={size} disabled={disable} {...props}>
          {submitText}
        </Button>
      ) : (
        <Button disabled>
          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
          {loadingText}
        </Button>
      )}
    </div>
  );
}
