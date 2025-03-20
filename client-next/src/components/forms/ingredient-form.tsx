"use client";

import { WithUser } from "@/lib/user";
import {
  BaseFormProps,
  dietTypes,
  getIngredientNutritionalFactSchema,
  IngredientNutritionalFactSchemaTexts,
  IngredientNutritionalFactType,
  macroKeys,
  unitTypes,
} from "@/types/forms";
import { BaseFormTexts } from "@/texts/components/forms";
import { useCallback, useEffect, useMemo, useRef } from "react";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import ErrorMessage from "@/components/forms/error-message";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import {
  CustomEntityModel,
  IngredientNutritionalFactResponse,
} from "@/types/dto";
import { toast } from "@/components/ui/use-toast";
import { handleBaseError } from "@/lib/utils";
import { BaseError } from "@/types/responses";
import IngredientMacrosPieChart, {
  IngredientPieChartTexts,
} from "@/components/charts/ingredient-macros-pie-chart";

import { motion } from "framer-motion";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { useNavigationGuardI18nForm } from "@/hoooks/use-navigation-guard-i18n-form";

export interface FieldTexts {
  label: string;
  placeholder: string;
}

export interface IngredientFormTexts extends BaseFormTexts {
  buttonSubmitTexts: ButtonSubmitTexts;
  titleTaken: string;
  ingredientNutritionalFactSchemaTexts: IngredientNutritionalFactSchemaTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  name: FieldTexts;
  dietType: FieldTexts;
  unitType: FieldTexts & { description: string };
  macrosTexts: {
    [key in (typeof macroKeys)[number]]: FieldTexts;
  };
  disableTooltip: string;
}

interface Props
  extends WithUser,
    Partial<IngredientNutritionalFactType>,
    BaseFormProps,
    IngredientFormTexts {}

