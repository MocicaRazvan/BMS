"use client";

import { ElementHeaderTexts } from "@/components/common/element-header";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { IngredientPieChartTexts } from "@/components/charts/ingredient-macros-pie-chart";
import { WithUser } from "@/lib/user";
import { useGetTitleBodyUser } from "@/hoooks/useGetTitleBodyUser";
import { CustomEntityModel, PlanResponse } from "@/types/dto";
import LoadingSpinner from "@/components/common/loading-spinner";
import React, { useCallback } from "react";
import { fetchStream } from "@/hoooks/fetchStream";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import ProseText from "@/components/common/prose-text";
import AuthorProfile from "@/components/common/author-profile";
import useClientNotFound from "@/hoooks/useClientNotFound";
import LikesDislikes from "@/components/common/likes-dislikes";
import { cn } from "@/lib/utils";
import DietBadge from "@/components/common/diet-badge";
import DaysList, { DaysListTexts } from "@/components/days/days-list";
import { SingleDayTexts } from "@/components/days/single-day";

export interface SingleSubscriptionTexts {
  elementHeaderTexts: ElementHeaderTexts;
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  singleDayTexts: SingleDayTexts;
  daysListTexts: DaysListTexts;
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

  if (error?.status) {
    return navigateToNotFound();
  }
  if (!isFinished || !planState) {
    return (
      <section className="w-full min-h-[calc(100vh-4rem)] flex items-center justify-center transition-all">
        <LoadingSpinner />
      </section>
    );
  }

  return (
    <section className="w-full mx-auto max-w-[1500px] min-h-[calc(100vh-4rem)] flex-col items-center justify-center transition-all px-1 md:px-6 py-10 relative ">
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
        <ProseText html={plan?.body} />
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
        />
      </div>
    </section>
  );
}
