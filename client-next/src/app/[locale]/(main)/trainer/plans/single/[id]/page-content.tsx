"use client";

import { WithUser } from "@/lib/user";
import { useGetTitleBodyUser } from "@/hoooks/useGetTitleBodyUser";
import { PlanResponse } from "@/types/dto";
import { notFound } from "next/navigation";
import LoadingSpinner from "@/components/common/loading-spinner";
import { checkOwnerOrAdmin, cn } from "@/lib/utils";
import { ElementHeaderTexts } from "@/components/common/element-header";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { IngredientPieChartTexts } from "@/components/charts/ingredient-macros-pie-chart";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import ProseText from "@/components/common/prose-text";
import AuthorProfile from "@/components/common/author-profile";
import { useFormatter } from "next-intl";
import { RecipePlanList } from "@/components/plans/plan-recipes";

export interface SingleTrainerPlanPageTexts {
  elementHeaderTexts: ElementHeaderTexts;
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  price: string;
  displayed: string;
  notDisplayed: string;
}

interface Props extends WithUser, SingleTrainerPlanPageTexts {
  id: string;
}

export default function SingleTrainerPlanPageContent({
  id,
  authUser,
  elementHeaderTexts,
  nutritionalTableTexts,
  ingredientPieChartTexts,
  notDisplayed,
  displayed,
  price,
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
    basePath: `/plans/withUser`,
  });
  const formatIntl = useFormatter();

  if (error?.status) {
    notFound();
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

  const { isOwner, isAdmin } = checkOwnerOrAdmin(authUser, plan);
  return (
    <section className="w-full max-w-[1500px] min-h-[calc(100vh-4rem)] flex-col items-center justify-center transition-all px-6 py-10 relative ">
      <div
        className="sticky top-[4rem] z-10 shadow-sm p-4 w-[130px] rounded-xl
      bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 overflow-hidden
       shadow-foreground/30 transition-all hover:scale-105
      "
      >
        <div className="flex justify-center items-center w-full gap-2">
          <span>{price} </span>
          <span className="font-bold">
            {" "}
            {formatIntl.number(plan.price, {
              style: "currency",
              currency: "EUR",
              maximumFractionDigits: 2,
            })}
          </span>
        </div>
      </div>

      <div className="w-3/4 mx-auto flex flex-col md:flex-row items-center justify-between gap-4 md:gap-20 mb-2 ">
        <h2
          className={cn(
            "text-2xl font-bold text-center md:text-start text-destructive w-[250px] order-1 md:order-0",
          )}
        >
          {!plan?.approved ? (
            elementHeaderTexts.notApproved
          ) : plan.display ? (
            <p className="text-success">{displayed}</p>
          ) : (
            <p>{notDisplayed}</p>
          )}
        </h2>

        <div className=" flex items-center justify-center order-0 md:order-1">
          <h1
            className={cn(
              "text-6xl tracking-tighter font-bold text-center md:translate-x-[-125px] ",
            )}
          >
            {plan?.title}
          </h1>
        </div>
        <div className="order-3">
          <p
            className={cn(
              `px-3 py-1 bg-${colorMap[plan.type]} text-${colorMap[plan.type]}-foreground rounded-full font-bold`,
            )}
          >
            {plan.type}
          </p>
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
        <RecipePlanList
          authUser={authUser}
          recipeIds={planState.recipes}
          ingredientPieChartTexts={ingredientPieChartTexts}
          nutritionalTableTexts={nutritionalTableTexts}
        />
      </div>
    </section>
  );
}