export default function IngredientForm({
  authUser,
  type = "create",
  path,
  buttonSubmitTexts,
  ingredientNutritionalFactSchemaTexts,
  error,
  descriptionToast,
  toastAction,
  altToast,
  header,
  titleTaken,
  ingredient,
  nutritionalFact,
  name,
  dietType,
  unitType,
  ingredientPieChartTexts,
  macrosTexts,
  disableTooltip,
}: Props) {
  const schema = useMemo(
    () =>
      getIngredientNutritionalFactSchema(ingredientNutritionalFactSchemaTexts),
    [ingredientNutritionalFactSchemaTexts],
  );
  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();
  const lastUpdatedField = useRef<string | null>(null);

  const form = useForm<IngredientNutritionalFactType>({
    resolver: zodResolver(schema),
    defaultValues: {
      ingredient: {
        type: ingredient?.type || undefined,
        name: ingredient?.name || "",
      },
      nutritionalFact: {
        carbohydrates: nutritionalFact?.carbohydrates || 0,
        sugar: nutritionalFact?.sugar || 0,
        fat: nutritionalFact?.fat || 0,
        saturatedFat: nutritionalFact?.saturatedFat || 0,
        protein: nutritionalFact?.protein || 0,
        salt: nutritionalFact?.salt || 0,
        unit: nutritionalFact?.unit || undefined,
      },
    },
    mode: "onTouched",
  });

  useNavigationGuardI18nForm({ form });

  const protein = form.watch("nutritionalFact.protein");
  const fat = form.watch("nutritionalFact.fat");
  const carbohydrates = form.watch("nutritionalFact.carbohydrates");
  const unitForm = form.watch("nutritionalFact.unit");
  const saturatedFat = form.watch("nutritionalFact.saturatedFat");
  const sugar = form.watch("nutritionalFact.sugar");
  const salt = form.watch("nutritionalFact.salt");
  const floatFat = parseFloat(fat as unknown as string);
  const floatSaturatedFat = parseFloat(saturatedFat as unknown as string);
  const floatSugar = parseFloat(sugar as unknown as string);
  const floatCarbohydrates = parseFloat(carbohydrates as unknown as string);
  const floatProtein = parseFloat(protein as unknown as string);
  const floatSalt = parseFloat(salt as unknown as string);
  const showChart =
    floatProtein >= 0 &&
    floatFat >= 0 &&
    floatCarbohydrates >= 0 &&
    floatSalt >= 0 &&
    floatProtein + floatFat + floatCarbohydrates + floatSalt > 0;

  console.log("protein", protein);

  useEffect(() => {
    lastUpdatedField.current = "fat";
  }, [fat]);

  useEffect(() => {
    lastUpdatedField.current = "carbohydrates";
  }, [carbohydrates]);

  useEffect(() => {
    lastUpdatedField.current = "protein";
  }, [protein]);

  useEffect(() => {
    lastUpdatedField.current = "salt";
  }, [salt]);

  useEffect(() => {
    if (
      form.formState.errors.nutritionalFact?.saturatedFat?.type === "custom" &&
      !form.formState.errors.nutritionalFact?.fat &&
      0 <= floatSaturatedFat &&
      floatSaturatedFat <= floatFat
    ) {
      form.clearErrors("nutritionalFact.saturatedFat");
    }
  }, [fat, floatFat, floatSaturatedFat, form, saturatedFat]);

  useEffect(() => {
    if (
      form.formState.errors.nutritionalFact?.sugar?.type === "custom" &&
      !form.formState.errors.nutritionalFact.carbohydrates &&
      0 <= floatSugar &&
      floatSugar <= floatCarbohydrates
    ) {
      form.clearErrors("nutritionalFact.sugar");
    }
  }, [carbohydrates, floatCarbohydrates, floatSugar, form, sugar]);

  useEffect(() => {
    form.clearErrors("nutritionalFact.carbohydrates");
    form.clearErrors("nutritionalFact.protein");
    form.clearErrors("nutritionalFact.fat");
    form.clearErrors("nutritionalFact.salt");
    form.clearErrors("nutritionalFact.sugar");
    form.clearErrors("nutritionalFact.saturatedFat");
  }, [form, unitForm]);

  useEffect(() => {
    const total = floatProtein + floatFat + floatCarbohydrates + floatSalt;
    if (
      total > 0 &&
      [floatProtein, floatFat, floatCarbohydrates, floatSalt].every(
        (v) => v >= 0,
      )
    ) {
      const keysToCheck: (keyof Omit<
        IngredientNutritionalFactType["nutritionalFact"],
        "unit" | "saturatedFat" | "sugar"
      >)[] = ["carbohydrates", "protein", "fat", "salt"];
      const manualErrors: Record<(typeof keysToCheck)[number], boolean> =
        keysToCheck.reduce(
          (acc, k) => ({
            ...acc,
            [k]: form.formState.errors.nutritionalFact?.[k]?.type === "manual",
          }),
          { carbohydrates: false, protein: false, fat: false, salt: false },
        );
      if (Object.values(manualErrors).some((v) => v)) {
        keysToCheck.forEach((k) => {
          form.clearErrors(`nutritionalFact.${k}`);
        });
      }
    }
  }, [floatCarbohydrates, floatFat, floatProtein, floatSalt, form]);

  const onSubmit = useCallback(
    async (data: IngredientNutritionalFactType) => {
      const total =
        data.nutritionalFact.carbohydrates +
        data.nutritionalFact.protein +
        data.nutritionalFact.fat +
        data.nutritionalFact.salt;

      if (total === 0) {
        form.setError("nutritionalFact.protein", {
          message:
            ingredientNutritionalFactSchemaTexts.nutritionalFact
              .atLeastOnePositive,
          type: "manual",
        });
        form.setError("nutritionalFact.fat", {
          message:
            ingredientNutritionalFactSchemaTexts.nutritionalFact
              .atLeastOnePositive,
          type: "manual",
        });
        form.setError("nutritionalFact.carbohydrates", {
          message:
            ingredientNutritionalFactSchemaTexts.nutritionalFact
              .atLeastOnePositive,
          type: "manual",
        });
        form.setError("nutritionalFact.salt", {
          message:
            ingredientNutritionalFactSchemaTexts.nutritionalFact
              .atLeastOnePositive,
          type: "manual",
        });
        return;
      }

      setIsLoading(true);
      setErrorMsg("");
      try {
        const res = await fetchStream<
          CustomEntityModel<IngredientNutritionalFactResponse>,
          BaseError & {
            name: string;
          }
        >({
          path,
          method: "POST",
          body: data,
          token: authUser?.token,
        });
        if (!res.error) {
          toast({
            title: data.ingredient.name,
            description: descriptionToast,
            variant: "success",
            // action: (
            //   <ToastAction
            //     altText={altToast}
            //     // todo to single ingredient
            //     onClick={() => router.push(`/admin/ingredients/`)}
            //   >
            //     {toastAction}
            //   </ToastAction>
            // ),
          });
          router.push(`/admin/ingredients/`);
        }
        //
        else {
          console.log("res", res.error);
          if (res.error.status === 409 && res.error?.name) {
            form.setError("ingredient.name", {
              message: titleTaken,
            });
            toast({
              title: titleTaken,
              variant: "destructive",
            });
          } else if (error) {
            handleBaseError(res.error, setErrorMsg, error);
          }
        }
      } catch (e) {
        console.log("form err", e);

        handleBaseError(e, setErrorMsg, error);
      } finally {
        setIsLoading(false);
      }
    },
    [
      altToast,
      authUser?.token,
      descriptionToast,
      error,
      form,
      ingredientNutritionalFactSchemaTexts.nutritionalFact.atLeastOnePositive,
      path,
      router,
      setErrorMsg,
      setIsLoading,
      titleTaken,
      toastAction,
    ],
  );

  return (
    <div className="max-w-7xl w-full sm:px-2 md:px-5 py-6  mx-auto min-w-[1000px]">
      <h1 className="text-lg lg:text-2xl font-bold tracking-tighter capitalize mb-8">
        {header}
        {ingredient?.name && (
          <p className="inline ms-2 text-3xl">{ingredient?.name}</p>
        )}
      </h1>
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(onSubmit)}
          className="space-y-8 lg:space-y-12"
          noValidate
        >
          <div className="flex flex-col lg:flex-row gap-8 lg:gap-12 w-full items-center justify-center">
            <div className="flex-1 w-full">
              <FormField
                control={form.control}
                name="ingredient.name"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{name.label}</FormLabel>
                    <FormControl>
                      <Input placeholder={name.placeholder} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            <div className="flex-1 w-full">
              <FormField
                control={form.control}
                name="ingredient.type"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{dietType.label}</FormLabel>
                    <FormControl>
                      <Select
                        onValueChange={field.onChange}
                        defaultValue={field.value}
                      >
                        <SelectTrigger>
                          <SelectValue placeholder={dietType.placeholder} />
                        </SelectTrigger>
                        <SelectContent>
                          {dietTypes.map((v) => (
                            <SelectItem
                              key={v}
                              value={v}
                              className="cursor-pointer"
                            >
                              {v}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
          </div>
          <FormField
            control={form.control}
            name="nutritionalFact.unit"
            render={({ field }) => (
              <FormItem>
                <FormLabel>{unitType.label}</FormLabel>
                <FormControl>
                  <Select
                    onValueChange={field.onChange}
                    defaultValue={field.value}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder={unitType.placeholder} />
                    </SelectTrigger>
                    <SelectContent>
                      {unitTypes.map((v) => (
                        <SelectItem
                          key={v}
                          value={v}
                          className="cursor-pointer"
                        >
                          {v}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </FormControl>
                <FormDescription className="text-lg tracking-tighter">
                  {/*The macros will be per 100 unit*/}
                  {unitType.description}
                </FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 lg:gap-12">
            {macroKeys.map((k) => (
              <div key={k} className="w-full col-span-1">
                <TooltipProvider>
                  <Tooltip>
                    <FormField
                      control={form.control}
                      name={`nutritionalFact.${k}`}
                      disabled={!unitForm}
                      render={({ field }) => (
                        <TooltipTrigger asChild>
                          <FormItem>
                            <FormLabel>{macrosTexts[k].label}</FormLabel>
                            <FormControl>
                              <Input
                                placeholder={macrosTexts[k].placeholder}
                                type="number"
                                min={0}
                                {...field}
                                onChange={(e) => {
                                  switch (k) {
                                    case "fat":
                                      form.setValue(
                                        "nutritionalFact.saturatedFat",
                                        0,
                                      );
                                      form.clearErrors(
                                        "nutritionalFact.saturatedFat",
                                      );
                                      break;
                                    case "carbohydrates":
                                      form.setValue("nutritionalFact.sugar", 0);
                                      form.clearErrors("nutritionalFact.sugar");
                                      break;
                                    default:
                                      break;
                                  }
                                  field.onChange(e);
                                }}
                              />
                            </FormControl>
                            {unitForm && <FormMessage />}
                          </FormItem>
                        </TooltipTrigger>
                      )}
                    />{" "}
                    {!unitForm && (
                      <TooltipContent>
                        <p>{disableTooltip}</p>
                      </TooltipContent>
                    )}
                  </Tooltip>
                </TooltipProvider>
              </div>
            ))}
          </div>
          {
            <motion.div
              initial={false}
              animate={{
                height: showChart ? "auto" : 0,
                opacity: showChart ? 1 : 0,
              }}
              transition={{ duration: 0.5 }}
              className="overflow-hidden h-[400px] "
            >
              <div className=" h-full">
                <IngredientMacrosPieChart
                  innerRadius={85}
                  items={[
                    { macro: "protein", value: floatProtein },
                    { macro: "fat", value: floatFat },
                    { macro: "carbohydrates", value: floatCarbohydrates },
                    { macro: "salt", value: floatSalt },
                  ]}
                  texts={ingredientPieChartTexts}
                />
              </div>
            </motion.div>
          }
          <ErrorMessage message={error} show={!!errorMsg} />
          <ButtonSubmit
            isLoading={isLoading}
            disable={false}
            buttonSubmitTexts={buttonSubmitTexts}
          />
        </form>
      </Form>
    </div>
  );
}
