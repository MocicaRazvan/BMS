import { PlanResponse } from "@/types/dto";
import { cn } from "@/lib/utils";
import AddToCartBtn, {
  AddToCartBtnTexts,
} from "@/components/plans/add-to-cart-btn";
import { NumberFormatOptions } from "use-intl";
import { ThumbsUp } from "lucide-react";
import React from "react";
import { ExtraProps } from "@/components/list/item-card";
import { WithUser } from "@/lib/user";

export const PlanImageOverlay = ({
  item,
  objective,
}: ExtraProps<PlanResponse> & { objective: string }) => {
  const colorMap = {
    VEGAN: "success",
    OMNIVORE: "secondary",
    VEGETARIAN: "accent",
  };
  return (
    <div className="absolute top-2 w-full flex items-center justify-between  px-3 py-1 flex-wrap">
      <div className="text-lg font-bold bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/70 p-1 rounded-md">
        {objective}
      </div>
      <div
        className={cn(
          ` bg-${colorMap[item.model.content.type]} text-${colorMap[item.model.content.type]}-foreground rounded-full font-bold w-fit text-lg
            py-1 px-1.5`,
        )}
      >
        {item.model.content.type}
      </div>
    </div>
  );
};

export const PlanExtraContent = ({
  item: {
    model: { content },
  },
  authUser,
  addToCartBtnTexts,
}: ExtraProps<PlanResponse> &
  WithUser & {
    addToCartBtnTexts: AddToCartBtnTexts;
  }) => (
  <div className="mt-10">
    <AddToCartBtn authUser={authUser} plan={content} {...addToCartBtnTexts} />
  </div>
);

export const PlanExtraHeader = ({
  item: {
    model: {
      content: { price, userLikes },
    },
  },
  formatFunction,
}: ExtraProps<PlanResponse> & {
  formatFunction: (
    value: number | bigint,
    formatOrOptions?: string | NumberFormatOptions,
  ) => string;
}) => (
  <div className="flex items-center gap-3.5 justify-start max-w-[300px]">
    <div className="flex items-start justify-center gap-0.5 font-semibold text-success">
      <span className="mt-0.5">{userLikes.length}</span>
      <ThumbsUp className="text-success" size={20} />
    </div>
    <p className="text-sm text-muted-foreground font-semibold">
      <span className="font-bold">
        {formatFunction(price, {
          style: "currency",
          currency: "EUR",
          maximumFractionDigits: 2,
        })}
      </span>
    </p>
  </div>
);
