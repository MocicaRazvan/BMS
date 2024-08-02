"use client";

import { WithUser } from "@/lib/user";
import { useGetTitleBodyUser } from "@/hoooks/useGetTitleBodyUser";
import { PlanResponse } from "@/types/dto";
import { useFormatter } from "next-intl";
import { notFound } from "next/navigation";
import { checkApprovePrivilege, cn } from "@/lib/utils";
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
export interface UserPlanPageContentTexts {
  elementHeaderTexts: ElementHeaderTexts;
  addToCartBtnTexts: AddToCartBtnTexts;
  price: string;
}
interface Props extends WithUser, UserPlanPageContentTexts {}

export default function UserPlanPageContent({
  authUser,
  elementHeaderTexts,
  price,
  addToCartBtnTexts,
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
  const { addToCartForUser, isInCartForUser, removeFromCartForUser } =
    useCartForUser(authUser.id);
  const { toast } = useToast();
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

  const { isOwnerOrAdmin, isAdmin, isOwner } = checkApprovePrivilege(
    authUser,
    planState,
  );
  const colorMap = {
    VEGAN: "success",
    OMNIVORE: "secondary",
    VEGETARIAN: "accent",
  };
  return (
    <section className="w-full mx-auto max-w-[1500px] min-h-[calc(100vh-4rem)] flex-col items-center justify-center transition-all px-6 py-10 relative ">
      <div
        className="sticky top-[4rem] z-10 shadow-sm p-4 w-[160px] rounded-xl
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
      <div className="sticky bottom-0 mt-10  w-fit mx-auto  bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/70 p-2 rounded-md">
        <AddToCartBtn authUser={authUser} plan={plan} {...addToCartBtnTexts} />
      </div>
    </section>
  );
}
