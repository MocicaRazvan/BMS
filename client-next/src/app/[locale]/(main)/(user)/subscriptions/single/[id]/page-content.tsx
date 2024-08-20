"use client";

import ElementHeader, {
  ElementHeaderTexts,
} from "@/components/common/element-header";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { IngredientPieChartTexts } from "@/components/charts/ingredient-macros-pie-chart";
import { WithUser } from "@/lib/user";
import { useGetTitleBodyUser } from "@/hoooks/useGetTitleBodyUser";
import { CustomEntityModel, PlanResponse } from "@/types/dto";
import { useFormatter } from "next-intl";
import LoadingSpinner from "@/components/common/loading-spinner";
import { useCallback } from "react";
import { fetchStream } from "@/hoooks/fetchStream";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import ProseText from "@/components/common/prose-text";
import AuthorProfile from "@/components/common/author-profile";
import { RecipePlanList } from "@/components/plans/plan-recipes";
import useClientNotFound from "@/hoooks/useClientNotFound";

export interface SingleSubscriptionTexts {
  elementHeaderTexts: ElementHeaderTexts;
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
}

interface Props extends WithUser, SingleSubscriptionTexts {
  id: string;
}

export default function SingleSubscriptionPageContent({
  id,
  authUser,
  elementHeaderTexts,
  nutritionalTableTexts,
  ingredientPieChartTexts,
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
        console.log(resp);
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
    console.log("loading main");
    return (
      <section className="w-full min-h-[calc(100vh-4rem)] flex items-center justify-center transition-all">
        <LoadingSpinner />
      </section>
    );
  }
  const colorMap = {
    VEGAN: "success",
    OMNIVORE: "secondary",
    VEGETARIAN: "accent",
  };
  return (
    <section className="w-full mx-auto max-w-[1500px] min-h-[calc(100vh-4rem)] flex-col items-center justify-center transition-all px-6 py-10 relative ">
      <ElementHeader
        elementState={planState}
        react={react}
        isLiked={isLiked}
        isDisliked={isDisliked}
        likesDisabled={!planState.approved}
        {...elementHeaderTexts}
      />{" "}
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
        <RecipePlanList
          authUser={authUser}
          recipeIds={planState.recipes}
          ingredientPieChartTexts={ingredientPieChartTexts}
          nutritionalTableTexts={nutritionalTableTexts}
          recipeBasePath={`/orders/subscriptions/recipes/${planState.id}`}
          showLikes
        />
      </div>
    </section>
  );
}
