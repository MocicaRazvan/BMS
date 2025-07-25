"use client";
import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Option } from "@/components/ui/multiple-selector";
import {
  CustomEntityModel,
  DietType,
  IngredientNutritionalFactResponse,
  PageableResponse,
  RecipeBody,
  RecipeResponse,
} from "@/types/dto";
import { WithUser } from "@/lib/user";
import { AnimatePresence, motion } from "framer-motion";
import ChildInputMultipleSelector, {
  ChildInputMultipleSelectorTexts,
} from "@/components/forms/child-input-multipleselector";
import { Button } from "@/components/ui/button";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
  AITitleBodyForm,
  BaseFormProps,
  getIngredientQuantitySchema,
  getRecipeSchema,
  IngredientQuantitySchemaTexts,
  IngredientQuantitySchemaType,
  RecipeSchemaTexts,
  RecipeSchemaType,
  TitleBodyType,
} from "@/types/forms";
import { v4 as uuidv4 } from "uuid";
import { TitleBodyForm, TitleBodyTexts } from "@/components/forms/title-body";
import InputFile, { FieldInputTexts } from "@/components/forms/input-file";
import { determineMostRestrictiveDiet, handleBaseError } from "@/lib/utils";
import {
  IngredientPieChartTexts,
  MacroChartElement,
} from "@/components/charts/ingredient-macros-pie-chart";
import { DiamondMinus, DiamondPlus } from "lucide-react";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import { BaseFormTexts } from "@/texts/components/forms";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import ErrorMessage from "@/components/forms/error-message";
import { Card, CardContent, CardTitle } from "@/components/ui/card";
import { fetchWithFilesMultipleFiles } from "@/hoooks/fetchWithFiles";
import { toast } from "@/components/ui/use-toast";
import { BaseError } from "@/types/responses";
import useFilesObjectURL from "@/hoooks/useFilesObjectURL";
import useProgressWebSocket from "@/hoooks/useProgressWebSocket";
import UploadingProgress, {
  UploadingProgressTexts,
} from "@/components/forms/uploading-progress";
import { AiIdeasField } from "@/types/ai-ideas-types";
import useBaseAICallbackTitleBody from "@/hoooks/useBaseAICallbackTitleBody";
import DiffusionImagesForm, {
  DiffusionImagesFormCallback,
  DiffusionImagesFormTexts,
} from "@/components/forms/diffusion-images-form";
import useGetDiffusionImages from "@/hoooks/useGetDiffusionImages";
import { useNavigationGuardI18nForm } from "@/hoooks/use-navigation-guard-i18n-form";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import dynamic from "next/dynamic";
import { Skeleton } from "@/components/ui/skeleton";

export function calculateMacroProportions(
  items: {
    fat: number;
    protein: number;
    carbohydrates: number;
    salt: number;
    quantity: number;
  }[],
): MacroChartElement[] {
  const totalMacros = items.reduce(
    (totals, ingredient) => {
      totals.fat += (ingredient.fat * ingredient.quantity) / 100;
      totals.protein += (ingredient.protein * ingredient.quantity) / 100;
      totals.carbohydrates +=
        (ingredient.carbohydrates * ingredient.quantity) / 100;
      totals.salt += (ingredient.salt * ingredient.quantity) / 100;
      totals.totalQuantity += ingredient.quantity;
      return totals;
    },
    { fat: 0, protein: 0, carbohydrates: 0, salt: 0, totalQuantity: 0 },
  );

  return [
    {
      macro: "fat",
      value: (totalMacros.fat / totalMacros.totalQuantity) * 100,
    },
    {
      macro: "protein",
      value: (totalMacros.protein / totalMacros.totalQuantity) * 100,
    },
    {
      macro: "carbohydrates",
      value: (totalMacros.carbohydrates / totalMacros.totalQuantity) * 100,
    },
    {
      macro: "salt",
      value: (totalMacros.salt / totalMacros.totalQuantity) * 100,
    },
  ];
}

