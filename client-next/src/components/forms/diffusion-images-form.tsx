"use client";

import {
  DiffusionSchemaTexts,
  DiffusionSchemaType,
  getDiffusionSchema,
} from "@/types/forms";
import { useCallback, useMemo, useState } from "react";
import { Path, useForm, UseFormReturn } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { getToxicity } from "@/actions/forms/toxcity-action";
import DOMPurify from "dompurify";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import { cn } from "@/lib/utils";

export type DiffusionImagesFormCallback = (
  images: DiffusionSchemaType,
) => Promise<void>;
export interface DiffusionImagesFormTexts {
  schemaTexts: DiffusionSchemaTexts;
  toxicError: string;
  englishError: string;
  triggerText: string;
  promptLabel: string;
  negativePromptLabel: string;
  numImagesLabel: string;
  promptPlaceholder: string;
  negativePromptPlaceholder: string;
  promptDescription: string;
  negativePromptDescription: string;
  buttonSubmitText: ButtonSubmitTexts;
}
interface Props {
  texts: DiffusionImagesFormTexts;
  callback: DiffusionImagesFormCallback;
}
export default function DiffusionImagesForm({
  texts: {
    schemaTexts,
    negativePromptDescription,
    negativePromptPlaceholder,
    negativePromptLabel,
    promptDescription,
    promptPlaceholder,
    triggerText,
    promptLabel,
    numImagesLabel,
    buttonSubmitText,
    toxicError,
    englishError,
  },
  callback,
}: Props) {
  const [loading, setLoading] = useState<boolean>(false);
  const [accOpen, setAccOpen] = useState<string>("");
  const diffusionSchema = useMemo(
    () => getDiffusionSchema(schemaTexts),
    [schemaTexts],
  );
  const form = useForm<DiffusionSchemaType>({
    resolver: zodResolver(diffusionSchema),
    defaultValues: {
      prompt: "",
      negativePrompt: "low quality, blurry, animated",
      numImages: 1,
    },
  });

  // useNavigationGuardI18nForm({ form });

  const { handleToxic } = useToxicPrompt({ toxicError, englishError, form });

  const onSubmit = useCallback(
    async (data: DiffusionSchemaType) => {
      setLoading(true);
      const [pToxic, nToxic] = await Promise.all([
        handleToxic(data.prompt, form, "prompt"),
        handleToxic(data.negativePrompt, form, "negativePrompt"),
      ]);
      if (!pToxic || !nToxic) {
        setLoading(false);
        return;
      }
      callback({
        prompt: data.prompt.trim(),
        negativePrompt: data.negativePrompt.trim(),
        numImages: data.numImages,
      })
        .then(() => {
          setLoading(false);
          setAccOpen("");
        })
        .finally(() => {
          setLoading(false);
        });
    },
    [callback, form, handleToxic],
  );
  return (
    <div className="w-full h-full ">
      <Accordion
        type={"single"}
        collapsible
        className="w-full"
        value={accOpen}
        onValueChange={setAccOpen}
      >
        <AccordionItem value="item-1">
          <AccordionTrigger className="text-lg font-semibold">
            <p
              className={cn(
                "bg-gradient-to-r from-blue-400 via-purple-400-500 to-pink-300",
                "text-transparent bg-clip-text text-lg font-semibold",
              )}
            >
              {triggerText}
            </p>
          </AccordionTrigger>
          <AccordionContent className="p-5 md:p-10">
            <Form {...form}>
              <div className="w-full h-full space-y-8 lg:space-y-10">
                <FormField
                  control={form.control}
                  name="prompt"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>{promptLabel}</FormLabel>
                      <FormControl>
                        <Textarea
                          placeholder={promptPlaceholder}
                          className="resize-none"
                          {...field}
                        />
                      </FormControl>
                      <FormDescription>{promptDescription}</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="negativePrompt"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>{negativePromptLabel}</FormLabel>
                      <FormControl>
                        <Textarea
                          placeholder={negativePromptPlaceholder}
                          className="resize-none"
                          {...field}
                        />
                      </FormControl>
                      <FormDescription>
                        {negativePromptDescription}
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="numImages"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>{numImagesLabel}</FormLabel>
                      <Select
                        onValueChange={field.onChange}
                        defaultValue={`${field.value}`}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue defaultValue={`${field.value}`} />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value={"1"}>{"1"}</SelectItem>
                          <SelectItem value={"2"}>{"2"}</SelectItem>
                          <SelectItem value={"3"}>{"3"}</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <ButtonSubmit
                  type="button"
                  onClick={form.handleSubmit(onSubmit)}
                  isLoading={loading}
                  disable={loading}
                  buttonSubmitTexts={buttonSubmitText}
                />
              </div>
            </Form>
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </div>
  );
}

interface UseToxicPromptArgs<
  T extends {
    prompt: string;
    negativePrompt: string;
  },
> {
  toxicError: string;
  englishError: string;
  form: UseFormReturn<T>;
}
export function useToxicPrompt<
  T extends {
    prompt: string;
    negativePrompt: string;
  },
>({ toxicError, englishError, form }: UseToxicPromptArgs<T>) {
  const handleToxic = useCallback(
    async (
      input: string,
      f: typeof form,
      field: "prompt" | "negativePrompt",
    ) => {
      const trimmedInput = input.trim().replace(/\s+/g, " ");
      const toxicResp = await getToxicity(
        DOMPurify.sanitize(trimmedInput, {
          ALLOWED_TAGS: [],
          ALLOWED_ATTR: [],
        }),
      );
      if (toxicResp.failure) {
        if (toxicResp.reason.toLowerCase() === "toxicity") {
          f.setError(field as Path<T>, { message: toxicError });
        } else {
          f.setError(field as Path<T>, { message: englishError });
        }
        return false;
      }
      return true;
    },
    [englishError, toxicError],
  );

  return { handleToxic };
}
