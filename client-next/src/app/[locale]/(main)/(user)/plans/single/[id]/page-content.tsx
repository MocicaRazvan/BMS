"use client";

import { useGetTitleBodyUser } from "@/hoooks/useGetTitleBodyUser";
import { PlanResponse } from "@/types/dto";
import { useFormatter } from "next-intl";
import { checkApprovePrivilege, isSuccessCheckReturn } from "@/lib/utils";
import ElementHeader, {
  ElementHeaderTexts,
} from "@/components/common/element-header";
import LoadingSpinner from "@/components/common/loading-spinner";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import ProseText from "@/components/common/prose-text";
import AuthorProfile from "@/components/common/author-profile";
import { AddToCartBtnTexts } from "@/components/plans/add-to-cart-btn";
import useClientNotFound from "@/hoooks/useClientNotFound";
import React from "react";
import { usePlansSubscription } from "@/context/subscriptions-context";
import PlanType from "@/components/plans/plan-type";
import PlanRecommendationList, {
  PlanRecommendationListTexts,
} from "@/components/recomandation/plan-recommendation-list";
import { Separator } from "@/components/ui/separator";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import PageContainer from "@/components/common/page-container";
import dynamic from "next/dynamic";

const DynamicAddToCartBtn = dynamic(
  () => import("@/components/plans/animated-add-to-cart-btn"),
  {
    ssr: false,
    loading: () => null,
  },
);

export interface UserPlanPageContentTexts {
  elementHeaderTexts: ElementHeaderTexts;
  addToCartBtnTexts: AddToCartBtnTexts;
  price: string;
  numberDays: string;
  buyPrompt: string;
  planRecommendationListTexts: PlanRecommendationListTexts;
}
interface Props extends UserPlanPageContentTexts {}

export default function UserPlanPageContent({
  elementHeaderTexts,
  price,
  addToCartBtnTexts,
  buyPrompt,
  numberDays,
  planRecommendationListTexts,
}: Props) {
  const { authUser } = useAuthUserMinRole();

  const { navigateToNotFound } = useClientNotFound();
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
  const { isPlanInSubscription, getSubscriptionPlanIds } =
    usePlansSubscription();

  const formatIntl = useFormatter();
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

  const privilegeReturn = checkApprovePrivilege(
    authUser,
    planState,
    navigateToNotFound,
  );

  if (React.isValidElement(privilegeReturn)) {
    return privilegeReturn;
  }

  if (!isSuccessCheckReturn(privilegeReturn)) {
    return navigateToNotFound();
  }

  const { isOwnerOrAdmin, isAdmin, isOwner } = privilegeReturn;

  return (
    <PageContainer>
      <div
        className="sticky top-[4rem] z-10 shadow-sm p-4 w-[200px] rounded-xl
      bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 overflow-hidden
       shadow-foreground/30 transition-all hover:scale-105
      "
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
        <div className="mt-5 flex justify-center items-center w-full gap-2">
          {numberDays}
          <span className="font-bold">{planState.days.length}</span>
        </div>
        <div className="mt-6">
          <PlanType type={plan.type} />
        </div>
      </div>
      <div className="md:mt-[-5rem] mt-3" />
      <ElementHeader
        elementState={planState}
        isLiked={isLiked}
        isDisliked={isDisliked}
        likesDisabled={true}
        {...elementHeaderTexts}
      />
      {plan?.images.length > 0 && (
        <div className="mt-10">
          <CustomImageCarousel images={plan?.images} />
        </div>
      )}
      <div className="mt-20 px-14">
        <ProseText html={plan?.body} />
        <AuthorProfile author={user} />
      </div>
      {!isPlanInSubscription(plan.id) && (
        <div className={"w-full flex items-center justify-center mt-20 "}>
          <h2 className="text-3xl md:text-4xl font-bold tracking-tighter">
            {buyPrompt}
          </h2>
        </div>
      )}
      <Separator className="my-5 md:my-10 md:mt-12" />
      <div>
        <PlanRecommendationList
          id={plan?.id}
          {...planRecommendationListTexts}
        />
      </div>
      <DynamicAddToCartBtn
        plan={plan}
        authUser={authUser}
        addToCartBtnTexts={addToCartBtnTexts}
      />
    </PageContainer>
  );
}
