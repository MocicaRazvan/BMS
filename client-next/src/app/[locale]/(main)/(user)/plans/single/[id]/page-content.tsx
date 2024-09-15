"use client";

import { WithUser } from "@/lib/user";
import { useGetTitleBodyUser } from "@/hoooks/useGetTitleBodyUser";
import { PlanResponse } from "@/types/dto";
import { useFormatter } from "next-intl";
import { checkApprovePrivilege, cn, isSuccessCheckReturn } from "@/lib/utils";
import ElementHeader, {
  ElementHeaderTexts,
} from "@/components/common/element-header";
import LoadingSpinner from "@/components/common/loading-spinner";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import ProseText from "@/components/common/prose-text";
import AuthorProfile from "@/components/common/author-profile";
import { useCartForUser } from "@/context/cart-context";
import { useToast } from "@/components/ui/use-toast";
import AddToCartBtn, {
  AddToCartBtnTexts,
} from "@/components/plans/add-to-cart-btn";
import useClientNotFound from "@/hoooks/useClientNotFound";
import React from "react";
import { useSubscription } from "@/context/subscriptions-context";
export interface UserPlanPageContentTexts {
  elementHeaderTexts: ElementHeaderTexts;
  addToCartBtnTexts: AddToCartBtnTexts;
  price: string;
  numberDays: string;
  buyPrompt: string;
}
interface Props extends WithUser, UserPlanPageContentTexts {}

export default function UserPlanPageContent({
  authUser,
  elementHeaderTexts,
  price,
  addToCartBtnTexts,
  buyPrompt,
  numberDays,
}: Props) {
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
  const { isPlanInSubscription } = useSubscription();

  const formatIntl = useFormatter();
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

  const colorMap = {
    VEGAN: "success",
    OMNIVORE: "secondary",
    VEGETARIAN: "accent",
  };
  return (
    <section className="w-full mx-auto max-w-[1500px] min-h-[calc(100vh-4rem)] flex-col items-center justify-center transition-all px-1 md:px-6 py-5 relative ">
      <div
        className="sticky top-[4rem] z-10 shadow-sm p-4 w-[200px] rounded-xl
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
        <div className="mt-5 flex justify-center items-center w-full gap-2">
          {numberDays}
          <span className="font-bold">{planState.days.length}</span>
        </div>
        <div className="mt-6">
          <p
            className={cn(
              `px-3 py-1 bg-${colorMap[plan.type]} text-${colorMap[plan.type]}-foreground rounded-full font-bold text-center`,
            )}
          >
            {plan.type}
          </p>
        </div>
      </div>
      <ElementHeader
        elementState={planState}
        isLiked={isLiked}
        isDisliked={isDisliked}
        likesDisabled={true}
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
      {!isPlanInSubscription(plan.id) && (
        <div className={"w-full flex items-center justify-center mt-20 "}>
          <h2 className="text-3xl md:text-4xl font-bold tracking-tighter">
            {buyPrompt}
          </h2>
        </div>
      )}
      <div className="sticky bottom-0 mt-4  w-fit mx-auto  bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/70 p-2 rounded-md">
        <AddToCartBtn
          authUser={authUser}
          plan={plan}
          {...addToCartBtnTexts}
          pulse
        />
      </div>
    </section>
  );
}
