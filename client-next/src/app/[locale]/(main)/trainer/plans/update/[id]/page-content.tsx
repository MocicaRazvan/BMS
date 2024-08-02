"use client";

import PlanForm, { PlanFormProps } from "@/components/forms/plan-form";
import useFetchStream from "@/hoooks/useFetchStream";
import {
  CustomEntityModel,
  DietType,
  PageableResponse,
  PlanResponse,
  RecipeResponse,
} from "@/types/dto";
import { BaseError } from "@/types/responses";
import LoadingSpinner from "@/components/common/loading-spinner";
import { notFound } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { fetchStream } from "@/hoooks/fetchStream";
import { checkOwner } from "@/lib/utils";
import { Option } from "@/components/ui/multiple-selector";
import RecipeForm from "@/components/forms/recipe-form";

interface Props extends PlanFormProps {
  id: string;
}

export default function UpdatePlanPageContent({
  id,
  authUser,
  ...props
}: Props) {
  const {
    messages: plan,
    error: planError,
    isFinished: planFinished,
  } = useFetchStream<CustomEntityModel<PlanResponse>, BaseError>({
    path: `/plans/${id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
  });

  const {
    messages: recipes,
    error: recipeError,
    isFinished: recipeFinished,
  } = useFetchStream<RecipeResponse, BaseError>({
    path: `/plans/recipes/${id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
  });

  const initialOptions: (Option & { type: DietType })[] = useMemo(
    () =>
      recipes.length == 0
        ? []
        : recipes.map(({ id, title, type }) => ({
            label: title,
            value: id.toString(),
            type,
          })),
    [recipes],
  );

  if (!planFinished || !recipeFinished) return <LoadingSpinner />;

  if (planError || recipeError) {
    return notFound();
  }

  if (planFinished && !plan[0]?.content) {
    return notFound();
  }

  if (recipeFinished && !(recipes.length > 0)) {
    return notFound();
  }

  const planResponse = plan[0].content;

  checkOwner(authUser, planResponse);

  console.log("plans", plan);
  console.log("recipes", recipes);

  return (
    <main className="flex items-center justify-center px-6 py-10">
      <PlanForm
        authUser={authUser}
        {...props}
        price={planResponse.price}
        title={planResponse.title}
        body={planResponse.body}
        recipes={planResponse.recipes}
        images={planResponse.images}
        initialOptions={initialOptions}
      />
    </main>
  );
}
