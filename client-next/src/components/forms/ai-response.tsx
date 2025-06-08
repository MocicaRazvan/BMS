"use client";
import { AnimatePresence, motion } from "framer-motion";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { AIPopCallbackArg } from "@/components/forms/ai-generate-pop";
import { Dispatch, HTMLProps, ReactNode, SetStateAction, useMemo } from "react";
import removeMd from "remove-markdown";

export interface AIResponseTexts {
  useButtonText: string;
  discardButtonText: string;
  warningText: string;
}

interface AIResponseProps extends AIResponseTexts {
  response: string | undefined;
  saveCallback: (resp: AIPopCallbackArg) => void;
  setResponse: Dispatch<SetStateAction<string | undefined>>;
  presentationCallback: (r: string) => ReactNode;
  isLoading: boolean;
  wrapperClassName?: HTMLProps<HTMLDivElement>["className"];
  wrapperButtonsClassName?: HTMLProps<HTMLDivElement>["className"];
  removeMarkdown?: boolean;
}

export function AIResponse({
  response,
  saveCallback,
  setResponse,
  presentationCallback,
  isLoading,
  wrapperClassName = "",
  wrapperButtonsClassName = "",
  discardButtonText,
  useButtonText,
  removeMarkdown = false,
}: AIResponseProps) {
  const cleanedResponse = useMemo(
    () => (!response || !removeMarkdown ? response : removeMd(response)),
    [removeMarkdown, response],
  );
  return (
    <AnimatePresence>
      {cleanedResponse && cleanedResponse.trim() !== "" && (
        <motion.div
          className={cn(
            !cleanedResponse
              ? "hidden"
              : "flex items-start justify-between gap-10 mt-5 md:mt-7 " +
                  wrapperClassName,
          )}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
        >
          {presentationCallback(cleanedResponse)}

          <div
            className={
              "flex items-center justify-between gap-5 " +
              wrapperButtonsClassName
            }
          >
            <Button
              type="button"
              onClick={() => {
                saveCallback({ answer: cleanedResponse });
                setResponse(undefined);
              }}
              variant="success"
            >
              {useButtonText}
            </Button>
            {!isLoading && (
              <Button type="button" onClick={() => setResponse(undefined)}>
                {discardButtonText}
              </Button>
            )}
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
