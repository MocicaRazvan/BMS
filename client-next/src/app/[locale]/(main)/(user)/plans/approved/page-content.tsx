"use client";

import { UseApprovedFilterTexts } from "@/components/list/useApprovedFilter";
import { UseBinaryTexts } from "@/components/list/useBinaryFilter";
import useFilterDropdown, {
  DropDownFieldFilterCriteriaCallback,
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
import {
  planObjectives,
  PlanResponse,
  ResponseWithUserDtoEntity,
} from "@/types/dto";
import { useRouter } from "@/navigation";
import { useCallback, useState } from "react";
import { useFormatter } from "next-intl";
import { AddToCartBtnTexts } from "@/components/plans/add-to-cart-btn";
import { usePlansSubscription } from "@/context/subscriptions-context";
import { useSearchParams } from "next/navigation";
import { Checkbox } from "@/components/ui/checkbox";
import {
  PlanExtraContent,
  PlanExtraHeader,
  PlanImageOverlay,
} from "@/components/plans/plan-list-extra";

export interface ApprovedPlansTexts {
  gridListTexts: GridListTexts;
  useApprovedFilterTexts: UseApprovedFilterTexts;
  displayFilterTexts: UseBinaryTexts;
  dietDropdownTexts: UseFilterDropdownTexts;
  objectiveDropDownTexts: UseFilterDropdownTexts;
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
  objectiveDropDownTexts,
}: Props) {
  const router = useRouter();
  const formatIntl = useFormatter();
  const { getSubscriptionPlanIds } = usePlansSubscription();
  const currentSearchParams = useSearchParams();

  const initialBuyable = currentSearchParams.get("buyable");
  const [buyable, setBuyable] = useState(initialBuyable === "true");

  const {
    value: dietType,
    updateFieldDropdownFilter: updateDietType,
    items: dietItems,
    setField: setDietType,
  } = useFilterDropdown({
    items: dietTypes.map((value) => ({
      value,
      label: dietDropdownTexts.labels[value],
    })),
    fieldKey: "type",
    noFilterLabel: dietDropdownTexts.noFilterLabel,
  });

  const {
    value: objectiveType,
    updateFieldDropdownFilter: updateObjectiveType,
    items: objectiveItems,
    setField: setObjectiveType,
  } = useFilterDropdown({
    items: planObjectives.map((value) => ({
      value,
      label: objectiveDropDownTexts.labels[value],
    })),
    fieldKey: "objective",
    noFilterLabel: objectiveDropDownTexts.noFilterLabel,
  });

  const extraContent = useCallback(
    (item: ResponseWithUserDtoEntity<PlanResponse>) =>
      PlanExtraContent(item, authUser, addToCartBtnTexts),
    [addToCartBtnTexts, authUser],
  );

  const extraHeader = useCallback(
    (item: ResponseWithUserDtoEntity<PlanResponse>) =>
      PlanExtraHeader(item, formatIntl.number),
    [formatIntl.number],
  );

  const imageOverlay = useCallback(
    (item: ResponseWithUserDtoEntity<PlanResponse>) =>
      PlanImageOverlay(
        item,
        objectiveDropDownTexts.labels[item.model.content.objective],
      ),
    [objectiveDropDownTexts.labels],
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
          updateObjectiveType(p);
        }}
        // extraCriteria={extraCriteria}
        extraQueryParams={{
          approved: "true",
          display: "true",
          ...(dietType ? { type: dietType } : {}),
          ...(objectiveType ? { objective: objectiveType } : {}),
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
            <div className="flex items-center justify-center flex-wrap gap-4 ml-10">
              <DropDownFieldFilterCriteriaCallback
                callback={callback}
                fieldKey={"type"}
                noFilterLabel={dietDropdownTexts.noFilterLabel}
                setGlobalFilter={setDietType}
                items={dietItems}
              />
              <DropDownFieldFilterCriteriaCallback
                callback={callback}
                fieldKey={"objective"}
                noFilterLabel={objectiveDropDownTexts.noFilterLabel}
                setGlobalFilter={setObjectiveType}
                items={objectiveItems}
              />
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="buyable"
                  checked={buyable}
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
