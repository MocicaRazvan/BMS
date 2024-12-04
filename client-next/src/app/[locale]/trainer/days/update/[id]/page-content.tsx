"use client";
import DayForm, { DayFormProps } from "@/components/forms/day-form";
import useClientNotFound from "@/hoooks/useClientNotFound";
import LoadingSpinner from "@/components/common/loading-spinner";
import React from "react";
import { checkOwner } from "@/lib/utils";
import { useGetDayMealsRecipes } from "@/hoooks/days/useGetDayMealsRecipes";

interface Props extends DayFormProps {
  id: string;
}
export default function UpdateDayPageContent({
  id,
  authUser,
  ...props
}: Props) {
  const {
    day,
    dayFinished,
    dayError,
    mealsError,
    meals,
    mealsFinished,
    initialData,
  } = useGetDayMealsRecipes(id);
  const { navigateToNotFound } = useClientNotFound();

  if (!mealsFinished || !dayFinished) return <LoadingSpinner />;

  if (dayError || mealsError) {
    return navigateToNotFound();
  }

  if (dayFinished && !day[0]?.content) {
    return navigateToNotFound();
  }

  if (mealsFinished && !meals[0]?.entity?.content) {
    return navigateToNotFound();
  }

  const dayResponse = day[0].content;
  const ownerReturn = checkOwner(authUser, dayResponse, navigateToNotFound);

  if (React.isValidElement(ownerReturn)) {
    return ownerReturn;
  }

  return (
    <DayForm
      {...props}
      authUser={authUser}
      path={"/days/update/meals/" + id}
      initialData={initialData}
      existingDay={dayResponse}
    />
  );
}
