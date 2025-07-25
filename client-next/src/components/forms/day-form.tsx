"use client";

import { TitleBodyForm, TitleBodyTexts } from "@/components/forms/title-body";
import ChildInputMultipleSelector, {
  ChildInputMultipleSelectorTexts,
} from "@/components/forms/child-input-multipleselector";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import { BaseFormTexts } from "@/texts/components/forms";
import {
  AITitleBodyForm,
  BaseFormProps,
  DaySchemaTexts,
  DaySchemaType,
  getDaySchema,
  getMealSchema,
  MealSchemaTexts,
  MealSchemaType,
} from "@/types/forms";
import { WithUser } from "@/lib/user";
import React, { useCallback, useEffect, useMemo, useState } from "react";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Card, CardContent, CardTitle } from "@/components/ui/card";
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
import {
  CustomEntityModel,
  DayBodyWithMeals,
  DayResponse,
  DayType,
  dayTypes,
  DietType,
  PageableResponse,
  RecipeResponse,
} from "@/types/dto";
import { Button } from "@/components/ui/button";
import { v4 as uuidv4 } from "uuid";
import { DiamondMinus, DiamondPlus, EditIcon } from "lucide-react";
import { AnimatePresence, motion } from "framer-motion";
import { Option } from "@/components/ui/multiple-selector";
import { Input } from "@/components/ui/input";
import { determineMostRestrictiveDiet, handleBaseError } from "@/lib/utils";
import ErrorMessage from "@/components/forms/error-message";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { toast } from "@/components/ui/use-toast";
import { AiIdeasField } from "@/types/ai-ideas-types";
import useBaseAICallbackTitleBody from "@/hoooks/useBaseAICallbackTitleBody";
import { useNavigationGuardI18nForm } from "@/hoooks/use-navigation-guard-i18n-form";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

export interface DayFromTexts extends SingleMealTexts, AITitleBodyForm {
  titleBodyTexts: TitleBodyTexts;
  mealsChildInputMultipleSelectorTexts: ChildInputMultipleSelectorTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  baseFormTexts: BaseFormTexts;
  daySchemaTexts: DaySchemaTexts;
  typeLabel: string;
  typePlaceholder: string;
  mealsLabel: string;
  addMeal: string;
  dayDietType: string;
  areMealsCompletedButNotSubmitted: string;
  continueBtn: string;
  dayTypesLabels: Record<DayType, string>;
}

export type CurrentMealType = MealSchemaType & { id: string };
export type InitialDataType = Record<
  string,
  CurrentMealType & {
    optionRecipes: (Option & { type: DietType })[];
  }
>;
export interface DayFormProps extends DayFromTexts, BaseFormProps {
  initialData?: InitialDataType;
  existingDay?: Partial<Omit<DaySchemaType, "meals">>;
}

