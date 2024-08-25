"use client";

import { WithUser } from "@/lib/user";
import useGetDaysWithMeals from "@/hoooks/days/useGetDayWithMeals";
import useClientNotFound from "@/hoooks/useClientNotFound";
import LoadingSpinner from "@/components/common/loading-spinner";
import React from "react";
import { checkOwnerOrAdmin, cn, isSuccessCheckReturn } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";
import LikesDislikes from "@/components/common/likes-dislikes";
import ProseText from "@/components/common/prose-text";
import AuthorProfile from "@/components/common/author-profile";
import MealsList from "@/components/days/meals-list";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { IngredientPieChartTexts } from "@/components/charts/ingredient-macros-pie-chart";
import SingleDay, { SingleDayTexts } from "@/components/days/single-day";

export interface SingleDayPageTexts {
  singleDayTexts: SingleDayTexts;
}

interface Props extends WithUser, SingleDayPageTexts {
  id: string;
}
export default function SingleDayTrainerPageContent({
  authUser,
  id,
  singleDayTexts,
}: Props) {
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
  } = useGetDaysWithMeals(id, authUser);
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
