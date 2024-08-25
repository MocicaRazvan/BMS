import { PlanResponse, ResponseWithUserDtoEntity } from "@/types/dto";
import { cn } from "@/lib/utils";
import AddToCartBtn, {
  AddToCartBtnTexts,
} from "@/components/plans/add-to-cart-btn";
import { Session } from "next-auth";
import { NumberFormatOptions } from "use-intl";

export const PlanImageOverlay = (
  item: ResponseWithUserDtoEntity<PlanResponse>,
  objective: string,
) => {
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
            p-1
            `,
        )}
      >
        {item.model.content.type}
      </div>
    </div>
  );
};

export const PlanExtraContent = (
  { model: { content } }: ResponseWithUserDtoEntity<PlanResponse>,
  authUser: NonNullable<Session["user"]>,
  addToCartBtnTexts: AddToCartBtnTexts,
) => (
  <div className="mt-10">
    <AddToCartBtn authUser={authUser} plan={content} {...addToCartBtnTexts} />
  </div>
);

export const PlanExtraHeader = (
  {
    model: {
      content: { price },
    },
  }: ResponseWithUserDtoEntity<PlanResponse>,
  formatFunction: (
    value: number | bigint,
    formatOrOptions?: string | NumberFormatOptions,
  ) => string,
) => (
  <span className="font-bold">
    {formatFunction(price, {
      style: "currency",
      currency: "EUR",
      maximumFractionDigits: 2,
    })}
  </span>
);