export interface RecipeFormTexts extends SingleChildFormTexts, AITitleBodyForm {
  ingredientQuantitySchemaTexts: IngredientQuantitySchemaTexts;
  recipeSchemaTexts: RecipeSchemaTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  imagesText: FieldInputTexts;
  videosText: FieldInputTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  baseFormTexts: BaseFormTexts;
  titleBodyTexts: TitleBodyTexts;
  childInputMultipleSelectorTexts: ChildInputMultipleSelectorTexts;
  error: string;
  ingredientsLabel: string;
  addIngredient: string;
  dietType: string;
  areIngredientsCompletedButNotSubmitted: string;
  continueBtn: string;
  loadedImages: UploadingProgressTexts;
  loadedVideos: UploadingProgressTexts;
  diffusionImagesFormTexts: DiffusionImagesFormTexts;
}

export interface RecipeFormProps
  extends RecipeFormTexts,
    BaseFormProps,
    Partial<TitleBodyType> {
  images?: string[];
  videos?: string[];
  initialChildren?: Record<string, Option & { quantity: number }>;
  // initialIngredients?: RecipeSchemaType["ingredients"];
}

const DynamicIngredientPieChart = dynamic(
  () => import("@/components/charts/ingredient-macros-pie-chart"),
  {
    ssr: false,
    loading: () => (
      <Skeleton className="mx-auto aspect-square  max-h-[350px] lg:max-h-[400px]" />
    ),
  },
);

