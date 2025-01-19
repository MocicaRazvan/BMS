"use client";

import {
  AdminAICreatePostSchemaTexts,
  AdminAICreatePostSchemaType,
  getAdminAICreatePostSchema,
} from "@/types/forms";
import { useNavigationGuardI18nForm } from "@/hoooks/use-navigation-guard-i18n-form";
import { Card, CardContent, CardTitle } from "@/components/ui/card";
import React, {
  Fragment,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { choseRandomItemsFromArray, choseRandomNumber, cn } from "@/lib/utils";
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Textarea } from "@/components/ui/textarea";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import { CustomEntityModel, PostBody, PostResponse } from "@/types/dto";
import useGetDiffusionImages from "@/hoooks/useGetDiffusionImages";
import { tags } from "@/lib/constants";
import { useToxicPrompt } from "@/components/forms/diffusion-images-form";
import { AiIdeasField } from "@/types/ai-ideas-types";
import { fetchWithFiles } from "@/hoooks/fetchWithFiles";
import { BaseError } from "@/types/responses";
import { WithUser } from "@/lib/user";
import { v4 as uuidv4 } from "uuid";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import UploadingProgress from "@/components/forms/uploading-progress";
import ErrorMessage from "@/components/forms/error-message";
import { Check, X } from "lucide-react";
import { motion } from "framer-motion";
import { purifyAIDescription } from "@/components/forms/title-body";

export interface AdminAIPostsCreateContentTexts {
  schemaTexts: AdminAICreatePostSchemaTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  error: string;
  toxicError: string;
  englishError: string;
  promptLabel: string;
  negativePromptLabel: string;
  promptPlaceholder: string;
  negativePromptPlaceholder: string;
  promptDescription: string;
  negativePromptDescription: string;
  descriptionLabel: string;
  descriptionPlaceholder: string;
  descriptionDescription: string;
  uploadingState: {
    title: string;
    description: string;
    images: string;
    submitted: string;
  };
  title: string;
  createdPosts: string;
  numberOfPostsLabel: string;
  singlePostLabel: string;
}

interface UploadingState {
  title: boolean;
  description: boolean;
  images: boolean;
  submitted: boolean;
}
const initialUploadingState: UploadingState = {
  title: false,
  description: false,
  images: false,
  submitted: false,
};

