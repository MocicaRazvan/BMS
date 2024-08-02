"use client";

import {
  BaseFormProps,
  getImageSchema,
  getPlanSchema,
  getTitleBodySchema,
  ImageSchemaTexts,
  PlanSchemaTexts,
  PlanSchemaType,
  RecipeSchemaType,
  TitleBodySchemaTexts,
} from "@/types/forms";

import { z } from "zod";
import { WithUser } from "@/lib/user";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import React, { useCallback, useMemo, useState } from "react";
import { Option } from "@/components/ui/multiple-selector";
import { MacroChartElement } from "@/components/charts/ingredient-macros-pie-chart";
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
import { TitleBodyForm, TitleBodyTexts } from "@/components/forms/title-body";
import ChildInputMultipleSelector, {
  ChildInputMultipleSelectorTexts,
} from "@/components/forms/child-input-multipleselector";
import {
  CustomEntityModel,
  DietType,
  IngredientNutritionalFactResponse,
  PageableResponse,
  PlanBody,
  PlanResponse,
  RecipeResponse,
} from "@/types/dto";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import InputFile, { FieldInputTexts } from "@/components/forms/input-file";
import { determineMostRestrictiveDiet, handleBaseError } from "@/lib/utils";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import ErrorMessage from "@/components/forms/error-message";
import { fetchWithFiles } from "@/hoooks/fetchWithFiles";
import { BaseError } from "@/types/responses";
import { toast } from "@/components/ui/use-toast";
import { ToastAction } from "@/components/ui/toast";
import { BaseFormTexts } from "@/texts/components/forms";
import useFilesBase64 from "@/hoooks/useFilesBase64";

export interface PlanFormTexts {
  titleBodyTexts: TitleBodyTexts;
  childInputMultipleSelectorTexts: ChildInputMultipleSelectorTexts;
  imagesText: FieldInputTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  planSchemaTexts: PlanSchemaTexts;
  baseFormTexts: BaseFormTexts;
  priceLabel: string;
  pricePlaceholder: string;
  recipesLabel: string;
  recipePlaceholder: string;
  dietMessage: string;
}
export interface PlanFormProps
  extends WithUser,
    PlanFormTexts,
    BaseFormProps,
    Partial<Omit<PlanSchemaType, "images">> {
  images?: string[];
  initialOptions?: (Option & { type: DietType })[];
}

// const childSchema = z.object({
//   ids: z.array(
//     z.coerce
//       .number({ invalid_type_error: "PUT NR" })
//       .min(1, "Enter at least 1 recipe"),
//   ),
// });
export default function PlanForm({
  titleBodyTexts,
  authUser,
  childInputMultipleSelectorTexts,
  imagesText,
  buttonSubmitTexts,
  planSchemaTexts,
  baseFormTexts: { descriptionToast, toastAction, altToast, header, error },
  priceLabel,
  pricePlaceholder,
  recipePlaceholder,
  recipesLabel,
  dietMessage,
  path,
  price = 0,
  title = "",
  body = "",
  recipes = [],
  images = [],
  initialOptions = [],
}: PlanFormProps) {
  const planSchema = useMemo(
    () => getPlanSchema(planSchemaTexts),
    [planSchemaTexts],
  );

  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();
  const [selectedOptions, setSelectedOptions] =
    useState<Option[]>(initialOptions);

  const form = useForm<PlanSchemaType>({
    resolver: zodResolver(planSchema),
    defaultValues: {
      price,
      title,
      recipes,
      body,
      images: [],
    },
  });

  useFilesBase64({
    files: images,
    fieldName: "images",
    setValue: form.setValue,
    getValues: form.getValues,
  });

  const onSubmit = useCallback(
    async (data: PlanSchemaType) => {
      setIsLoading(true);
      setErrorMsg("");
      const body: PlanBody = {
        ...data,
        type: determineMostRestrictiveDiet(
          selectedOptions.map((o) => o.type as DietType),
        ) as DietType,
      };
      const files = data.images.map((image) => image.file);
      try {
        const res = await fetchWithFiles<
          PlanBody,
          BaseError,
          CustomEntityModel<PlanResponse>
        >({
          // path: "/plans/createWithImages",
          path,
          token: authUser.token,
          data: {
            files,
            body,
          },
        });
        toast({
          title: data.title,
          description: descriptionToast,
          variant: "success",
          action: (
            <ToastAction
              altText={altToast}
              // onClick={() => router.push(`/posts/single/${res.content.id}`)}
            >
              {toastAction}
            </ToastAction>
          ),
        });
      } catch (e) {
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
      selectedOptions,
      setErrorMsg,
      setIsLoading,
      toastAction,
      path,
    ],
  );
  console.log("selectedOptions", selectedOptions);
  return (
    <Card className="max-w-7xl w-full sm:px-2 md:px-5 py-6">
      <CardTitle className="font-bold text-2xl text-center capitalize">
        {header}
        {/*{title && <p className="inline ms-2">{title}</p>}*/}
      </CardTitle>
      <CardContent className="w-full">
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-8 lg:space-y-12 w-full"
          >
            <TitleBodyForm<PlanSchemaType>
              control={form.control}
              titleBodyTexts={titleBodyTexts}
            />
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
                      disabled={false}
                      path={`/recipes//trainer/filtered/${authUser.id}`}
                      sortingCriteria={{ title: "asc" }}
                      extraQueryParams={{ approved: "true" }}
                      valueKey={"title"}
                      mapping={(r) => ({
                        value: r.content.content.id.toString(),
                        label: r.content.content.title,
                        type: r.content.content.type,
                      })}
                      giveUnselectedValue={false}
                      value={selectedOptions}
                      onChange={(options) => {
                        console.log("options", options);
                        if (options.length > 0) {
                          form.clearErrors("recipes");
                        }
                        setSelectedOptions(options);
                        form.setValue(
                          "recipes",
                          options.map((o) => parseInt(o.value)),
                        );
                      }}
                      authUser={authUser}
                      {...childInputMultipleSelectorTexts}
                    />
                  </FormControl>{" "}
                  <FormMessage />
                  {selectedOptions.length > 0 && (
                    <FormDescription className="tex-lg">
                      {/*The diet will be for{" "}*/}
                      {dietMessage}
                      <span className="text-xl font-semibold ms-2">
                        {determineMostRestrictiveDiet(
                          selectedOptions.map((o) => o.type as DietType),
                        )}
                      </span>
                    </FormDescription>
                  )}
                </FormItem>
              )}
            />
            {/*{selectedOptions.length > 0 && (*/}
            {/*  <span className="text-2xl font-semibold ms-2">*/}
            {/*    {determineMostRestrictiveDiet(*/}
            {/*      selectedOptions.map((o) => o.type as DietType),*/}
            {/*    )}*/}
            {/*  </span>*/}
            {/*)}*/}
            <FormField
              control={form.control}
              name={"price"}
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="capitalize">{priceLabel}</FormLabel>
                  <FormControl>
                    <Input
                      placeholder={pricePlaceholder}
                      type={"number"}
                      // min={1}
                      {...field}
                      // value={field.value || ""}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <InputFile<PlanSchemaType>
              control={form.control}
              fieldName={"images"}
              fieldTexts={imagesText}
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