export default function DayForm({
  daySchemaTexts,
  recipesChildInputMultipleSelectorTexts,
  titleBodyTexts,
  baseFormTexts: { descriptionToast, toastAction, altToast, header, error },
  buttonSubmitTexts,
  path,
  typeLabel,
  typePlaceholder,
  mealsLabel,
  addMeal,
  hourLabel,
  hourPlaceholder,
  hourDescription,
  minuteLabel,
  minutePlaceholder,
  minuteDescription,
  recipesLabel,
  submitMeal,
  editMeal,
  removeMeal,
  initialData,
  existingDay,
  dayDietType: dayD,
  mealType,
  areMealsCompletedButNotSubmitted,
  continueBtn,
  dayTypesLabels,
  aiCheckBoxes,
  titleAIGeneratedPopTexts,
  bodyAIGeneratedPopTexts,
}: DayFormProps) {
  const { authUser } = useAuthUserMinRole();

  const initialCurrentMeals = useMemo(
    () =>
      initialData
        ? Object.values(initialData).sort(
            (a, b) =>
              a.period.hour - b.period.hour ||
              a.period.minute - b.period.minute,
          )
        : [],
    [JSON.stringify(initialData)],
  );
  const initialChildrenMeals = useMemo(
    () => (!initialCurrentMeals ? [] : initialCurrentMeals.map(({ id }) => id)),
    [initialCurrentMeals],
  );

  const [childrenMeals, setChildrenMeals] =
    useState<string[]>(initialChildrenMeals);
  const [currentMeals, setCurrentMeals] =
    useState<CurrentMealType[]>(initialCurrentMeals);
  const daySchema = useMemo(
    () => getDaySchema(daySchemaTexts),
    [daySchemaTexts],
  );

  const [dayDietType, setDayDietType] = useState<DietType | undefined>(
    initialCurrentMeals
      ? determineMostRestrictiveDiet(
          initialCurrentMeals.reduce(
            (acc, { optionRecipes }) => [
              ...acc,
              ...optionRecipes.map(({ type }) => type),
            ],
            [] as DietType[],
          ),
        ) || undefined
      : undefined,
  );

  const [isAnyMealNotSubmitted, setIsAnyMealNotSubmitted] = useState(true);
  const [totalOptions, setTotalOptions] = useState<Option[]>(() => {
    const options = initialCurrentMeals.flatMap(
      ({ optionRecipes }) => optionRecipes,
    );
    const uniqueByLabel = new Map(
      options.map((option) => [option.label, option]),
    );

    return Array.from(uniqueByLabel.values());
  });

  const [isMealCompletedButNotSubmitted, setIsMealCompletedButNotSubmitted] =
    useState<Record<string, boolean>>({});
  const isOneMealCompletedButNotSubmitted = useMemo(() => {
    const values = Object.values(isMealCompletedButNotSubmitted);
    return values.length > 0 && values.includes(true);
  }, [isMealCompletedButNotSubmitted]);

  const { isLoading, setIsLoading, errorMsg, setErrorMsg, router } =
    useLoadingErrorState();

  const form = useForm<DaySchemaType>({
    resolver: zodResolver(daySchema),
    defaultValues: {
      type: existingDay?.type || undefined,
      meals: initialCurrentMeals || [],
      body: existingDay?.body || "",
      title: existingDay?.title || "",
    },
  });

  useNavigationGuardI18nForm({ form });

  const watchTitle = form.watch("title");
  const watchBody = form.watch("body");
  const watchType = form.watch("type");
  const handleMealRemove = useCallback(
    (mealId: string) => {
      setIsMealCompletedButNotSubmitted((prev) => {
        if (prev[mealId]) {
          delete prev[mealId];
        }
        return prev;
      });
      const newMeals = currentMeals.filter((meal) => meal.id !== mealId);
      setCurrentMeals(newMeals);
      setChildrenMeals((prev) => prev.filter((id) => id !== mealId));
      form.setValue("meals", newMeals);
    },
    [currentMeals, form],
  );

  const handleMealSubmit = useCallback(
    (data: MealSchemaType & { id: string }, selectedOptions: Option[]) => {
      const existingMeal = currentMeals.find((meal) => meal.id === data.id);
      let newMeals;
      if (existingMeal) {
        newMeals = currentMeals.map((meal) =>
          meal.id === data.id ? data : meal,
        );
      } else {
        newMeals = [...currentMeals, data];
      }
      setCurrentMeals(newMeals);
      form.setValue("meals", newMeals);
      setTotalOptions((prev) => {
        const combined = [...prev, ...selectedOptions];

        const uniqueByLabel = new Map(
          combined.map((option) => [option.label, option]),
        );

        return Array.from(uniqueByLabel.values());
      });
    },
    [currentMeals, form],
  );

  const aiFields: AiIdeasField[] = useMemo(() => {
    const fields: AiIdeasField[] = [];
    if (watchTitle.trim()) {
      fields.push({
        content: watchTitle,
        name: "title",
        isHtml: false,
        role: "Title of a day in a meal plan",
      });
    }
    if (watchBody.trim()) {
      fields.push({
        content: watchBody,
        name: "body",
        isHtml: true,
        role: "Description of a day in a meal plan",
      });
    }
    if (watchType) {
      fields.push({
        content: watchType,
        name: "type",
        isHtml: false,
        role: "Kind of a day in a meal plan",
      });
    }

    if (currentMeals.length) {
      fields.push({
        content: currentMeals.reduce((acc, m, i) => {
          acc += `\n Meal number ${i + 1}. Period: ${m.period.hour}:${m.period.minute}.`;
          if ("optionRecipes" in m && Array.isArray(m.optionRecipes)) {
            acc +=
              m.optionRecipes.reduce((acc, r) => {
                acc += ` Title of the recipe: ${r?.label}. Type of the recipe: ${r?.type}.`;
                return acc;
              }, `\t`) + `\t`;
          }
          acc += acc + " \n";
          return acc;
        }, ``),
        name: "recipes",
        isHtml: false,
        role: "Recipes of a day in a meal plan",
      });
      fields.push({
        content: dayDietType || "",
        name: "dietType",
        isHtml: false,
        role: "Diet type of a day in a meal plan",
      });
    }
    return fields;
  }, [dayDietType, currentMeals, watchBody, watchTitle, watchType]);

  const handleMealSubmitChange = useCallback((change: boolean) => {
    setIsAnyMealNotSubmitted(change);
  }, []);

  const onSubmit = useCallback(
    async (data: DaySchemaType) => {
      setIsLoading(true);
      setErrorMsg("");
      const parsedMeals = data.meals.map(
        ({ period: { hour, minute }, recipes }) => ({
          period: `${hour}:${minute < 10 ? `0${minute}` : minute}`,
          recipes,
        }),
      );
      const body: DayBodyWithMeals = {
        ...data,
        meals: parsedMeals,
        type: data.type as DayType,
      };
      try {
        const res = await fetchStream<CustomEntityModel<DayResponse>>({
          path,
          body,
          token: authUser.token,
          method: "POST",
        });
        if (res.error) {
          handleBaseError(res.error, setErrorMsg, error);
        } else {
          toast({
            title: data.title,
            description: descriptionToast,
            variant: "success",
          });

          router.push(`/trainer/days/single/${res?.messages?.[0]?.content.id}`);
        }
      } catch (e) {
        handleBaseError(e, setErrorMsg, error);
      } finally {
        setIsLoading(false);
      }
    },
    [
      authUser.token,
      descriptionToast,
      error,
      path,
      router,
      setErrorMsg,
      setIsLoading,
    ],
  );
  const [editorKey, setEditorKey] = useState<number>(0);
  const baseAICallback = useBaseAICallbackTitleBody(form, setEditorKey);

  const handleMealOptionsChange = useCallback((dietType: DietType | null) => {
    if (!dietType) return;
    setDayDietType(
      (prev) =>
        determineMostRestrictiveDiet(prev ? [prev, dietType] : [dietType]) ||
        undefined,
    );
  }, []);

  return (
    <Card className="max-w-7xl w-full sm:px-2 md:px-5 py-6 ">
      <CardTitle className="font-bold text-2xl text-center capitalize mb-3.5 flex flex-col items-center gap-2.5">
        {header}
        {existingDay?.title && (
          <p className="font-semibold text-muted-foreground">
            {existingDay.title}
          </p>
        )}
      </CardTitle>
      <CardContent className="w-full">
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-8 lg:space-y-12 w-full"
            noValidate
          >
            <TitleBodyForm<DaySchemaType>
              control={form.control}
              titleBodyTexts={titleBodyTexts}
              showAIPopDescription={true}
              showAIPopTitle={true}
              aiFields={aiFields}
              editorKey={editorKey}
              aiDescriptionCallBack={(r) => baseAICallback("body", r)}
              aiTitleCallBack={(r) => baseAICallback("title", r)}
              aiItem={"day in a meal plan"}
              aiCheckBoxes={aiCheckBoxes}
              titleAIGeneratedPopTexts={titleAIGeneratedPopTexts}
              bodyAIGeneratedPopTexts={bodyAIGeneratedPopTexts}
            />
            <FormField
              control={form.control}
              name="type"
              render={({ field }) => (
                <FormItem className="flex-1 w-full">
                  <FormLabel className="capitalize">{typeLabel}</FormLabel>
                  <Select
                    onValueChange={field.onChange}
                    defaultValue={field.value}
                  >
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder={typePlaceholder} />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {dayTypes.map((value) => (
                        <SelectItem
                          key={value}
                          value={value}
                          className="cursor-pointer"
                        >
                          {dayTypesLabels[value]}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name={"meals"}
              render={() => (
                <FormItem>
                  <FormLabel className="capitalize w-full flex items-center justify-between mb-10">
                    <p>{mealsLabel}</p>
                    <div>
                      <Button
                        type="button"
                        size={"lg"}
                        className="flex items-center justify-center gap-2"
                        onClick={() =>
                          setChildrenMeals((prev) => [...prev, uuidv4()])
                        }
                      >
                        <DiamondPlus />
                        <p>{addMeal}</p>
                      </Button>
                    </div>
                  </FormLabel>
                  <FormControl>
                    <div className="w-full py-1 md:py-2">
                      <AnimatePresence>
                        {childrenMeals.map((cm) => (
                          <SingleMealForm
                            mealId={cm}
                            onRemove={handleMealRemove}
                            onSubmitCallback={handleMealSubmit}
                            key={cm}
                            childrenNumber={childrenMeals.length}
                            mealSchemaTexts={daySchemaTexts.meals}
                            authUser={authUser}
                            recipesChildInputMultipleSelectorTexts={
                              recipesChildInputMultipleSelectorTexts
                            }
                            submitChangeCallback={handleMealSubmitChange}
                            currentMeals={currentMeals}
                            editMeal={editMeal}
                            removeMeal={removeMeal}
                            submitMeal={submitMeal}
                            hourDescription={hourDescription}
                            hourLabel={hourLabel}
                            hourPlaceholder={hourPlaceholder}
                            minuteDescription={minuteDescription}
                            minuteLabel={minuteLabel}
                            minutePlaceholder={minutePlaceholder}
                            recipesLabel={recipesLabel}
                            mealType={mealType}
                            initialMeal={initialData?.[cm]}
                            optionsChangeCallback={handleMealOptionsChange}
                            setIsMealCompletedButNotSubmitted={(value) => {
                              setIsMealCompletedButNotSubmitted((prev) => ({
                                ...prev,
                                [cm]: value,
                              }));
                            }}
                          />
                        ))}
                      </AnimatePresence>
                    </div>
                  </FormControl>
                </FormItem>
              )}
            />
            {childrenMeals.length > 1 && (
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
                      onClick={() =>
                        setChildrenMeals((prev) => [...prev, uuidv4()])
                      }
                    >
                      <DiamondPlus />
                      <p>{addMeal}</p>
                    </Button>
                  </motion.div>
                </AnimatePresence>
              </div>
            )}

            {dayDietType && (
              <FormDescription className="my-5">
                {dayD}{" "}
                <span className="text-xl font-semibold ms-2 my-1">
                  {dayDietType}
                </span>
              </FormDescription>
            )}
            <div className="mt-15">
              <ErrorMessage message={error} show={!!errorMsg} />
              {isOneMealCompletedButNotSubmitted && (
                <div className="w-full space-y-8">
                  <p className="text-lg font-medium text-amber">
                    {areMealsCompletedButNotSubmitted}
                  </p>
                  <Button
                    variant="amber"
                    type="button"
                    onClick={() => {
                      setIsMealCompletedButNotSubmitted({});
                    }}
                  >
                    {continueBtn}
                  </Button>
                </div>
              )}
              {!isOneMealCompletedButNotSubmitted && (
                <ButtonSubmit
                  isLoading={isLoading}
                  // disable={!isAnyMealNotSubmitted}
                  disable={false}
                  buttonSubmitTexts={buttonSubmitTexts}
                />
              )}
            </div>
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}

interface SingleMealTexts {
  mealSchemaTexts: MealSchemaTexts;
  recipesChildInputMultipleSelectorTexts: ChildInputMultipleSelectorTexts;
  hourLabel: string;
  hourPlaceholder: string;
  hourDescription: string;
  minuteLabel: string;
  minutePlaceholder: string;
  minuteDescription: string;
  recipesLabel: string;
  submitMeal: string;
  editMeal: string;
  removeMeal: string;
  mealType: string;
}

interface SingleMealProps extends WithUser, SingleMealTexts {
  mealId: string;
  onRemove: (mealId: string) => void;
  onSubmitCallback: (
    data: MealSchemaType & { id: string },
    selectedOptions: Option[],
  ) => void;
  childrenNumber: number;
  submitChangeCallback: (change: boolean) => void;
  currentMeals: CurrentMealType[];
  initialMeal?: InitialDataType[string];
  optionsChangeCallback: (dietType: DietType | null) => void;
  setIsMealCompletedButNotSubmitted: (value: boolean) => void;
}
const motionProps = {
  initial: { opacity: 0, height: 0 },
  animate: { opacity: 1, height: "auto" },
  exit: { opacity: 0, height: 0, transition: { duration: 0 } },
  transition: { duration: 0.5 },
};

function SingleMealForm({
  mealSchemaTexts,
  onSubmitCallback,
  onRemove,
  mealId,
  authUser,
  recipesChildInputMultipleSelectorTexts,
  submitChangeCallback,
  currentMeals,
  editMeal,
  removeMeal,
  submitMeal,
  hourDescription,
  hourPlaceholder,
  hourLabel,
  minuteDescription,
  recipesLabel,
  minutePlaceholder,
  minuteLabel,
  initialMeal,
  optionsChangeCallback,
  mealType,
  setIsMealCompletedButNotSubmitted,
}: SingleMealProps) {
  const mealSchema = useMemo(
    () => getMealSchema(mealSchemaTexts),
    [mealSchemaTexts],
  );
  const [wasSubmitted, setWasSubmitted] = useState(!!initialMeal);

  const [selectedOptions, setSelectedOptions] = useState<Option[]>(
    initialMeal ? initialMeal.optionRecipes : [],
  );

  const form = useForm<MealSchemaType>({
    resolver: zodResolver(mealSchema),
    defaultValues: {
      period: {
        hour: initialMeal?.period.hour || undefined,
        minute: initialMeal?.period.minute || undefined,
      },
      recipes: initialMeal?.recipes || [],
    },
  });

  const hourWatch = form.watch("period.hour");
  const minuteWatch = form.watch("period.minute");
  const recipesWatch = form.watch("recipes");

  useEffect(() => {
    if (hourWatch && minuteWatch && recipesWatch.length > 0 && !wasSubmitted) {
      setIsMealCompletedButNotSubmitted(true);
    }
  }, [hourWatch, minuteWatch, recipesWatch?.length, wasSubmitted]);

  useEffect(() => {
    if (!hourWatch || !minuteWatch) return;
    const isMealWithSamePeriod = currentMeals.find(
      ({ period: { hour, minute }, id }) =>
        hour?.toString() === hourWatch?.toString() &&
        minute?.toString() === minuteWatch?.toString() &&
        id !== mealId,
    );
    if (isMealWithSamePeriod) {
      form.setError(
        "period.hour",
        { message: mealSchemaTexts.periodUsed },
        { shouldFocus: true },
      );
      form.setError("period.minute", { message: mealSchemaTexts.periodUsed });
    } else {
      form.clearErrors("period.hour");
      form.clearErrors("period.minute");
    }
  }, [
    currentMeals,
    form,
    hourWatch,
    mealId,
    mealSchemaTexts.periodUsed,
    minuteWatch,
  ]);

  useEffect(() => {
    submitChangeCallback(wasSubmitted);
  }, [submitChangeCallback, wasSubmitted]);

  const onSubmit = useCallback(
    (data: MealSchemaType) => {
      setIsMealCompletedButNotSubmitted(false);
      setWasSubmitted(true);
      onSubmitCallback({ ...data, id: mealId }, selectedOptions);
    },
    [mealId, onSubmitCallback, selectedOptions],
  );
  const mapping: (
    i: PageableResponse<CustomEntityModel<RecipeResponse>>,
  ) => Option = useCallback(
    (r) => ({
      value: r.content.content.id.toString(),
      label: r.content.content.title + " - " + r.content.content.type,
      type: r.content.content.type,
    }),
    [],
  );

  const onChange: (options: Option[]) => void = useCallback(
    (options) => {
      if (options.length > 0) {
        form.clearErrors("recipes");
      }
      setSelectedOptions(options);
      optionsChangeCallback(
        determineMostRestrictiveDiet(options.map((o) => o.type as DietType)),
      );
      form.setValue(
        "recipes",
        options.map((o) => parseInt(o.value)),
      );
    },
    [form, optionsChangeCallback],
  );

  return (
    <motion.div {...motionProps} className="w-full h-full my-8 ">
      <Form {...form}>
        <div className="w-full h-full space-y-8 lg:space-y-10">
          <div className="w-full h-full flex flex-col lg:flex-row items-start justify-between gap-4 lg:gap-12">
            <FormField
              control={form.control}
              name="period.hour"
              render={({ field }) => (
                <FormItem className="flex-1  max-w-[400px]">
                  <FormLabel className="capitalize">{hourLabel}</FormLabel>
                  <FormControl>
                    <Input
                      placeholder={hourPlaceholder}
                      type={"number"}
                      {...field}
                      disabled={wasSubmitted}
                    />
                  </FormControl>
                  <FormMessage />
                  <FormDescription>{hourDescription}</FormDescription>
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="period.minute"
              render={({ field }) => (
                <FormItem className="flex-1 max-w-[400px]">
                  <FormLabel className="capitalize">{minuteLabel}</FormLabel>
                  <FormControl>
                    <Input
                      placeholder={minutePlaceholder}
                      type={"number"}
                      {...field}
                      disabled={wasSubmitted}
                    />
                  </FormControl>
                  <FormMessage />
                  <FormDescription>{minuteDescription}</FormDescription>
                </FormItem>
              )}
            />
          </div>
          <FormField
            control={form.control}
            name={"recipes"}
            render={() => (
              <FormItem>
                <FormLabel className={"capitalize"}>{recipesLabel}</FormLabel>
                <FormControl>
                  <ChildInputMultipleSelector<
                    PageableResponse<CustomEntityModel<RecipeResponse>>
                  >
                    disabled={wasSubmitted}
                    path={`/recipes/trainer/filtered/${authUser.id}`}
                    // sortingCriteria={{ title: "asc" }}
                    extraQueryParams={{ approved: "true" }}
                    valueKey={"title"}
                    pageSize={20}
                    mapping={mapping}
                    giveUnselectedValue={false}
                    value={selectedOptions}
                    onChange={onChange}
                    authUser={authUser}
                    {...recipesChildInputMultipleSelectorTexts}
                  />
                </FormControl>
                <FormMessage />
                {selectedOptions.length > 0 && (
                  <FormDescription className="tex-lg">
                    {mealType}{" "}
                    <span className="text-xl font-semibold ms-2 my-1">
                      {determineMostRestrictiveDiet(
                        selectedOptions.map((o) => o.type as DietType),
                      )}
                    </span>
                  </FormDescription>
                )}
              </FormItem>
            )}
          />
          <div className="flex flex-col lg:flex-row items-center justify-between  h-full gap-5">
            <div>
              {!wasSubmitted ? (
                <Button
                  type="button"
                  disabled={wasSubmitted}
                  onClick={() => {
                    form.handleSubmit(onSubmit)();
                  }}
                >
                  {submitMeal}
                </Button>
              ) : (
                <Button type="button" onClick={() => setWasSubmitted(false)}>
                  <EditIcon className="me-2" /> {editMeal}
                </Button>
              )}
            </div>
            <div className="flex items-center justify-center gap-5">
              <Button
                variant="destructive"
                onClick={() => {
                  setIsMealCompletedButNotSubmitted(false);
                  onRemove(mealId);
                }}
                disabled={wasSubmitted}
                type="button"
              >
                <DiamondMinus className="me-2" />
                <p>{removeMeal}</p>
              </Button>
            </div>
          </div>
        </div>
      </Form>
    </motion.div>
  );
}
