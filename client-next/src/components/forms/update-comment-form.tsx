"use client";

import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import ErrorMessage from "@/components/forms/error-message";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import Editor, { EditorTexts } from "@/components/editor/editor";
import { useCallback, useMemo } from "react";
import {
  CommentSchemaTexts,
  CommentSchemaType,
  getCommentSchema,
} from "@/types/forms";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { TitleBodyTexts } from "@/components/forms/title-body";
import { CommentResponse, CustomEntityModel } from "@/types/dto";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import { cleanText } from "@/lib/utils";
import { getToxicity } from "@/actions/toxcity";
import DOMPurify from "dompurify";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { WithUser } from "@/lib/user";

interface Props extends WithUser {
  titleBodyTexts: TitleBodyTexts;
  commentSchemaTexts: CommentSchemaTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  editorTexts: EditorTexts;
  content: CommentResponse;
  edited: string;
  editHeader: string;
  deleteCommentDialog: string;
  editCommentLabel: string;
  updateCallback: (
    id: number,
    data: CommentSchemaType,
    updatedAt: string,
  ) => void;
  englishError: string;
  toxicError: string;
  errorText: string;
  setEditMode: (mode: string) => void;
}

export default function UpdateCommentForm({
  titleBodyTexts,
  buttonSubmitTexts,
  editorTexts,
  commentSchemaTexts,
  content,
  edited,
  editHeader,
  deleteCommentDialog,
  editCommentLabel,
  updateCallback,
  englishError,
  toxicError,
  errorText,
  authUser,
  setEditMode,
}: Props) {
  const schema = useMemo(
    () => getCommentSchema(commentSchemaTexts),
    [commentSchemaTexts],
  );
  const form = useForm<CommentSchemaType>({
    resolver: zodResolver(schema),
    defaultValues: {
      body: content.body,
      title: content.title,
    },
  });

  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();

  const watchBody = form.watch("body");
  const isSameBody = useMemo(() => {
    return cleanText(watchBody) === cleanText(content.body);
  }, [watchBody, content.body]);

  const onSubmit = useCallback(
    async (data: CommentSchemaType) => {
      setIsLoading(true);
      const trimmedBody = data.body.trim();
      const toxicRes = await getToxicity(
        DOMPurify.sanitize(trimmedBody, {
          ALLOWED_TAGS: [],
          ALLOWED_ATTR: [],
        }),
      );
      if (toxicRes.failure) {
        if (toxicRes.reason.toLowerCase() === "toxicity") {
          form.setError("body", {
            message: toxicError,
          });
        } else {
          form.setError("body", {
            message: englishError,
          });
        }
        setIsLoading(false);

        return;
      }
      try {
        const { messages, error, isFinished } = await fetchStream<
          CustomEntityModel<CommentResponse>
        >({
          path: `/comments/update/${content.id}`,
          method: "PUT",
          body: {
            ...data,
            body: trimmedBody,
          },
          token: authUser.token,
        });
        if (error) {
          if (error.message) {
            setErrorMsg(error.message);
          }
          setErrorMsg(errorText);
        } else {
          if (messages[0]) {
            updateCallback(
              content.id,
              {
                title: messages[0].content.title,
                body: messages[0].content.body,
              },
              messages[0].content.updatedAt,
            );
            console.log("messages", messages);
            setEditMode("");
          }
        }
      } catch (error) {
        console.log(error);
      } finally {
        setIsLoading(false);
      }
    },
    [
      authUser.token,
      content.id,
      errorText,
      setErrorMsg,
      setIsLoading,
      updateCallback,
    ],
  );

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit(onSubmit)}
        className="space-y-8 w-full px-10 pt-1 lg:space-y-12"
      >
        <FormField
          control={form.control}
          name={"body"}
          render={({ field }) => (
            <FormItem className="space-y-0">
              <FormLabel>{editCommentLabel}</FormLabel>
              <FormControl>
                <Editor
                  descritpion={field.value as string}
                  onChange={field.onChange}
                  placeholder={titleBodyTexts.bodyPlaceholder}
                  texts={editorTexts}
                  separatorClassname="h-6"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <ErrorMessage message={errorText} show={!!errorMsg} />
        <ButtonSubmit
          isLoading={isLoading}
          disable={isLoading || isSameBody}
          buttonSubmitTexts={buttonSubmitTexts}
        />
      </form>
    </Form>
  );
}
