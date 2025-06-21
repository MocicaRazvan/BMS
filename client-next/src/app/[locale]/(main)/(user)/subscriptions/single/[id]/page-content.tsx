"use client";

import { ElementHeaderTexts } from "@/components/common/element-header";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { IngredientPieChartTexts } from "@/components/charts/ingredient-macros-pie-chart";
import { WithUser } from "@/lib/user";
import { useGetTitleBodyUser } from "@/hoooks/useGetTitleBodyUser";
import {
  CustomEntityModel,
  DayCalendarUserDates,
  PlanResponse,
} from "@/types/dto";
import LoadingSpinner from "@/components/common/loading-spinner";
import React, { useCallback, useMemo } from "react";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import AuthorProfile from "@/components/common/author-profile";
import useClientNotFound from "@/hoooks/useClientNotFound";
import LikesDislikes from "@/components/common/likes-dislikes";
import { cn } from "@/lib/utils";
import DietBadge from "@/components/common/diet-badge";
import DaysList, { DaysListTexts } from "@/components/days/days-list";
import { SingleDayTexts } from "@/components/days/single-day";
import PlanRecommendationList, {
  PlanRecommendationListTexts,
} from "@/components/recomandation/plan-recommendation-list";
import { Separator } from "@/components/ui/separator";
import { AnswerFromBodyFormTexts } from "@/components/forms/answer-from-body-form";
import ItemBodyQa from "@/components/common/item-body-qa";
import AddDayToCalendar, {
  AddDayToCalendarTexts,
} from "@/components/forms/add-day-to-calendar";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import PageContainer from "@/components/common/page-container";

export interface SingleSubscriptionTexts {
  elementHeaderTexts: ElementHeaderTexts;
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  singleDayTexts: SingleDayTexts;
  daysListTexts: DaysListTexts;
  planRecommendationListTexts: PlanRecommendationListTexts;
  answerFromBodyFormTexts: AnswerFromBodyFormTexts;
  addDayToCalendarTexts: AddDayToCalendarTexts;
}

interface Props extends WithUser, SingleSubscriptionTexts {
  id: string;
}

export default function SingleSubscriptionPageContent({
  id,
  authUser,
  elementHeaderTexts,
  singleDayTexts,
  daysListTexts,
  planRecommendationListTexts,
  answerFromBodyFormTexts,
  addDayToCalendarTexts,
}: Props) {
  const {
    itemState: planState,
    setItemState: setPlanState,
    messages,
    error,
    item: plan,
    user,
    router,
    isFinished,
    isLiked,
    isDisliked,
  } = useGetTitleBodyUser<PlanResponse>({
    authUser,
    basePath: `/orders/subscriptions`,
  });

  const {
    messages: userDates,
    isFinished: isFinishedUserDates,
    error: userDatesError,
    refetch: refetchUserDates,
  } = useFetchStream<DayCalendarUserDates>({
    path: "/daysCalendar/userDates",
    authToken: true,
  });

  const { navigateToNotFound } = useClientNotFound();

  const react = useCallback(
    async (type: "like" | "dislike") => {
      try {
        const resp = await fetchStream<CustomEntityModel<PlanResponse>>({
          path: `/plans/${type}/${id}`,
          method: "PATCH",
          token: authUser.token,
        });
        const newPlan = resp.messages[0]?.content;
        setPlanState((prev) =>
          !prev
            ? prev
            : {
                ...prev,
                userLikes: newPlan.userLikes,
                userDislikes: newPlan.userDislikes,
              },
        );
      } catch (error) {
        console.log(error);
      }
    },
    [authUser.token, id, planState?.approved, setPlanState],
  );

  const forbiddenDates = useMemo(
    () => userDates.map((d) => d.customDate),
    [userDates],
  );

  if (error?.status) {
    return navigateToNotFound();
  }
  if (!isFinished || !planState) {
    return (
      <section className="w-full min-h-[calc(100vh-4rem)] flex items-center justify-center">
        <LoadingSpinner />
      </section>
    );
  }

  return (
    <PageContainer>
      <div className="w-3/4 mx-auto flex flex-col md:flex-row items-center justify-between gap-10 md:gap-20 mb-2 ">
        <div className="order-1 flex items-center justify-center gap-3">
          <div className="flex flex-row md:flex-col items-center justify-center gap-4 flex-1">
            <div className="flex items-center justify-center gap-4">
              <LikesDislikes
                likes={planState.userLikes}
                dislikes={planState.userDislikes}
                isLiked={isLiked || false}
                isDisliked={isDisliked || false}
                react={react}
              />
            </div>
          </div>
        </div>
        <div className=" flex items-center justify-center order-0 md:order-1 flex-1 ">
          <h1
            className={cn(
              "text-2xl md:text-4xl tracking-tighter font-bold text-center  ",
            )}
          >
            {planState.title}
          </h1>
        </div>
        <div className="order-3">
          <DietBadge dietType={planState.type} />
        </div>
      </div>

      {plan?.images.length > 0 && (
        <div className="mt-10">
          <CustomImageCarousel images={plan?.images} />
        </div>
      )}
      <div className="mt-20 px-14">
        <ItemBodyQa
          html={plan?.body}
          formProps={{ body: plan?.body, texts: answerFromBodyFormTexts }}
        />
        <AuthorProfile author={user} />
      </div>
      <div className={"mt-20"}>
        <DaysList
          authUser={authUser}
          dayIds={planState.days}
          texts={singleDayTexts}
          dayBasePath={`/orders/subscriptions/days/${id}`}
          mealsBasePath={`/orders/subscriptions/days/meals/${id}`}
          recipeBasePath={`/orders/subscriptions/recipe/${id}`}
          disableLikes={false}
          {...daysListTexts}
          subHeaderContent={(day) => (
            <div className="flex items-center justify-end mt-8 mb-5">
              {isFinishedUserDates && !userDatesError && (
                <AddDayToCalendar
                  day={day}
                  {...addDayToCalendarTexts}
                  forbiddenDates={forbiddenDates}
                  onAddDayToCalendar={refetchUserDates}
                  authUser={authUser}
                />
              )}
            </div>
          )}
        />
      </div>
      <Separator className="my-5 md:my-10 md:mt-14" />
      <div>
        <PlanRecommendationList
          id={plan?.id}
          {...planRecommendationListTexts}
        />
      </div>
    </PageContainer>
  );
}
