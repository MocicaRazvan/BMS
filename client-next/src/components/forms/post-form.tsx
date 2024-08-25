"use client";

import {
  BaseFormProps,
  getPostSchema,
  PostSchemaTexts,
  PostType,
} from "@/types/forms";
import InputFile, { FieldInputTexts } from "@/components/forms/input-file";
import { TitleBodyForm, TitleBodyTexts } from "@/components/forms/title-body";
import InputMultipleSelector, {
  InputMultipleSelectorTexts,
} from "@/components/forms/input-multiselector";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import { useCallback, useEffect, useMemo } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Card, CardContent, CardTitle } from "@/components/ui/card";
import { Form } from "@/components/ui/form";
import { tagsOptions } from "@/lib/constants";
import { Session } from "next-auth";
import { CustomEntityModel, PostBody, PostResponse } from "@/types/dto";
import { fetchWithFiles } from "@/hoooks/fetchWithFiles";
import { BaseError } from "@/types/responses";
import { BaseFormTexts } from "@/texts/components/forms";
import ErrorMessage from "@/components/forms/error-message";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import { toast } from "@/components/ui/use-toast";
import { ToastAction } from "@/components/ui/toast";
import useFilesBase64 from "@/hoooks/useFilesObjectURL";
import { handleBaseError } from "@/lib/utils";

export interface PostFormProps
  extends Partial<Omit<PostType, "images">>,
    BaseFormTexts,
    BaseFormProps {
  postSchemaTexts: PostSchemaTexts;
  fieldTexts: FieldInputTexts;
  titleBodyTexts: TitleBodyTexts;
  inputMultipleSelectorTexts: InputMultipleSelectorTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  authUser: NonNullable<Session["user"]>;
  images?: string[];
}

export default function PostForm({
  postSchemaTexts,
  fieldTexts,
  titleBodyTexts,
  inputMultipleSelectorTexts,
  buttonSubmitTexts,
  header,
  body = "",
  title = "",
  images = [],
  tags = [],
  authUser,
  error,
  descriptionToast,
  toastAction,
  altToast,
  path,
  type = "create",
}: PostFormProps) {
  const schema = useMemo(
    () => getPostSchema(postSchemaTexts),
    [postSchemaTexts],
  );

  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();

  console.log("images", images);

  const form = useForm<PostType>({
    resolver: zodResolver(schema),
    defaultValues: {
      images: [],
      body,
      tags,
      title,
    },
  });
  const { fileCleanup } = useFilesBase64({
    files: images,
    fieldName: "images",
    setValue: form.setValue,
    getValues: form.getValues,
  });
  useEffect(() => {
    return () => {
      fileCleanup();
    };
  }, [fileCleanup]);

  const onSubmit = useCallback(
    async (data: PostType) => {
      setIsLoading(true);
      setErrorMsg("");
      const postBody: PostBody = {
        ...data,
        tags: data.tags.map((tag) => tag.value),
      };
      const files: File[] = data.images.map((image) => image.file);
      try {
        const res = await fetchWithFiles<
          PostBody,
          BaseError,
          CustomEntityModel<PostResponse>
        >({
          path,
          token: authUser.token,
          data: {
            files,
            body: postBody,
          },
        });
        toast({
          title: data.title,
          description: descriptionToast,
          variant: "success",
          action: (
            <ToastAction
              altText={altToast}
              onClick={() => router.push(`/posts/single/${res.content.id}`)}
            >
              {toastAction}
            </ToastAction>
          ),
        });
        router.push(`/posts/single/${res.content.id}`);
      } catch (e) {
        console.log(e);
        handleBaseError(e, setErrorMsg, error);
      } finally {
        setIsLoading(false);
      }
    },
    [
      altToast,
      authUser.token,
      descriptionToast,
      error,
      path,
      router,
      setErrorMsg,
      setIsLoading,
      toastAction,
    ],
  );

  return (
    <Card className="max-w-7xl w-full sm:px-2 md:px-5 py-6">
      <CardTitle className="font-bold text-2xl text-center capitalize">
        {header} {title && <p className="inline">{title}</p>}
      </CardTitle>
      <CardContent>
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-8 lg:space-y-12"
          >
            <TitleBodyForm<PostType>
              control={form.control}
              titleBodyTexts={titleBodyTexts}
            />
            <InputMultipleSelector
              control={form.control}
              fieldName={"tags"}
              inputMultipleSelectorTexts={inputMultipleSelectorTexts}
              options={tagsOptions}
            />
            <InputFile<PostType>
              control={form.control}
              fieldName={"images"}
              fieldTexts={fieldTexts}
              initialLength={images?.length || 0}
            />
            <ErrorMessage message={error} show={!!errorMsg} />
            <ButtonSubmit
              isLoading={isLoading}
              disable={false}
              buttonSubmitTexts={buttonSubmitTexts}
            />
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}
