"use client";

import React, { useCallback, useMemo, useState } from "react";
import {
  AnswerFromBodySchemaTexts,
  AnswerFromBodySchemaType,
  getAnswerFromBodySchema,
} from "@/types/forms";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { getToxicity } from "@/actions/toxcity";
import DOMPurify from "dompurify";
import { getAnswerFromBody } from "@/actions/texts/answer-from-body";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { cn, wrapItemToString } from "@/lib/utils";
import ErrorMessage from "@/components/forms/error-message";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { v4 as uuidv4 } from "uuid";
import { AnimatePresence, motion } from "framer-motion";

const kOptions = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10] as const;

export interface AnswerFromBodyFormTexts {
  schemaTexts: AnswerFromBodySchemaTexts;
  toxicError: string;
  englishError: string;
  answerError: string;
  questionLabel: string;
  kLabel: string;
  questionPlaceholder: string;
  kPlaceholder: string;
  buttonSubmitTexts: ButtonSubmitTexts;
  triggerText: string;
  disclaimerText: string;
  matchScore: string;
}
export interface AnswerFromBodyFormProps {
  texts: AnswerFromBodyFormTexts;
  body: string;
}
type AnswersType = NonNullable<Awaited<ReturnType<typeof getAnswerFromBody>>>;
export default function AnswerFromBodyForm({
  texts: {
    schemaTexts,
    toxicError,
    englishError,
    answerError,
    questionPlaceholder,
    questionLabel,
    kLabel,
    kPlaceholder,
    buttonSubmitTexts,
    triggerText,
    disclaimerText,
    matchScore,
  },

  body,
}: AnswerFromBodyFormProps) {
  const uniqueItemKey = useMemo(() => uuidv4(), []);
  const { isLoading, setIsLoading, errorMsg, setErrorMsg } =
    useLoadingErrorState();
  const [answers, setAnswers] = useState<AnswersType>([]);
  const answerFromBodySchema = useMemo(
    () => getAnswerFromBodySchema(schemaTexts),
    [schemaTexts],
  );
  const form = useForm<AnswerFromBodySchemaType>({
    resolver: zodResolver(answerFromBodySchema),
    defaultValues: {
      question: "",
      k: 2,
    },
  });

  const onSubmit = useCallback(
    async ({ k, question }: AnswerFromBodySchemaType) => {
      setAnswers([]);
      setIsLoading(true);
      const trimmedQuestion = question.trim().replace(/\s+/g, " ");
      const toxicResp = await getToxicity(
        DOMPurify.sanitize(trimmedQuestion, {
          ALLOWED_TAGS: [],
          ALLOWED_ATTR: [],
        }),
      );
      if (toxicResp.failure) {
        setIsLoading(false);
        if (toxicResp.reason.toLowerCase() === "toxicity") {
          form.setError("question", { message: toxicError });
        } else {
          form.setError("question", { message: englishError });
        }
        return;
      }

      const answers = await getAnswerFromBody(body, trimmedQuestion, k);
      if (!answers) {
        setIsLoading(false);
        setErrorMsg(answerError);
        return;
      }
      setAnswers(answers);
      setIsLoading(false);
    },
    [
      answerError,
      body,
      englishError,
      form,
      setErrorMsg,
      setIsLoading,
      toxicError,
    ],
  );

  return (
    <div className="w-full h-full">
      <Accordion type="single" collapsible className="w-full">
        <AccordionItem value={uniqueItemKey + "item-1"}>
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
            <p className="text-center text-amber text-lg font-semibold">
              {disclaimerText}
            </p>
            <Form {...form}>
              <form
                onSubmit={form.handleSubmit(onSubmit)}
                className="space-y-8 lg:space-y-12"
                noValidate
              >
                <FormField
                  control={form.control}
                  name="question"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>{questionLabel}</FormLabel>
                      <FormControl>
                        <Textarea
                          placeholder={questionPlaceholder}
                          className="resize-none"
                          {...field}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="k"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>{kLabel}</FormLabel>
                      <Select
                        onValueChange={field.onChange}
                        defaultValue={wrapItemToString(field.value)}
                      >
                        <FormControl>
                          <SelectTrigger className="w-full max-w-60">
                            <SelectValue placeholder={kPlaceholder} />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {kOptions.map((k) => (
                            <SelectItem
                              value={wrapItemToString(k)}
                              key={uniqueItemKey + k}
                            >
                              {k}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>

                      <FormMessage />
                    </FormItem>
                  )}
                />
                <ErrorMessage message={answerError} show={!!errorMsg} />
                <ButtonSubmit
                  isLoading={isLoading}
                  disable={false}
                  buttonSubmitTexts={buttonSubmitTexts}
                />
              </form>
            </Form>
            <div className="w-full mt-3 md:mt-4 py-5 md:py-10">
              <motion.div
                className="grid grid-cols-1 md:grid-cols-2 w-full  mx-auto p-1 gap-5 md:gap-8"
                layout={true}
                initial={{ opacity: 0 }}
                animate={{ opacity: answers.length > 0 ? 1 : 0 }}
                transition={{ duration: 0.3 }}
              >
                <AnimatePresence>
                  {answers.map((item, i) => (
                    <motion.div
                      className="h-full"
                      key={uniqueItemKey + i}
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: -10 }}
                      transition={{ duration: 0.3 }}
                    >
                      <AnswerItemCard item={item} matchScore={matchScore} />
                    </motion.div>
                  ))}
                </AnimatePresence>
              </motion.div>
            </div>
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </div>
  );
}
interface AnswerItemProps {
  item: AnswersType[number];
  matchScore: string;
}
function AnswerItemCard({
  item: { content, score },
  matchScore,
}: AnswerItemProps) {
  const scoreColor =
    score > 0.5 ? "bg-success" : score > 0.3 ? "bg-amber" : "bg-error";
  const fixedValue = Math.round(score * 100);
  return (
    <Card className="w-full h-full flex flex-col items-center justify-between gap-2 shadow-sm shadow-shadow_color">
      <CardHeader className="pb-2 w-full">
        <CardTitle className="text-lg font-medium">{content}</CardTitle>
      </CardHeader>
      <CardContent className="w-full">
        <div className="flex items-center justify-between mb-1">
          <span className="text-sm font-medium text-muted-foreground">
            {matchScore}
          </span>
          <span className="text-sm font-medium">{`${fixedValue}%`}</span>
        </div>
        <Progress
          value={fixedValue}
          className="h-2"
          indicatorClassName={`${scoreColor}`}
        />
      </CardContent>
    </Card>
  );
}
