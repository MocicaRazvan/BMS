"use client";

import { useGetTitleBodyUser } from "@/hoooks/useGetTitleBodyUser";
import { CustomEntityModel, PlanResponse } from "@/types/dto";
import LoadingSpinner from "@/components/common/loading-spinner";
import { checkOwnerOrAdmin, cn, isSuccessCheckReturn } from "@/lib/utils";
import { ElementHeaderTexts } from "@/components/common/element-header";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { IngredientPieChartTexts } from "@/components/charts/ingredient-macros-pie-chart";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import AuthorProfile from "@/components/common/author-profile";
import { useFormatter } from "next-intl";
import useClientNotFound from "@/hoooks/useClientNotFound";
import React, { useCallback } from "react";
import DaysList, { DaysListTexts } from "@/components/days/days-list";
import { SingleDayTexts } from "@/components/days/single-day";
import LikesDislikes from "@/components/common/likes-dislikes";
import DietBadge from "@/components/common/diet-badge";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { AnswerFromBodyFormTexts } from "@/components/forms/answer-from-body-form";
import ItemBodyQa from "@/components/common/item-body-qa";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import PageContainer from "@/components/common/page-container";

export interface SingleTrainerPlanPageTexts {
  elementHeaderTexts: ElementHeaderTexts;
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  price: string;
  displayed: string;
  notDisplayed: string;
  singleDayTexts: SingleDayTexts;
  daysListTexts: DaysListTexts;
  answerFromBodyFormTexts: AnswerFromBodyFormTexts;
}

interface Props extends SingleTrainerPlanPageTexts {
  id: string;
}

export default function SingleTrainerPlanPageContent({
  id,

  elementHeaderTexts,
  nutritionalTableTexts,
  ingredientPieChartTexts,
  notDisplayed,
  displayed,
  price,
  singleDayTexts,
  daysListTexts,
  answerFromBodyFormTexts,
}: Props) {
  const { authUser } = useAuthUserMinRole();

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
    basePath: `/plans/withUser`,
  });
  const { navigateToNotFound } = useClientNotFound();
  const formatIntl = useFormatter();

  const react = useCallback(
    async (type: "like" | "dislike") => {
      if (!planState?.approved) return;
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
                userDislikes: newPlan.userDislikes,
                userLikes: newPlan.userLikes,
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
    console.log("loading main");
    return (
      <section className="w-full min-h-[calc(100vh-4rem)] flex items-center justify-center">
        <LoadingSpinner />
      </section>
    );
  }

  const ownerReturn = checkOwnerOrAdmin(authUser, plan, navigateToNotFound);

  if (React.isValidElement(ownerReturn)) {
    return ownerReturn;
  }

  if (!isSuccessCheckReturn(ownerReturn)) {
    return navigateToNotFound();
  }

  const { isOwnerOrAdmin, isAdmin, isOwner } = ownerReturn;

  return (
    <PageContainer>
      <div
        className="sticky top-[4rem] z-10 shadow-sm p-4 w-[130px] rounded-xl
      bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 overflow-hidden
       shadow-foreground/30 transition-all hover:scale-105"
      >
        <div className="flex justify-center items-center w-full gap-2">
          <span>{price} </span>
          <span className="font-bold">
            {formatIntl.number(plan.price, {
              style: "currency",
              currency: "EUR",
              maximumFractionDigits: 2,
            })}
          </span>
        </div>
      </div>
      <div className="md:mt-[-5rem] mt-1.5" />
      <div className="w-3/4 mx-auto flex flex-col md:flex-row items-center justify-between gap-10 md:gap-20 mb-2 ">
        <div className="order-1 flex items-center justify-center gap-3">
          <div className="flex flex-row md:flex-col items-center justify-center gap-4 flex-1">
            <div className="text-xl lg:text-2xl font-bold tracking-tight">
              {!plan?.approved ? (
                elementHeaderTexts.notApproved
              ) : plan.display ? (
                <p className="text-success">{displayed}</p>
              ) : (
                <p>{notDisplayed}</p>
              )}
            </div>
            <div className="flex items-center justify-center gap-4">
              <LikesDislikes
                likes={planState.userLikes}
                dislikes={planState.userDislikes}
                isLiked={isLiked || false}
                isDisliked={isDisliked || false}
                react={react}
                disabled={!plan?.approved}
              />
            </div>
          </div>
        </div>
        <div className=" flex items-center justify-center order-0 md:order-1 flex-1 ">
          <h1
            className={cn(
              "text-2xl md:text-4xl text-balance tracking-tighter font-bold text-center  ",
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
          html={planState.body}
          formProps={{ body: planState.body, texts: answerFromBodyFormTexts }}
        />
        <AuthorProfile author={user} />
      </div>
      <div className="mt-20">
        <DaysList
          authUser={authUser}
          dayIds={planState.days}
          texts={singleDayTexts}
          {...daysListTexts}
        />
      </div>
    </PageContainer>
  );
}