export default function RecipeForm({
  recipeSchemaTexts,
  ingredientQuantitySchemaTexts,
  ingredientPieChartTexts,
  titleBodyTexts,
  videosText,
  imagesText,
  buttonSubmitTexts,
  childInputMultipleSelectorTexts,
  error,
  baseFormTexts,
  type,
  path,
  ingredientChildLabel,
  submitChild,
  removeChild,
  clearChild,
  ingredientsLabel,
  addIngredient,
  quantityChildPlaceholder,
  quantityChildLabel,
  dietType,
  title = "",
  body = "",
  images = [],
  videos = [],
  // initialIngredients = [],
  initialChildren = {},
  areIngredientsCompletedButNotSubmitted,
  continueBtn,
  loadedImages,
  loadedVideos,
  titleAIGeneratedPopTexts,
  bodyAIGeneratedPopTexts,
  aiCheckBoxes,
  diffusionImagesFormTexts,
}: RecipeFormProps) {
  const { authUser } = useAuthUserMinRole();

  const initialChildrenKeys = Object.keys(initialChildren);
  const initialChildrenValues = Object.values(initialChildren);
  const recipeSchema = useMemo(
    () => getRecipeSchema(recipeSchemaTexts),
    [recipeSchemaTexts],
  );
  const [clientId] = useState(uuidv4);
  const [editorKey, setEditorKey] = useState<number>(0);
  const [isImagesListCollapsed, setIsImagesListCollapsed] = useState(true);

  const [
    isIngredientCompletedButNotSubmitted,
    setIsIngredientCompletedButNotSubmitted,
  ] = useState<Record<string, boolean>>({});

  const { messages: messagesVideos } = useProgressWebSocket(
    authUser.token,
    clientId,
    "VIDEO",
  );
  const { messages: messagesImages } = useProgressWebSocket(
    authUser.token,
    clientId,
    "IMAGE",
  );

  const isOneIngredientCompletedButNotSubmitted = useMemo(() => {
    const values = Object.values(isIngredientCompletedButNotSubmitted);
    return values.length > 0 && values.includes(true);
  }, [isIngredientCompletedButNotSubmitted]);

  const form = useForm<RecipeSchemaType>({
    resolver: zodResolver(recipeSchema),
    defaultValues: {
      title,
      body,
      images: [],
      videos: [],
      ingredients: initialChildrenValues.map((i) => ({
        id: parseInt(i.value),
        quantity: i.quantity,
      })),
    },
  });

  useNavigationGuardI18nForm({ form });

  const watchImages = form.watch("images");
  const watchVideos = form.watch("videos");
  const watchBody = form.watch("body");
  const watchTitle = form.watch("title");

  const baseAICallback = useBaseAICallbackTitleBody(form, setEditorKey);

  const {
    fileCleanup: imagesCleanup,
    chunkProgressValue: imagesChunkProgress,
  } = useFilesObjectURL({
    files: images,
    fieldName: "images",
    setValue: form.setValue,
    getValues: form.getValues,
    currentItems: watchImages,
  });
  const {
    fileCleanup: videosCleanup,
    chunkProgressValue: videosChunkProgress,
  } = useFilesObjectURL({
    files: videos,
    fieldName: "videos",
    setValue: form.setValue,
    getValues: form.getValues,
    currentItems: watchVideos,
  });

  const isSubmitDisabled = useMemo(
    () =>
      (imagesChunkProgress < 100 && images.length > 0) ||
      (videosChunkProgress < 100 && videos.length > 0),
    [imagesChunkProgress, images.length, videosChunkProgress, videos.length],
  );

  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();

  const ingredients = form.watch("ingredients");

  const [selectedOptions, setSelectedOptions] = useState<Option[]>(
    initialChildrenValues,
  );

  const [children, setChildren] = useState<string[]>(
    initialChildrenKeys?.length > 0 ? initialChildrenKeys : [uuidv4()],
  );

  const [chartItems, setChartItems] = useState<MacroChartElement[]>([]);

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
    const items = ingredients.reduce(
      (acc, cur) => {
        const item = selectedOptions.find((o) => o.value === cur.id.toString());
        if (item) {
          acc.push({
            fat: parseFloat(item.fat as string),
            protein: parseFloat(item.protein as string),
            carbohydrates: parseFloat(item.carbohydrates as string),
            salt: parseFloat(item.salt as string),
            quantity: cur.quantity,
          });
        }
        return acc;
      },
      [] as {
        fat: number;
        protein: number;
        carbohydrates: number;
        salt: number;
        quantity: number;
      }[],
    );

    if (items.length) {
      setChartItems(calculateMacroProportions(items));
    } else {
      setChartItems([]);
    }
  }, [selectedOptions, JSON.stringify(ingredients)]);

  const handleOnChange = useCallback((options: Option[]) => {
    setSelectedOptions((prev) =>
      prev.findIndex((o) => o.value === options[0].value) === -1
        ? [...prev, ...options]
        : prev.filter((o) => o.value !== options[0].value),
    );
  }, []);
  const disableCallback = useCallback(
    (
      i: PageableResponse<CustomEntityModel<IngredientNutritionalFactResponse>>,
    ) =>
      selectedOptions.findIndex(
        (o) => o.value === i.content.content.ingredient.id.toString(),
      ) !== -1,
    [selectedOptions],
  );

  const handleClear = useCallback(
    (childId: string) => {
      const option = selectedOptions.find(
        (o) => o.childId === childId.toString(),
      );

      setSelectedOptions((prev) =>
        prev.filter((o) => o.childId !== childId.toString()),
      );
      if (option) {
        const newIngredients = ingredients.filter(
          (i) => i.id !== parseInt(option.value),
        );
        form.setValue("ingredients", newIngredients);
      }
    },
    [form, ingredients, selectedOptions],
  );

  const handleRemove = useCallback(
    (childId: string) => {
      setIsIngredientCompletedButNotSubmitted((prev) => {
        if (prev[childId]) {
          delete prev[childId];
        }
        return prev;
      });
      handleClear(childId);
      setChildren((prev) => prev.filter((id) => id !== childId));
    },
    [handleClear],
  );

  const submitCallback = useCallback(
    (data: IngredientQuantitySchemaType) => {
      form.clearErrors("ingredients");
      const newIngredients = [...ingredients, data];
      form.setValue("ingredients", newIngredients);
    },
    [form, ingredients],
  );
  const onSubmit = useCallback(
    async (data: RecipeSchemaType) => {
      setIsLoading(true);
      setErrorMsg("");
      const recipeBody: RecipeBody = {
        type:
          determineMostRestrictiveDiet(
            selectedOptions.map((o) => o.type as DietType),
          ) || "OMNIVORE",
        ingredients: data.ingredients.map((i) => ({
          ingredientId: i.id,
          quantity: i.quantity,
        })),
        title: data.title,
        body: data.body,
      };
      const images = data.images.map((i) => i.file);
      const videos = data.videos.map((i) => i.file);
      const fileObj = { images, videos };
      try {
        const res = await fetchWithFilesMultipleFiles<
          RecipeBody,
          BaseError,
          CustomEntityModel<RecipeResponse>
        >({
          path,
          token: authUser.token,
          method: "POST",
          data: {
            body: recipeBody,
            filesObj: fileObj,
          },
          clientId,
        });
        toast({
          title: data.title,
          description: baseFormTexts.descriptionToast,
          variant: "success",
        });
        router.push(`/trainer/recipes/single/${res.content.id}`);
      } catch (e) {
        console.log("e", e);
        handleBaseError(e, setErrorMsg, error);
      } finally {
        setIsLoading(false);
      }
    },
    [
      setIsLoading,
      setErrorMsg,
      selectedOptions,
      path,
      authUser.token,
      baseFormTexts.descriptionToast,
      baseFormTexts.altToast,
      baseFormTexts.toastAction,
      router,
      error,
      clientId,
    ],
  );
  useEffect(() => {
    return () => {
      invalidateImages();
    };
  }, [invalidateImages]);

  useEffect(() => {
    return () => {
      // console.log("cleaning files");
      imagesCleanup();
      videosCleanup();
    };
  }, [imagesCleanup, videosCleanup]);

  const aiFields: AiIdeasField[] = useMemo(() => {
    const fields: AiIdeasField[] = [];
    if (selectedOptions.length) {
      fields.push({
        content: selectedOptions
          .map(
            (o, i) =>
              `\n Ingredient Number:${i + 1}. Name of the ingredient: ${o.label}. Quantity in unit ${o?.unit || ""} : ${o?.quantity} \n`,
          )
          .join(","),
        name: "ingredients",
        isHtml: false,
        role: "Ingredients of the recipe",
      });
      fields.push({
        content:
          determineMostRestrictiveDiet(
            selectedOptions.map((o) => o.type as DietType),
          ) || "OMNIVORE",
        name: "dietType",
        isHtml: false,
        role: "Diet type of the recipe",
      });
    }
    if (watchTitle.trim()) {
      fields.push({
        content: watchTitle,
        name: "title",
        isHtml: false,
        role: "Title of the recipe",
      });
    }
    if (watchBody.trim()) {
      fields.push({
        content: watchBody,
        name: "body",
        isHtml: true,
        role: "Description of the of the recipe",
      });
    }
    return fields;
  }, [selectedOptions, watchBody, watchTitle]);
  return (
    <Card className="max-w-7xl w-full sm:px-2 md:px-5 py-6">
      <CardTitle className="font-bold text-2xl text-center capitalize mb-3.5 flex flex-col items-center gap-2.5">
        {baseFormTexts.header}
        {title && (
          <p className="font-semibold text-muted-foreground">{title}</p>
        )}
      </CardTitle>
      <CardContent className="w-full">
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-8 lg:space-y-12 w-full"
            noValidate
          >
            <TitleBodyForm<RecipeSchemaType>
              control={form.control}
              titleBodyTexts={titleBodyTexts}
              showAIPopDescription={true}
              showAIPopTitle={true}
              aiFields={aiFields}
              editorKey={editorKey}
              aiDescriptionCallBack={(r) => baseAICallback("body", r)}
              aiTitleCallBack={(r) => baseAICallback("title", r)}
              aiItem={"recipe"}
              aiCheckBoxes={aiCheckBoxes}
              titleAIGeneratedPopTexts={titleAIGeneratedPopTexts}
              bodyAIGeneratedPopTexts={bodyAIGeneratedPopTexts}
            />
            <FormField
              control={form.control}
              name={"ingredients"}
              render={() => (
                <FormItem>
                  <FormLabel className="capitalize w-full flex items-center justify-between">
                    <p>{ingredientsLabel}</p>
                    <div>
                      <Button
                        type="button"
                        size={"lg"}
                        className="flex items-center justify-center gap-2"
                        onClick={() =>
                          setChildren((prev) => [...prev, uuidv4()])
                        }
                      >
                        <DiamondPlus />
                        <p>{addIngredient}</p>
                      </Button>
                    </div>
                  </FormLabel>
                  <FormControl>
                    <div className="w-full">
                      <AnimatePresence>
                        {children.map((childId, i) => (
                          <SingleChildForm
                            key={childId}
                            childId={childId}
                            disableCallback={disableCallback}
                            onChange={handleOnChange}
                            onRemove={handleRemove}
                            onSubmitCallback={submitCallback}
                            authUser={authUser}
                            onClear={handleClear}
                            // index={i}
                            childrenNumber={children.length}
                            ingredientQuantitySchemaTexts={
                              ingredientQuantitySchemaTexts
                            }
                            childInputMultipleSelectorTexts={
                              childInputMultipleSelectorTexts
                            }
                            clearChild={clearChild}
                            ingredientChildLabel={ingredientChildLabel}
                            removeChild={removeChild}
                            submitChild={submitChild}
                            quantityChildPlaceholder={quantityChildPlaceholder}
                            quantityChildLabel={quantityChildLabel}
                            initialValue={initialChildren[childId]}
                            setIsIngredientCompletedButNotSubmitted={(
                              value,
                            ) => {
                              setIsIngredientCompletedButNotSubmitted(
                                (prev) => ({
                                  ...prev,
                                  [childId]: value,
                                }),
                              );
                            }}
                          />
                        ))}
                      </AnimatePresence>
                    </div>
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            {children.length > 1 && (
              <div className="w-full flex items-center justify-end">
                <AnimatePresence>
                  <motion.div
                    initial={{ opacity: 0, scale: 0 }}
                    animate={{ opacity: 1, scale: 1 }}
                    exit={{ opacity: 0, scale: 0 }}
                    transition={{ duration: 0.5 }}
                  >
                    <Button
                      type="button"
                      size={"lg"}
                      className="flex items-center justify-center gap-2"
                      onClick={() => setChildren((prev) => [...prev, uuidv4()])}
                    >
                      <DiamondPlus />
                      <p>{addIngredient}</p>
                    </Button>
                  </motion.div>
                </AnimatePresence>
              </div>
            )}
            <div>
              <AnimatePresence>
                {chartItems.length > 0 && (
                  <motion.div
                    initial={false}
                    animate={{ opacity: 1, height: "auto" }}
                    exit={{ opacity: 0, height: 0 }}
                    transition={{ duration: 0.5 }}
                    className="overflow-hidden h-[400px] "
                  >
                    <div className=" h-full">
                      <p className="tex-lg mb-5 text-center ">
                        {dietType}
                        <span className="text-2xl font-semibold ms-2">
                          {determineMostRestrictiveDiet(
                            selectedOptions.map((o) => o.type as DietType),
                          )}
                        </span>
                      </p>
                      <DynamicIngredientPieChart
                        innerRadius={85}
                        items={chartItems}
                        texts={ingredientPieChartTexts}
                      />
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
            <InputFile<RecipeSchemaType>
              control={form.control}
              fieldName={"images"}
              fieldTexts={imagesText}
              initialLength={images?.length || 0}
              parentListCollapsed={isImagesListCollapsed}
              loadingProgress={imagesChunkProgress}
            />
            <DiffusionImagesForm
              texts={diffusionImagesFormTexts}
              callback={addDiffusionImages}
            />
            <InputFile<RecipeSchemaType>
              control={form.control}
              fieldName={"videos"}
              fieldTexts={videosText}
              initialLength={videos?.length || 0}
              loadingProgress={videosChunkProgress}
            />
            {isOneIngredientCompletedButNotSubmitted && (
              <div className="w-full space-y-8">
                <p className="text-lg font-medium text-destructive">
                  {areIngredientsCompletedButNotSubmitted}
                </p>
                <Button
                  variant={"destructive"}
                  type={"button"}
                  onClick={() => {
                    setIsIngredientCompletedButNotSubmitted({});
                  }}
                >
                  {continueBtn}
                </Button>
              </div>
            )}
            {!isOneIngredientCompletedButNotSubmitted && (
              <ButtonSubmit
                isLoading={isLoading}
                disable={isSubmitDisabled}
                buttonSubmitTexts={buttonSubmitTexts}
              />
            )}
            {isLoading && (
              <div className="space-y-5">
                <UploadingProgress
                  total={watchImages.length}
                  loaded={messagesImages.length}
                  {...loadedImages}
                />
                <UploadingProgress
                  total={watchVideos.length}
                  loaded={messagesVideos.length}
                  {...loadedVideos}
                />
              </div>
            )}
            <ErrorMessage message={error} show={!!errorMsg} />
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}

interface SingleChildFormTexts {
  ingredientChildLabel: string;
  submitChild: string;
  removeChild: string;
  clearChild: string;
  quantityChildPlaceholder: string;
  quantityChildLabel: string;
}

interface SingleChildFormProps extends WithUser, SingleChildFormTexts {
  disableCallback: (
    i: PageableResponse<CustomEntityModel<IngredientNutritionalFactResponse>>,
  ) => boolean;
  onChange: (options: Option[]) => void;
  childId: string;
  onRemove: (childId: string) => void;
  onClear: (childId: string) => void;
  onSubmitCallback: (data: IngredientQuantitySchemaType) => void;
  childrenNumber: number;
  ingredientQuantitySchemaTexts: IngredientQuantitySchemaTexts;
  childInputMultipleSelectorTexts: ChildInputMultipleSelectorTexts;
  initialId?: number;
  initialQuantity?: number;
  initialValue?: Option & { quantity: number };
  setIsIngredientCompletedButNotSubmitted: (value: boolean) => void;
}
function SingleChildForm({
  authUser,
  disableCallback,
  onChange,
  childId,
  onRemove,
  onSubmitCallback,
  onClear,
  childrenNumber,
  ingredientQuantitySchemaTexts,
  childInputMultipleSelectorTexts,
  ingredientChildLabel,
  submitChild,
  removeChild,
  clearChild,
  quantityChildPlaceholder,
  quantityChildLabel,
  initialValue = undefined,
  setIsIngredientCompletedButNotSubmitted,
}: SingleChildFormProps) {
  const ingredientQuantitySchema = useMemo(
    () => getIngredientQuantitySchema(ingredientQuantitySchemaTexts),
    [ingredientQuantitySchemaTexts],
  );

  const [wasSubmitted, setWasSubmitted] = useState(!!initialValue);
  const [selectorValue, setSelectorValue] = useState<Option[]>(
    initialValue ? [initialValue] : [],
  );

  const form = useForm<IngredientQuantitySchemaType>({
    resolver: zodResolver(ingredientQuantitySchema),
    defaultValues: {
      id: undefined,
      quantity: undefined,
    },
  });
  const watchId = form.watch("id");
  const watchQuantity = form.watch("quantity");

  useEffect(() => {
    if (watchId && watchQuantity && !wasSubmitted) {
      setIsIngredientCompletedButNotSubmitted(true);
    }
  }, [wasSubmitted, watchId, watchQuantity]);

  useEffect(() => {
    if (initialValue) {
      form.setValue("id", parseInt(initialValue.value));
      form.setValue("quantity", initialValue.quantity);
    }
  }, []);

  const onSubmit = useCallback(
    async (data: IngredientQuantitySchemaType) => {
      setIsIngredientCompletedButNotSubmitted(false);
      onSubmitCallback(data);
      console.log("data", data);
      setWasSubmitted(true);
    },
    [onSubmitCallback],
  );
  const motionProps =
    childrenNumber > 1
      ? {
          initial: { opacity: 0, height: 0 },
          animate: { opacity: 1, height: "auto" },
          exit: { opacity: 0, height: 0 },
          transition: { duration: 0.5 },
        }
      : {};

  const mapping: (
    i: PageableResponse<CustomEntityModel<IngredientNutritionalFactResponse>>,
  ) => Option = useCallback(
    (i) => ({
      value: i.content.content.ingredient.id.toString(),
      label: i.content.content.ingredient.name,
      disable: disableCallback(i),
      childId: childId.toString(),
      type: i.content.content.ingredient.type,
      unit: i.content.content.nutritionalFact.unit,
      fat: i.content.content.nutritionalFact.fat.toString(),
      saturatedFat: i.content.content.nutritionalFact.saturatedFat.toString(),
      protein: i.content.content.nutritionalFact.protein.toString(),
      carbohydrates: i.content.content.nutritionalFact.carbohydrates.toString(),
      sugar: i.content.content.nutritionalFact.sugar.toString(),
      salt: i.content.content.nutritionalFact.salt.toString(),
    }),
    [childId, disableCallback],
  );

  const onChangeChild: (options: Option[]) => void = useCallback(
    (options) => {
      const currentId = form.getValues("id");
      if (currentId) {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        form.setValue("id", undefined);
        setSelectorValue([]);
      } else {
        form.setValue("id", parseInt(options[0].value));
        setSelectorValue(options);
      }

      onChange(options);
    },
    [form, onChange],
  );

  return (
    <motion.div
      {...motionProps}
      className="w-full h-full flex flex-col lg:flex-row items-center justify-center  mt-10 gap-5"
    >
      <div className="flex-1 flex items-start justify-center ">
        <div className="flex-1 ">
          <Form {...form}>
            <div className="space-y-8 lg:space-y-12 w-full">
              <div className="w-full flex flex-col lg:flex-row gap-5">
                <FormField
                  control={form.control}
                  name={"id"}
                  render={({ field, formState }) => (
                    <FormItem className="flex-1">
                      <FormLabel className="capitalize">
                        {ingredientChildLabel}
                      </FormLabel>
                      <FormControl>
                        <ChildInputMultipleSelector<
                          PageableResponse<
                            CustomEntityModel<IngredientNutritionalFactResponse>
                          >
                        >
                          disabled={wasSubmitted}
                          path="/ingredients/filtered"
                          // sortingCriteria={{ name: "asc" }}
                          extraQueryParams={{ display: "true" }}
                          valueKey="name"
                          maxSelected={1}
                          pageSize={20}
                          closeOnSelect={true}
                          mapping={mapping}
                          value={selectorValue}
                          onChange={onChangeChild}
                          authUser={authUser}
                          {...childInputMultipleSelectorTexts}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name={"quantity"}
                  render={({ field }) => (
                    <FormItem className="flex-1">
                      <FormLabel className="capitalize">
                        {quantityChildLabel}
                      </FormLabel>
                      <FormControl>
                        <Input
                          disabled={wasSubmitted || !form.getValues("id")}
                          placeholder={quantityChildPlaceholder}
                          type={"number"}
                          min={1}
                          {...field}
                          value={field.value || ""}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <div className="flex flex-col lg:flex-row items-center justify-between  h-full gap-5">
                <div>
                  <Button
                    type="button"
                    disabled={wasSubmitted}
                    onClick={() => {
                      form.handleSubmit(onSubmit)();
                    }}
                  >
                    {submitChild}
                  </Button>
                </div>
                <div className="flex items-center justify-center gap-5">
                  <Button
                    variant="destructive"
                    disabled={
                      // wasSubmitted ||
                      childrenNumber === 1
                    }
                    onClick={() => {
                      setIsIngredientCompletedButNotSubmitted(false);
                      onRemove(childId);
                    }}
                    type="button"
                  >
                    <DiamondMinus className="me-2" />
                    <p>{removeChild}</p>
                  </Button>
                  <Button
                    variant="destructive"
                    type="button"
                    disabled={!initialValue ? !form.formState.isDirty : false}
                    onClick={() => {
                      form.reset();
                      setSelectorValue([]);
                      setWasSubmitted(false);
                      onClear(childId);
                    }}
                  >
                    {clearChild}
                  </Button>
                </div>
              </div>
            </div>
          </Form>
        </div>
      </div>
    </motion.div>
  );
}