async function getAIIdeaResponse(
  fields: AiIdeasField[],
  input: string,
  targetedField: "title" | "description",
) {
  const res = await fetch("/api/ai-idea/json", {
    method: "POST",
    body: JSON.stringify({
      fields: fields,
      input: input.trim(),
      targetedField,
      item: "post",
      streamResponse: false,
    }),
  }).then((res) => res.json());
  if (!res?.answer || !(typeof res.answer === "string")) {
    throw new Error("No response from AI");
  }
  return res.answer as string;
}
const numberOfPostsOptions = Array.from({ length: 5 }, (_, i) => i + 1);
interface Props extends AdminAIPostsCreateContentTexts, WithUser {}
export default function AdminAIPostsCreateContent({
  schemaTexts,
  authUser,
  error,
  toxicError,
  englishError,
  createdPosts,
  uploadingState,
  promptPlaceholder,
  negativePromptPlaceholder,
  negativePromptDescription,
  negativePromptLabel,
  promptDescription,
  promptLabel,
  descriptionDescription,
  descriptionPlaceholder,
  title,
  descriptionLabel,
  buttonSubmitTexts,
  numberOfPostsLabel,
  singlePostLabel,
}: Props) {
  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();
  const [createdPostsNumber, setCreatedPostsNumber] = useState<number>(0);
  const [clientId] = useState(uuidv4);
  const [currentStatus, setCurrentStatus] = useState<UploadingState[]>([
    // initialUploadingState,
    // initialUploadingState,
    // initialUploadingState,
    // initialUploadingState,
  ]);
  const schema = useMemo(
    () => getAdminAICreatePostSchema(schemaTexts),
    [schemaTexts],
  );
  const setCurrentStatusForIndex = useCallback(
    (index: number, status: Partial<UploadingState>) => {
      setCurrentStatus((prev) => {
        const newStatus = [...prev];
        if (!newStatus.at(index)) {
          newStatus[index] = initialUploadingState;
        }
        newStatus[index] = { ...newStatus[index], ...status };
        return newStatus;
      });
    },
    [setCurrentStatus],
  );
  //realistic, chicken with salad and tomatoes
  //Make it about nutrition healthy earthing and having fun while being healthy
  const form = useForm<AdminAICreatePostSchemaType>({
    resolver: zodResolver(schema),
    defaultValues: {
      prompt: undefined,
      negativePrompt: "low quality, blurry, animated, childish",
      description: undefined,
      numImages: 1,
      images: [],
      numberOfPosts: 1,
    },
  });
  const { invalidateImages, getImages } = useGetDiffusionImages({
    cleanUpArgs: {
      fieldName: "images",
      getValues: form.getValues,
    },
  });

  useNavigationGuardI18nForm({ form });

  useEffect(() => {
    return () => {
      invalidateImages();
    };
  }, [invalidateImages]);

  const { handleToxic } = useToxicPrompt({
    toxicError,
    englishError,
    form,
  });

  const watchNumberOfPosts = form.watch("numberOfPosts");

  const onSubmit = useCallback(
    async (data: AdminAICreatePostSchemaType) => {
      setIsLoading(true);
      const [pToxic, nToxic] = await Promise.all([
        handleToxic(data.prompt, form, "prompt"),
        handleToxic(data.negativePrompt, form, "negativePrompt"),
      ]);
      if (!pToxic || !nToxic) {
        setIsLoading(false);
        return;
      }
      setCreatedPostsNumber(0);
      setCurrentStatus([]);

      for (let i = 0; i < data.numberOfPosts; i++) {
        setCurrentStatusForIndex(i, initialUploadingState);
      }
      const createOnePost = async (index: number) => {
        const randomTags = choseRandomItemsFromArray<string>(
          tags as unknown as string[],
          choseRandomNumber(0, tags.length - 1),
        );
        const tagsField: AiIdeasField = {
          content: randomTags.join(","),
          name: "tags",
          isHtml: false,
          role: "Tags of the post",
        };
        const aiTitle = await getAIIdeaResponse(
          [tagsField],
          data.description,
          "title",
        );
        setCurrentStatusForIndex(index, { title: true });
        const titleTags: AiIdeasField = {
          content: aiTitle,
          name: "title",
          isHtml: false,
          role: "Title of the post",
        };
        const [description, diffusionImage] = await Promise.all([
          getAIIdeaResponse(
            [tagsField, titleTags],
            data.description,
            "description",
          ).then((description) => {
            setCurrentStatusForIndex(index, { description: true });
            return description;
          }),
          getImages({
            prompt: data.prompt,
            negative_prompt: data.negativePrompt,
            num_images: choseRandomNumber(1, 3),
          }).then((images) => {
            setCurrentStatusForIndex(index, { images: true });
            return images;
          }),
        ]);
        const res = await fetchWithFiles<
          PostBody,
          BaseError,
          CustomEntityModel<PostResponse>
        >({
          path: "/posts/createWithImages",
          token: authUser.token,
          data: {
            files: diffusionImage.urls.map((u) => u.file),
            body: {
              body: purifyAIDescription(description),
              title: aiTitle,
              tags: randomTags,
            },
          },

          clientId,
        });
        setCurrentStatusForIndex(index, { submitted: true });
        setCreatedPostsNumber((prev) => prev + 1);
      };

      try {
        // secvential ca 1 ollama si 1 diffusion
        for (let i = 0; i < data.numberOfPosts; i++) {
          await createOnePost(i);
        }
        form.reset();
        router.push(
          "/admin/posts?sort=createdAt:desc&title=&currentPage=0&pageSize=10&approved=false",
        );
      } catch (e) {
        console.log(e);
        setIsLoading(false);
        setErrorMsg(error);
      } finally {
        setIsLoading(false);
        setCreatedPostsNumber(0);
        setCurrentStatus([]);
      }
    },
    [
      authUser.token,
      clientId,
      error,
      form,
      getImages,
      handleToxic,
      router,
      setCurrentStatusForIndex,
      setErrorMsg,
      setIsLoading,
    ],
  );

  return (
    <Card className="max-w-7xl w-full sm:px-2 md:px-5 py-6">
      <CardTitle className="font-bold text-2xl text-center capitalize">
        {title}
      </CardTitle>
      <CardContent>
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-8 lg:space-y-12 w-full h-full"
            noValidate
          >
            <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>{descriptionLabel}</FormLabel>
                  <FormControl>
                    <Textarea
                      placeholder={descriptionPlaceholder}
                      className="resize-none"
                      {...field}
                    />
                  </FormControl>
                  <FormDescription>{descriptionDescription}</FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />
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
                  <FormDescription>{negativePromptDescription}</FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="numberOfPosts"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>{numberOfPostsLabel}</FormLabel>
                  <Select
                    onValueChange={field.onChange}
                    defaultValue={`${field.value}`}
                    disabled={isLoading}
                  >
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue defaultValue={`${field.value}`} />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {numberOfPostsOptions.map((n) => (
                        <Fragment key={n + " selectnop"}>
                          <SelectItem value={`${n}`}>{n}</SelectItem>
                        </Fragment>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
            <ErrorMessage message={error} show={!!errorMsg} />
            <ButtonSubmit
              isLoading={isLoading}
              disable={isLoading}
              buttonSubmitTexts={buttonSubmitTexts}
            />
            <motion.div className="w-full h-full" layout={true}>
              {isLoading && (
                <div className="w-full h-full space-y-6">
                  <UploadingProgress
                    total={watchNumberOfPosts}
                    loaded={createdPostsNumber}
                    loadedItems={createdPosts}
                  />
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-6 px-3 md:px-10">
                    {currentStatus.map((cr, i) => (
                      <div
                        className="space-y-1"
                        key={i + "-crsmp-" + currentStatus.length}
                      >
                        <p
                          className={cn(
                            "text-lg font-semibold",
                            Object.values(cr).every((v) => v)
                              ? "text-success"
                              : "text-amber",
                          )}
                        >{`${singlePostLabel} ${i + 1}`}</p>
                        <ul className="space-y-1">
                          {Object.entries(cr).map(([key, value]) => (
                            <li
                              className="flex items-center space-x-2 "
                              key={key + "si"}
                            >
                              <StatusItem
                                status={value}
                                text={
                                  uploadingState[key as keyof UploadingState]
                                }
                              />
                            </li>
                          ))}
                        </ul>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </motion.div>
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}

interface StatusItemProps {
  status: boolean;
  text: string;
}
const StatusItem = ({ status, text }: StatusItemProps) => {
  return (
    <div className="w-full h-full">
      {status ? (
        <div className="flex items-center justify-start mx-auto flex-1 ">
          <Check className="w-6 h-6 text-success" />
          <span className=" text-success">{text}</span>
        </div>
      ) : (
        <div className="flex items-center justify-start mx-auto flex-1 ">
          <X className="w-6 h-6 text-amber" />
          <span className="text-amber">{text}</span>
        </div>
      )}
    </div>
  );
};
