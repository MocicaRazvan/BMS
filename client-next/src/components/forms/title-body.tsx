"use client";
import { Control, Path, useFormContext } from "react-hook-form";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import Editor from "../editor/editor";
import { TitleBodyDto } from "@/types/dto";
import { AiIdeasField } from "@/types/ai-ideas-types";
import AIGeneratePop, {
  AIGeneratePopTexts,
  AIPopCallback,
  AIPopCallbackArg,
} from "@/components/forms/ai-generate-pop";
import {
  Dispatch,
  HTMLProps,
  ReactNode,
  SetStateAction,
  useMemo,
  useState,
} from "react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { motion, AnimatePresence } from "framer-motion";
import * as DOMPurify from "dompurify";
import removeMd from "remove-markdown";

export interface TitleBodyTexts {
  title: string;
  body: string;
  titlePlaceholder?: string;
  bodyPlaceholder?: string;
  aiResponseTexts: AIResponseTexts;
}

interface CustomFieldProps<TFieldValues extends TitleBodyDto> {
  control: Control<TFieldValues>;
  titleBodyTexts: TitleBodyTexts;
  hideTitle?: boolean;
  editorKey?: number;
  showAIPopTitle?: boolean;
  showAIPopDescription?: boolean;
  aiFields?: AiIdeasField[];
  aiTitleCallBack?: AIPopCallback;
  aiDescriptionCallBack?: AIPopCallback;
  aiItem?: string;
  aiCheckBoxes?: Record<string, string>;
  titleAIGeneratedPopTexts?: AIGeneratePopTexts;
  bodyAIGeneratedPopTexts?: AIGeneratePopTexts;
  extraBodyContext?: number;
}

