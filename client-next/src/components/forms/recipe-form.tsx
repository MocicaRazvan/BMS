"use client";
import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
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
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
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
import { cn, determineMostRestrictiveDiet, handleBaseError } from "@/lib/utils";
import IngredientMacrosPieChart, {
  calculateMacroProportions,
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
import { ToastAction } from "@/components/ui/toast";
import { BaseError } from "@/types/responses";
import useFilesBase64 from "@/hoooks/useFilesBase64";

export interface RecipeFormTexts extends SingleChildFormTexts {
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
}

export interface RecipeFormProps
  extends WithUser,
    RecipeFormTexts,
    BaseFormProps,
    Partial<TitleBodyType> {
  images?: string[];
  videos?: string[];
  initialChildren?: Record<string, Option & { quantity: number }>;
  // initialIngredients?: RecipeSchemaType["ingredients"];
}
export default function RecipeForm({
  authUser,
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
}: RecipeFormProps) {
  const initialChildrenKeys = Object.keys(initialChildren);
  const initialChildrenValues = Object.values(initialChildren);

  const recipeSchema = useMemo(
    () => getRecipeSchema(recipeSchemaTexts),
    [recipeSchemaTexts],
  );

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

  useFilesBase64({
    files: images,
    fieldName: "images",
    setValue: form.setValue,
    getValues: form.getValues,
  });
  useFilesBase64({
    files: videos,
    fieldName: "videos",
    setValue: form.setValue,
    getValues: form.getValues,
  });

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
  }, [JSON.stringify(selectedOptions), JSON.stringify(ingredients)]);

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
      console.log("childId", childId);
      console.log("childIdOP", option);
      console.log("childIdSO", selectedOptions);
      setSelectedOptions((prev) =>
        prev.filter((o) => o.childId !== childId.toString()),
      );
      if (option) {
        // setSubmittedChildren((prev) =>
        //   prev.filter((o) => o.id !== parseInt(option.value)),
        // );
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
      handleClear(childId);
      setChildren((prev) => prev.filter((id) => id !== childId));
    },
    [handleClear],
  );

  const submitCallback = useCallback(
    (data: IngredientQuantitySchemaType) => {
      // setSubmittedChildren((prev) => [...prev, data]);
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
        });
        toast({
          title: data.title,
          description: baseFormTexts.descriptionToast,
          variant: "success",
          action: (
            <ToastAction
              altText={baseFormTexts.altToast}
              onClick={() =>
                // todo fix, on click goes to home
                router.push(`/trainer/recipes/single/${res.content.id}`)
              }
            >
              {baseFormTexts.toastAction}
            </ToastAction>
          ),
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
      authUser.token,
      baseFormTexts.altToast,
      baseFormTexts.descriptionToast,
      baseFormTexts.toastAction,
      error,
      path,
      selectedOptions,
      setErrorMsg,
      setIsLoading,
      router,
    ],
  );

  return (
    <Card className="max-w-7xl w-full sm:px-2 md:px-5 py-6">
      <CardTitle className="font-bold text-2xl text-center capitalize">
        {baseFormTexts.header}
        {title && <p className="inline ms-2">{title}</p>}
      </CardTitle>
      <CardContent className="w-full">
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-8 lg:space-y-12 w-full"
          >
            <TitleBodyForm<RecipeSchemaType>
              control={form.control}
              titleBodyTexts={titleBodyTexts}
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
                          <div key={childId} className="w-full">
                            <SingleChildForm
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
                              quantityChildPlaceholder={
                                quantityChildPlaceholder
                              }
                              quantityChildLabel={quantityChildLabel}
                              initialValue={initialChildren[childId]}
                            />
                          </div>
                        ))}
                      </AnimatePresence>
                    </div>
                  </FormControl>{" "}
                  <FormMessage />
                </FormItem>
              )}
            />
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
                      <IngredientMacrosPieChart
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
            />
            <InputFile<RecipeSchemaType>
              control={form.control}
              fieldName={"videos"}
              fieldTexts={videosText}
            />
            <ButtonSubmit
              isLoading={isLoading}
              disable={false}
              buttonSubmitTexts={buttonSubmitTexts}
            />
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

  useEffect(() => {
    if (initialValue) {
      form.setValue("id", parseInt(initialValue.value));
      form.setValue("quantity", initialValue.quantity);
    }
  }, []);

  const onSubmit = useCallback(
    async (data: IngredientQuantitySchemaType) => {
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

  // const motionProps = {
  //   initial: { opacity: 0, height: 0 },
  //   animate: { opacity: 1, height: "auto" },
  //   exit: { opacity: 0, height: 0 },
  //   transition: { duration: 0.5 },
  // };

  return (
    <motion.div
      {...motionProps}
      className="w-full h-full flex flex-col lg:flex-row items-center justify-center  mt-10 gap-5"
    >
      <div className="flex-1 flex items-start justify-center ">
        <div className="flex-1 ">
          <Form {...form}>
            <div
              // onSubmit={(e) => {
              //   e.stopPropagation();
              //   form.handleSubmit(onSubmit)(e);
              // }}
              className="space-y-8 lg:space-y-12 w-full"
            >
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
                          path={"/ingredients/filtered"}
                          sortingCriteria={{ name: "asc" }}
                          extraQueryParams={{ display: "true" }}
                          valueKey={"name"}
                          maxSelected={1}
                          mapping={(i) => ({
                            value: i.content.content.ingredient.id.toString(),
                            label: i.content.content.ingredient.name,
                            disable: disableCallback(i),
                            childId: childId.toString(),
                            type: i.content.content.ingredient.type,
                            unit: i.content.content.nutritionalFact.unit,
                            fat: i.content.content.nutritionalFact.fat.toString(),
                            saturatedFat:
                              i.content.content.nutritionalFact.saturatedFat.toString(),
                            protein:
                              i.content.content.nutritionalFact.protein.toString(),
                            carbohydrates:
                              i.content.content.nutritionalFact.carbohydrates.toString(),
                            sugar:
                              i.content.content.nutritionalFact.sugar.toString(),
                            salt: i.content.content.nutritionalFact.salt.toString(),
                          })}
                          value={selectorValue}
                          onChange={(options) => {
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
                          }}
                          authUser={authUser}
                          {...childInputMultipleSelectorTexts}
                        />
                      </FormControl>
                      <FormMessage />
                      {/*<FormDescription*/}
                      {/*  className={cn(selectorValue.length === 0 && "hidden")}*/}
                      {/*>*/}
                      {/*  {selectorValue[0]?.unit || ""}*/}
                      {/*</FormDescription>*/}
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
                    onClick={() => onRemove(childId)}
                    type="button"
                  >
                    <DiamondMinus className="me-2" />
                    <p>{removeChild}</p>
                  </Button>{" "}
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
          </Form>{" "}
        </div>
      </div>
    </motion.div>
  );
}
