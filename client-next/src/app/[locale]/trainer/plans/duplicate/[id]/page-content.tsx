"use client";

import PlanForm, { PlanFormProps } from "@/components/forms/plan-form";
import LoadingSpinner from "@/components/common/loading-spinner";
import React from "react";
import { checkOwner } from "@/lib/utils";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { useGetPlanWithDays } from "@/hoooks/plans/useGetPlanWithDays";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

interface Props extends PlanFormProps {
  id: string;
}

export default function DuplicatePlanPageContent({ id, ...props }: Props) {
  const { authUser } = useAuthUserMinRole();

  const {
    plan,
    planError,
    planFinished,
    dayFinished,
    days,
    dayError,
    initialOptions,
  } = useGetPlanWithDays(id);

  const { navigateToNotFound } = useClientNotFound();

  if (!planFinished || !dayFinished) return <LoadingSpinner />;

  if (
    planError ||
    dayError ||
    (planFinished && !plan[0]?.content) ||
    (dayFinished && !(days.length > 0))
  ) {
    return navigateToNotFound();
  }

  const planResponse = plan[0].content;

  const ownerReturn = checkOwner(authUser, planResponse, navigateToNotFound);

  if (React.isValidElement(ownerReturn)) {
    return ownerReturn;
  }

  return (
    <PlanForm
      {...props}
      price={planResponse.price}
      title={undefined}
      body={planResponse.body}
      days={planResponse.days}
      objective={planResponse.objective}
      images={planResponse.images}
      initialOptions={initialOptions}
    />
  );
}
