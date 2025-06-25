"use client";

import useGetDaysWithMeals from "@/hoooks/days/useGetDayWithMeals";
import useClientNotFound from "@/hoooks/useClientNotFound";
import LoadingSpinner from "@/components/common/loading-spinner";
import React from "react";
import { checkOwnerOrAdmin, isSuccessCheckReturn } from "@/lib/utils";
import SingleDay, { SingleDayTexts } from "@/components/days/single-day";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

export interface SingleDayPageTexts {
  singleDayTexts: SingleDayTexts;
}

interface Props extends SingleDayPageTexts {
  id: string;
}
export default function SingleDayTrainerPageContent({
  id,
  singleDayTexts,
}: Props) {
  const { authUser } = useAuthUserMinRole();

  const {
    isLiked,
    day,
    dayState,
    dayIsFinished,
    mealsIsFinished,
    dayError,
    setDayState,
    mealsError,
    mealsMessage,
    messages,
    user,
    router,
    isDisliked,
    meals,
  } = useGetDaysWithMeals({ dayId: id, authUser });

  const { navigateToNotFound } = useClientNotFound();

  if (!mealsIsFinished || !dayIsFinished) {
    return <LoadingSpinner />;
  }

  if (dayError || mealsError) {
    return navigateToNotFound();
  }

  if (!dayState || !meals) return null;

  const ownerReturn = checkOwnerOrAdmin(authUser, dayState, navigateToNotFound);

  if (React.isValidElement(ownerReturn)) {
    return ownerReturn;
  }

  if (!isSuccessCheckReturn(ownerReturn)) {
    return navigateToNotFound();
  }

  return (
    <SingleDay
      day={dayState}
      author={user}
      meals={meals}
      texts={singleDayTexts}
      authUser={authUser}
    />
  );
}