export const TitleBodyForm = <TFieldValues extends TitleBodyDto>({
  control,
  titleBodyTexts: {
    body,
    bodyPlaceholder,
    titlePlaceholder,
    title,
    aiResponseTexts,
  },
  hideTitle = false,
  editorKey,
  showAIPopTitle = false,
  showAIPopDescription = false,
  aiFields = [],
  aiTitleCallBack = (r) => {},
  aiDescriptionCallBack = (r) => {},
  aiItem = "",
  aiCheckBoxes = {},
  titleAIGeneratedPopTexts,
  bodyAIGeneratedPopTexts,
  extraBodyContext = 5,
}: CustomFieldProps<TFieldValues>) => {
  const { getValues } = useFormContext<TFieldValues>();
  const {
    response: titleAI,
    setResponse: setTitleAI,
    isLoading: titleIsLoading,
    setIsLoading: setTitleIsLoading,
  } = useAIGenerateResponse();
  const {
    response: bodyAI,
    setResponse: setBodyAI,
    isLoading: bodyIsLoading,
    setIsLoading: setBodyIsLoading,
  } = useAIGenerateResponse();

  if (!["title", "body"].every((k) => k in getValues())) {
    throw new Error(`Invalid field names`);
  }
  const titleAIFields = useMemo(
    () =>
      // .filter((f) => f.name !== "title"),
      aiFields,
    [aiFields],
  );
  const bodyAIFields = useMemo(
    () =>
      // .filter((f) => f.name !== "description"),
      aiFields,
    [aiFields],
  );

  return (
    <div className="space-y-8 lg:space-y-12">
      {!hideTitle && (
        <FormField
          control={control}
          name={"title" as Path<TFieldValues>}
          render={({ field }) => (
            <FormItem>
              <FormLabel className="capitalize">{title}</FormLabel>
              <FormControl>
                <div className="w-full h-full">
                  <div className="flex items-center justify-center">
                    <Input
                      placeholder={titlePlaceholder}
                      {...field}
                      className="flex-1"
                    />

                    {showAIPopTitle && titleAIGeneratedPopTexts && (
                      <div className="ml-2 md:ml-4">
                        <AIGeneratePop
                          fields={titleAIFields}
                          finishCallback={aiTitleCallBack}
                          targetedField={"title"}
                          item={aiItem}
                          checkBoxes={aiCheckBoxes}
                          {...titleAIGeneratedPopTexts}
                          updateCallback={setTitleAI}
                          updateDelayMs={100}
                          loadingCallback={setTitleIsLoading}
                        />
                      </div>
                    )}
                  </div>
                  <AIResponse
                    response={titleAI}
                    saveCallback={aiTitleCallBack}
                    setResponse={setTitleAI}
                    {...aiResponseTexts}
                    presentationCallback={(r) => (
                      <div className="w-full h-full space-y-2">
                        <Input
                          value={removeMd(r)}
                          disabled={true}
                          className="flex-1 disabled:cursor-default disabled:bg-background disabled:opacity-100"
                        />
                        <p className="text-sm text-amber">
                          {aiResponseTexts.warningText}
                        </p>
                      </div>
                    )}
                    isLoading={titleIsLoading}
                  />
                </div>
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
      )}
      <FormField
        control={control}
        name={"body" as Path<TFieldValues>}
        render={({ field }) => (
          <FormItem>
            <div className="flex items-center justify-between">
              <FormLabel className="capitalize">{body}</FormLabel>
              {showAIPopDescription && bodyAIGeneratedPopTexts && (
                <AIGeneratePop
                  fields={bodyAIFields}
                  finishCallback={aiDescriptionCallBack}
                  targetedField={"description"}
                  item={aiItem}
                  checkBoxes={aiCheckBoxes}
                  extraContext={extraBodyContext}
                  {...bodyAIGeneratedPopTexts}
                  updateCallback={setBodyAI}
                  updateDelayMs={2100}
                  loadingCallback={setBodyIsLoading}
                />
              )}
            </div>
            <FormControl>
              <div className="h-full w-full space-y-1">
                <AIResponse
                  response={bodyAI}
                  saveCallback={aiDescriptionCallBack}
                  setResponse={setBodyAI}
                  isLoading={bodyIsLoading}
                  {...aiResponseTexts}
                  presentationCallback={(r) => (
                    <div className="w-full h-full space-y-2 mb-16">
                      <p className="text-sm dark:text-amber-500 text-amber-400">
                        {aiResponseTexts.warningText}
                      </p>
                      <div
                        className="p-2  prose max-w-none [&_ol]:list-decimal [&_ul]:list-disc dark:prose-invert rounded-md border min-h-[140px] border-input bg-background ring-offset-2"
                        dangerouslySetInnerHTML={{
                          __html: purifyAIDescription(r),
                        }}
                      />
                    </div>
                  )}
                  wrapperClassName="flex-col-reverse"
                  wrapperButtonsClassName="md:justify-end gap-10 w-full mt-2"
                />
                <Editor
                  descritpion={field.value as string}
                  onChange={field.onChange}
                  placeholder={bodyPlaceholder}
                  key={editorKey}
                />
              </div>
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />
    </div>
  );
};

export const purifyAIDescription = (descriptions: string) =>
  DOMPurify.sanitize(descriptions, {
    FORBID_ATTR: ["style", "id", "class"],
    FORBID_TAGS: [
      "style",
      "script",
      "html",
      "body",
      "image",
      "head",
      "meta",
      "svg",
    ],
  });

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
}

function AIResponse({
  response,
  saveCallback,
  setResponse,
  presentationCallback,
  isLoading,
  wrapperClassName = "",
  wrapperButtonsClassName = "",
  discardButtonText,
  useButtonText,
}: AIResponseProps) {
  return (
    <AnimatePresence>
      {response && response.trim() !== "" && (
        <motion.div
          className={cn(
            !response
              ? "hidden"
              : "flex items-start justify-between gap-10 mt-5 md:mt-7 " +
                  wrapperClassName,
          )}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
        >
          {presentationCallback(response)}

          <div
            className={
              "flex items-center justify-between gap-5 " +
              wrapperButtonsClassName
            }
          >
            <Button
              type={"button"}
              onClick={() => {
                saveCallback({ answer: response });
                setResponse(undefined);
              }}
              variant={"success"}
            >
              {useButtonText}
            </Button>
            {!isLoading && (
              <Button type={"button"} onClick={() => setResponse(undefined)}>
                {discardButtonText}
              </Button>
            )}
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

function useAIGenerateResponse() {
  const [response, setResponse] = useState<string | undefined>();
  const [isLoading, setIsLoading] = useState(false);

  return { response, setResponse, isLoading, setIsLoading };
}
