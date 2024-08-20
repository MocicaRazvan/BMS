"use client";

import { UseApprovedFilterTexts } from "@/components/list/useApprovedFilter";
import { UseBinaryTexts } from "@/components/list/useBinaryFilter";
import useFilterDropdown, {
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import GridList, {
  GridListTexts,
  SortingOption,
} from "@/components/list/grid-list";
import { SortingOptionsTexts } from "@/lib/constants";
import { WithUser } from "@/lib/user";
import { dietTypes } from "@/types/forms";
import Heading from "@/components/common/heading";
import { PlanResponse, ResponseWithUserDtoEntity } from "@/types/dto";
import { useRouter } from "@/navigation";
import { useCallback, useState } from "react";
import { useFormatter } from "next-intl";
import { cn } from "@/lib/utils";
import AddToCartBtn, {
  AddToCartBtnTexts,
} from "@/components/plans/add-to-cart-btn";
import { useSubscription } from "@/context/subscriptions-context";
import { useSearchParams } from "next/navigation";
import { Checkbox } from "@/components/ui/checkbox";

export interface ApprovedPlansTexts {
  gridListTexts: GridListTexts;
  useApprovedFilterTexts: UseApprovedFilterTexts;
  displayFilterTexts: UseBinaryTexts;
  dietDropdownTexts: UseFilterDropdownTexts;
  sortingPlansSortingOptions: SortingOptionsTexts;
  title: string;
  header: string;
  addToCartBtnTexts: AddToCartBtnTexts;
  buyableLabel: string;
}

interface Props extends WithUser, ApprovedPlansTexts {
  options: SortingOption[];
}

export default function PlanApprovedPageContent({
  header,
  sortingPlansSortingOptions,
  options,
  title,
  displayFilterTexts,
  useApprovedFilterTexts,
  gridListTexts,
  dietDropdownTexts,
  authUser,
  addToCartBtnTexts,
  buyableLabel,
}: Props) {
  const router = useRouter();
  const formatIntl = useFormatter();
  const { getSubscriptionPlanIds } = useSubscription();
  const currentSearchParams = useSearchParams();

  const initialBuyable = currentSearchParams.get("buyable");
  const [buyable, setBuyable] = useState(initialBuyable === "true");

  const {
    value: dietType,
    fieldDropdownFilterQueryParam: dietTypeQP,
    updateFieldDropdownFilter: updateDietType,
    filedFilterCriteriaCallback: dietTypeCriteriaCallback,
  } = useFilterDropdown({
    items: dietTypes.map((value) => ({
      value,
      label: dietDropdownTexts.labels[value],
    })),
    fieldKey: "type",
    noFilterLabel: dietDropdownTexts.noFilterLabel,
  });

  const extraContent = useCallback(
    ({ model: { content } }: ResponseWithUserDtoEntity<PlanResponse>) => (
      <div className="mt-10">
        <AddToCartBtn
          authUser={authUser}
          plan={content}
          {...addToCartBtnTexts}
        />
      </div>
    ),
    [authUser, addToCartBtnTexts],
  );

  const extraHeader = useCallback(
    ({
      model: {
        content: { price },
      },
    }: ResponseWithUserDtoEntity<PlanResponse>) => (
      <span className="font-bold">
        {formatIntl.number(price, {
          style: "currency",
          currency: "EUR",
          maximumFractionDigits: 2,
        })}
      </span>
    ),
    [formatIntl],
  );

  const imageOverlay = useCallback(
    (item: ResponseWithUserDtoEntity<PlanResponse>) => {
      const colorMap = {
        VEGAN: "success",
        OMNIVORE: "secondary",
        VEGETARIAN: "accent",
      };
      return (
        <div
          className={cn(
            ` absolute top-2 right-2 px-3 py-1 bg-${colorMap[item.model.content.type]} text-${colorMap[item.model.content.type]}-foreground rounded-full font-bold w-fit text-lg
            
            `,
          )}
        >
          {item.model.content.type}
        </div>
      );
    },
    [],
  );
  return (
    <section className="w-full min-h-[calc(100vh-4rem)] transition-all py-5 px-4 max-w-[1300px] mx-auto ">
      <Heading title={title} header={header} />
      <GridList<PlanResponse>
        onItemClick={({
          model: {
            content: { id },
          },
        }) => {
          router.push(`/plans/single/${id}`);
        }}
        sizeOptions={[6, 12, 18]}
        path="/plans/filtered/withUser"
        sortingOptions={options}
        extraUpdateSearchParams={(p) => {
          if (buyable) {
            p.set("buyable", "true");
          } else {
            p.delete("buyable");
          }
          updateDietType(p);
        }}
        // extraCriteria={extraCriteria}
        extraQueryParams={{
          approved: "true",
          display: "true",
          ...(dietType ? { type: dietType } : {}),
        }}
        extraArrayQueryParam={{
          ...(buyable
            ? {
                excludeIds: getSubscriptionPlanIds().map((id) => id.toString()),
              }
            : {}),
        }}
        {...gridListTexts}
        extraCriteriaWithCallBack={(callback) => {
          return (
            <div className="flex items-center justify-center flex-wrap gap-10">
              {dietTypeCriteriaCallback(callback)}
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="buyable"
                  onCheckedChange={(checked) => {
                    setBuyable(checked === true);
                    callback();
                  }}
                />
                <label
                  htmlFor="buyable"
                  className=" font-medium leading-none capitalize"
                >
                  {buyableLabel}
                </label>
              </div>
            </div>
          );
        }}
        passExtraHeader={extraHeader}
        passExtraImageOverlay={imageOverlay}
        passExtraContent={extraContent}
      />
    </section>
  );
}
