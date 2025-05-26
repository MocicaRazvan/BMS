"use client";

import {
  AITitleBodyForm,
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
import React, { useCallback, useEffect, useMemo, useState } from "react";
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
import useFilesObjectURL from "@/hoooks/useFilesObjectURL";
import { handleBaseError } from "@/lib/utils";
import useProgressWebSocket from "@/hoooks/useProgressWebSocket";
import { v4 as uuidv4 } from "uuid";
import UploadingProgress, {
  UploadingProgressTexts,
} from "@/components/forms/uploading-progress";
import { AiIdeasField } from "@/types/ai-ideas-types";
import useBaseAICallbackTitleBody from "@/hoooks/useBaseAICallbackTitleBody";
import useGetDiffusionImages from "@/hoooks/useGetDiffusionImages";
import DiffusionImagesForm, {
  DiffusionImagesFormCallback,
  DiffusionImagesFormTexts,
} from "@/components/forms/diffusion-images-form";
import { useNavigationGuardI18nForm } from "@/hoooks/use-navigation-guard-i18n-form";

export interface PostFormProps
  extends Partial<Omit<PostType, "images">>,
    BaseFormTexts,
    BaseFormProps,
    AITitleBodyForm {
  postSchemaTexts: PostSchemaTexts;
  fieldTexts: FieldInputTexts;
  titleBodyTexts: TitleBodyTexts;
  inputMultipleSelectorTexts: InputMultipleSelectorTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  authUser: NonNullable<Session["user"]>;
  images?: string[];
  loadedImages: UploadingProgressTexts;
  diffusionImagesFormTexts: DiffusionImagesFormTexts;
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
  loadedImages,
  titleAIGeneratedPopTexts,
  bodyAIGeneratedPopTexts,
  aiCheckBoxes,
  diffusionImagesFormTexts,
}: PostFormProps) {
  const schema = useMemo(
    () => getPostSchema(postSchemaTexts),
    [postSchemaTexts],
  );
  const [clientId] = useState(uuidv4);
  const { messages: messagesImages } = useProgressWebSocket(
    authUser.token,
    clientId,
    "IMAGE",
  );
  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();
  const [editorKey, setEditorKey] = useState<number>(0);

  const form = useForm<PostType>({
    resolver: zodResolver(schema),
    defaultValues: {
      images: [],
      body,
      tags,
      title,
    },
  });

  useNavigationGuardI18nForm({ form });

  const [isImagesListCollapsed, setIsImagesListCollapsed] = useState(true);
  const watchImages = form.watch("images");
  const watchTitle = form.watch("title");
  const watchBody = form.watch("body");
  const watchTags = form.watch("tags");

  const baseAICallback = useBaseAICallbackTitleBody(form, setEditorKey);

  const aiFields: AiIdeasField[] = useMemo(() => {
    const fields: AiIdeasField[] = [];
    if (watchTitle.trim()) {
      fields.push({
        content: watchTitle,
        name: "title",
        isHtml: false,
        role: "Title of the post",
      });
    }
    if (watchBody.trim()) {
      fields.push({
        content: watchBody,
        name: "body",
        isHtml: true,
        role: "Description of the of the post",
      });
    }
    if (watchTags.length) {
      fields.push({
        content: watchTags.map((tag) => tag.value).join(","),
        name: "tags",
        isHtml: false,
        role: "Tags of the post",
      });
    }
    return fields;
  }, [watchBody, watchTags, watchTitle]);
  const { fileCleanup, chunkProgressValue } = useFilesObjectURL({
    files: images,
    fieldName: "images",
    setValue: form.setValue,
    getValues: form.getValues,
  });

  const isSubmitDisabled = useMemo(
    () => chunkProgressValue < 100 && images.length > 0,
    [chunkProgressValue, images.length],
  );

  const { invalidateImages, getImages, addImagesCallback } =
    useGetDiffusionImages({
      cleanUpArgs: {
        fieldName: "images",
        getValues: form.getValues,
      },
    });

  const addDiffusionImages: DiffusionImagesFormCallback = useCallback(
    async ({ numImages, prompt, negativePrompt }) => {
      await getImages({
        num_images: numImages,
        prompt,
        negative_prompt: negativePrompt,
        successCallback: (images) => addImagesCallback(images, form),
      })
        .catch((e) => {
          console.log(e);
        })
        .then(() => {
          setTimeout(() => {
            setIsImagesListCollapsed(false);
          }, 400);
        });
    },
    [getImages, addImagesCallback, form],
  );

  useEffect(() => {
    return () => {
      invalidateImages();
    };
  }, [invalidateImages]);

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
          clientId,
        });
        toast({
          title: data.title,
          description: descriptionToast,
          variant: "success",
        });
        router.push(`/trainer/posts/single/${res.content.id}`);
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
      clientId,
    ],
  );

  return (
    <Card className="max-w-7xl w-full sm:px-2 md:px-5 py-6">
      <CardTitle className="font-bold text-2xl text-center capitalize mb-3.5">
        {header} {title && <p className="inline">{title}</p>}
      </CardTitle>
      <CardContent>
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-8 lg:space-y-12"
            noValidate
          >
            <TitleBodyForm<PostType>
              control={form.control}
              titleBodyTexts={titleBodyTexts}
              showAIPopDescription={true}
              showAIPopTitle={true}
              aiFields={aiFields}
              editorKey={editorKey}
              aiDescriptionCallBack={(r) => baseAICallback("body", r)}
              aiTitleCallBack={(r) => baseAICallback("title", r)}
              aiItem={"post"}
              aiCheckBoxes={aiCheckBoxes}
              titleAIGeneratedPopTexts={titleAIGeneratedPopTexts}
              bodyAIGeneratedPopTexts={bodyAIGeneratedPopTexts}
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
              parentListCollapsed={isImagesListCollapsed}
              loadingProgress={chunkProgressValue}
            />
            <DiffusionImagesForm
              texts={diffusionImagesFormTexts}
              callback={addDiffusionImages}
            />
            <ErrorMessage message={error} show={!!errorMsg} />
            <ButtonSubmit
              isLoading={isLoading}
              disable={isSubmitDisabled}
              buttonSubmitTexts={buttonSubmitTexts}
            />
            {isLoading && (
              <UploadingProgress
                total={watchImages.length}
                loaded={messagesImages.length}
                {...loadedImages}
              />
            )}
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}
