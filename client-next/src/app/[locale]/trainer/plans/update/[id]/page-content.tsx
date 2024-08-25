"use client";

import PlanForm, { PlanFormProps } from "@/components/forms/plan-form";
import useFetchStream from "@/hoooks/useFetchStream";
import {
  CustomEntityModel,
  DayResponse,
  DietType,
  PageableResponse,
  PlanResponse,
  RecipeResponse,
} from "@/types/dto";
import { BaseError } from "@/types/responses";
import LoadingSpinner from "@/components/common/loading-spinner";
import React, { useEffect, useMemo, useState } from "react";
import { checkOwner } from "@/lib/utils";
import { Option } from "@/components/ui/multiple-selector";
import useClientNotFound from "@/hoooks/useClientNotFound";

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
    messages: days,
    error: dayError,
    isFinished: dayFinished,
  } = useFetchStream<DayResponse, BaseError>({
    path: `/plans/days/${id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
  });

  const { navigateToNotFound } = useClientNotFound();

  const initialOptions: (Option & { dragId: string })[] = useMemo(
    () =>
      days.length == 0
        ? []
        : days.map(({ id, title }, i) => ({
            label: title,
            value: id.toString(),
            dragId: id + "_" + i,
          })),
    [days],
  );

  if (!planFinished || !dayFinished) return <LoadingSpinner />;

  if (planError || dayError) {
    return navigateToNotFound();
  }

  if (planFinished && !plan[0]?.content) {
    return navigateToNotFound();
  }

  if (dayFinished && !(days.length > 0)) {
    return navigateToNotFound();
  }

  const planResponse = plan[0].content;

  const ownerReturn = checkOwner(authUser, planResponse, navigateToNotFound);

  if (React.isValidElement(ownerReturn)) {
    return ownerReturn;
  }
  //initialOptions?.map((o, i) => ({ ...o, dragId: o.value + "_" + i }))

  console.log("plans", plan);
  console.log("days", days);

  return (
    <PlanForm
      authUser={authUser}
      {...props}
      price={planResponse.price}
      title={planResponse.title}
      body={planResponse.body}
      days={planResponse.days}
      objective={planResponse.objective}
      images={planResponse.images}
      initialOptions={initialOptions}
    />
  );
}
