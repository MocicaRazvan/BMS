"use client";

import { AIPopCallbackArg } from "@/components/forms/ai-generate-pop";
import { TitleBodyDto } from "@/types/dto";
import { Path, PathValue, UseFormReturn } from "react-hook-form";
import { SetStateAction, useCallback } from "react";
import { trimHTML, trimString } from "@/lib/utils";

export default function useBaseAICallbackTitleBody<T extends TitleBodyDto>(
  form: UseFormReturn<T>,
  setEditorKey: (value: SetStateAction<number>) => void,
) {
  return useCallback(
    (field: "title" | "body", r: AIPopCallbackArg) => {
      console.log("AIRES", r);
      if (r?.answer) {
        if (field === "title") {
          form.setValue(
            field as Path<T>,
            trimString(r.answer) as PathValue<T, Path<T>>,
          );
        } else {
          form.setValue(
            field as Path<T>,
            trimHTML(r.answer) as PathValue<T, Path<T>>,
          );
          setEditorKey((prev) => prev + 1);
        }
      }
    },
    [form, setEditorKey],
  );
}
