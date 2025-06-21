"use client";

import { UseApprovedFilterTexts } from "@/components/list/useApprovedFilter";
import { UseBinaryTexts } from "@/components/list/useBinaryFilter";
import useFilterDropdown, {
  DropDownFieldFilterCriteriaCallback,
  FilterDropdownItem,
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import GridList, {
  CriteriaProps,
  GridListTexts,
  SortingOption,
} from "@/components/list/grid-list";
import { SortingOptionsTexts } from "@/types/constants";
import { dietTypes } from "@/types/forms";
import Heading from "@/components/common/heading";
import { planObjectives, PlanResponse } from "@/types/dto";
import {
  Dispatch,
  memo,
  SetStateAction,
  useCallback,
  useMemo,
  useState,
} from "react";
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
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import { useDeepCompareMemo } from "@/hoooks/use-deep-memo";

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

interface Props extends ApprovedPlansTexts {
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
  addToCartBtnTexts,
  buyableLabel,
  objectiveDropDownTexts,
}: Props) {
  const { authUser } = useAuthUserMinRole();

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

  const extraUpdateSearchParams = useCallback(
    (p: URLSearchParams) => {
      if (buyable) {
        p.set("buyable", "true");
      } else {
        p.delete("buyable");
      }
      updateDietType(p);
      updateObjectiveType(p);
    },
    [buyable, updateDietType, updateObjectiveType],
  );
  const extraQueryParams = useMemo(
    () => ({
      approved: "true",
      display: "true",
      ...(dietType ? { type: dietType } : {}),
      ...(objectiveType ? { objective: objectiveType } : {}),
    }),
    [dietType, objectiveType],
  );
  const extraArrayQueryParam = useDeepCompareMemo(
    () => ({
      ...(buyable
        ? {
            excludeIds: getSubscriptionPlanIds().map((id) => id.toString()),
          }
        : {}),
    }),
    [buyable, getSubscriptionPlanIds],
  );

  return (
    <section className="w-full min-h-[calc(100vh-4rem)] transition-all py-5 px-4 max-w-[1300px] mx-auto ">
      <Heading title={title} header={header} />
      <GridList<PlanResponse>
        itemLinkCallback={({
          model: {
            content: { id },
          },
        }) => `/plans/single/${id}`}
        path="/plans/filtered/withUser"
        sortingOptions={options}
        extraUpdateSearchParams={extraUpdateSearchParams}
        // extraCriteria={extraCriteria}
        extraQueryParams={extraQueryParams}
        extraArrayQueryParam={extraArrayQueryParam}
        {...gridListTexts}
        ExtraCriteria={(props) => (
          <PlanExtraCriteria
            {...props}
            dietNoFilterLabel={dietDropdownTexts.noFilterLabel}
            objectiveNoFilterLabel={objectiveDropDownTexts.noFilterLabel}
            buyableLabel={buyableLabel}
            setDietType={setDietType}
            dietItems={dietItems}
            setObjectiveType={setObjectiveType}
            objectiveItems={objectiveItems}
            buyable={buyable}
            setBuyable={setBuyable}
          />
        )}
        ItemExtraHeader={(props) => (
          <PlanExtraHeader {...props} formatFunction={formatIntl.number} />
        )}
        ItemImageOverlay={(props) => (
          <PlanImageOverlay
            {...props}
            objective={
              objectiveDropDownTexts.labels[props.item.model.content.objective]
            }
          />
        )}
        ItemExtraContent={(props) => (
          <PlanExtraContent
            {...props}
            authUser={authUser}
            addToCartBtnTexts={addToCartBtnTexts}
          />
        )}
      />
    </section>
  );
}

const PlanExtraCriteria = memo(
  ({
    callback,
    dietNoFilterLabel,
    objectiveNoFilterLabel,
    buyableLabel,
    buyable,
    setObjectiveType,
    setDietType,
    dietItems,
    objectiveItems,
    setBuyable,
  }: CriteriaProps & {
    dietNoFilterLabel: string;
    objectiveNoFilterLabel: string;
    buyableLabel: string;
    setDietType: Dispatch<SetStateAction<FilterDropdownItem>>;
    buyable: boolean;
    setObjectiveType: Dispatch<SetStateAction<FilterDropdownItem>>;
    dietItems: FilterDropdownItem[];
    objectiveItems: FilterDropdownItem[];
    setBuyable: Dispatch<SetStateAction<boolean>>;
  }) => {
    return (
      <div className="flex items-center justify-center flex-wrap gap-4 ml-10">
        <DropDownFieldFilterCriteriaCallback
          callback={callback}
          fieldKey={"type"}
          noFilterLabel={dietNoFilterLabel}
          setGlobalFilter={setDietType}
          items={dietItems}
        />
        <DropDownFieldFilterCriteriaCallback
          callback={callback}
          fieldKey={"objective"}
          noFilterLabel={objectiveNoFilterLabel}
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
  },
);

PlanExtraCriteria.displayName = "PlanExtraCriteria";
