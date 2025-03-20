"use client";

import { zodResolver } from "@hookform/resolvers/zod";

import { useCallback, useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";

import DOMPurify from "dompurify";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import {
  CommentSchemaType,
  getCommentSchema,
  TitleBodyType,
} from "@/types/forms";
import { CommentFormTexts } from "@/texts/components/forms";
import ErrorMessage from "@/components/forms/error-message";
import ButtonSubmit from "@/components/forms/button-submit";
import { getToxicity } from "@/actions/toxcity";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import Editor, { EditorTexts } from "@/components/editor/editor";

export interface CommentAccordionTexts {
  commentFormTexts: CommentFormTexts;
  englishError: string;
  toxicError: string;
  englishHeading: string;
  editorTexts: EditorTexts;
}

interface Props extends CommentAccordionTexts {
  title?: string;
  body?: string;
  postId: number;
  token: string;
  refetch: () => void;
}

export default function CommentAccordion({
  title = "",
  body = "",
  postId,
  token,
  refetch,
  commentFormTexts: {
    commentSchemaTexts,
    buttonSubmitTexts,
    error,
    descriptionToast,
    toastAction,
    altToast,
    titleBodyTexts,
    baseFormTextsUpdate,
    header,
  },
  englishHeading,
  englishError,
  toxicError,
  editorTexts,
}: Props) {
  const [value, setValue] = useState("");
  const schema = useMemo(
    () => getCommentSchema(commentSchemaTexts),
    [commentSchemaTexts],
  );

  const form = useForm<CommentSchemaType>({
    resolver: zodResolver(schema),
    defaultValues: {
      body,
      title: "NONE",
    },
  });
  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();

  const handleToxicResp = useCallback(
    (
      resp: Awaited<ReturnType<typeof getToxicity>>,
      key: keyof TitleBodyType & string,
    ) => {
      if (!resp.failure) {
        return;
      }
      if (resp.reason.toLowerCase() === "toxicity") {
        form.setError(key, {
          message: toxicError,
        });
      } else {
        form.setError(key, {
          message: englishError,
        });
      }
    },
    [englishError, form, toxicError],
  );

  const onSubmit = useCallback(
    async (body: CommentSchemaType) => {
      if (!token) return;
      setIsLoading(true);
      const trimmedBody = body.body.trim();
      const [
        // titleRes,
        bodyRes,
      ] = await Promise.all([
        // getToxicity(
        //   DOMPurify.sanitize(body.title, {
        //     ALLOWED_TAGS: [],
        //     ALLOWED_ATTR: [],
        //   }),
        // ),
        getToxicity(
          DOMPurify.sanitize(trimmedBody, {
            ALLOWED_TAGS: [],
            ALLOWED_ATTR: [],
          }),
        ),
      ]);
      if (
        // titleRes.failure ||
        bodyRes.failure
      ) {
        // handleToxicResp(titleRes, "title");
        handleToxicResp(bodyRes, "body");
        setIsLoading(false);
        return;
      }

      try {
        const {
          messages,
          error: fError,
          isFinished,
        } = await fetchStream({
          path: `/comments/create/post/${postId}`,
          method: "POST",
          body: {
            ...body,
            body: trimmedBody,
          },
          token,
        });
        console.log("error", error);
        if (fError) {
          if (fError.message) {
            setErrorMsg(fError.message);
          }
          setErrorMsg(error);
          setIsLoading(false);
        } else {
          refetch();
          // form.reset();
          // setValue("");
          setTimeout(() => {
            setIsLoading(false);
            form.reset();
            setValue("");
          }, 450);
        }
      } catch (e) {
        console.log(e);
        setIsLoading(false);
      } finally {
        // setIsLoading(false);
      }
    },
    [
      error,
      form,
      handleToxicResp,
      postId,
      refetch,
      setErrorMsg,
      setIsLoading,
      token,
    ],
  );

  return (
    <Accordion
      type="single"
      collapsible
      className="w-full"
      value={value}
      onValueChange={setValue}
    >
      <AccordionItem value="item-1">
        <AccordionTrigger>{header}</AccordionTrigger>
        <AccordionContent className=" w-full flex items-center justify-center mx-auto ">
          <Form {...form}>
            <form
              onSubmit={form.handleSubmit(onSubmit)}
              className="space-y-8 w-full px-10 pt-1 mb-4"
            >
              <div>
                <h2 className="text-xl font-bold tracking-tighter mt-2">
                  {englishHeading}
                </h2>
                {/*<TitleBodyForm<TitleBodyType>*/}
                {/*  control={form.control}*/}
                {/*  titleBodyTexts={titleBodyTexts}*/}
                {/*  hideTitle={true}*/}
                {/*/>*/}

                <FormField
                  control={form.control}
                  name={"body"}
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="capitalize sr-only">
                        {body}
                      </FormLabel>
                      <FormControl>
                        <Editor
                          descritpion={field.value as string}
                          onChange={field.onChange}
                          placeholder={titleBodyTexts.bodyPlaceholder}
                          texts={editorTexts}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <ErrorMessage message={error} show={!!errorMsg} />
              <ButtonSubmit
                isLoading={isLoading}
                disable={false}
                buttonSubmitTexts={buttonSubmitTexts}
              />
            </form>
          </Form>
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  );
}
