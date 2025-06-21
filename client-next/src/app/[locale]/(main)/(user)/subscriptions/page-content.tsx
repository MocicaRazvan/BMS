"use client";

import GridList, {
  CriteriaProps,
  GridListTexts,
  SortingOption,
} from "@/components/list/grid-list";
import { SortingOptionsTexts } from "@/types/constants";
import useFilterDropdown, {
  DropDownFieldFilterCriteriaCallback,
  FilterDropdownItem,
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import { dietTypes } from "@/types/forms";
import { Dispatch, memo, SetStateAction, useCallback, useMemo } from "react";
import { planObjectives, PlanResponse } from "@/types/dto";
import Heading from "@/components/common/heading";
import { useFormatter } from "next-intl";
import {
  PlanExtraHeader,
  PlanImageOverlay,
} from "@/components/plans/plan-list-extra";
import DaysCalendarCTA, {
  DaysCalendarCTATexts,
} from "@/components/days-calendar/days-calendar-cta";

export interface SubscriptionsPageContentTexts {
  gridListTexts: GridListTexts;
  sortingPlansSortingOptions: SortingOptionsTexts;
  dietDropdownTexts: UseFilterDropdownTexts;
  objectiveDropDownTexts: UseFilterDropdownTexts;
  title: string;
  header: string;
  dayCalendarCTATexts: DaysCalendarCTATexts;
}
interface Props extends SubscriptionsPageContentTexts {
  options: SortingOption[];
}

export default function SubscriptionsPageContent({
  header,
  sortingPlansSortingOptions,
  options,
  title,
  gridListTexts,
  dietDropdownTexts,
  objectiveDropDownTexts,
  dayCalendarCTATexts,
}: Props) {
  const formatIntl = useFormatter();

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
      updateDietType(p);
      updateObjectiveType(p);
    },
    [updateDietType, updateObjectiveType],
  );
  const finalExtraQueryParams = useMemo(
    () => ({
      ...(dietType ? { type: dietType } : {}),
      ...(objectiveType ? { objective: objectiveType } : {}),
    }),
    [dietType, objectiveType],
  );

  return (
    <section className="w-full min-h-[calc(100vh-4rem)] transition-all py-5 px-4 max-w-[1300px] mx-auto ">
      <Heading title={title} header={header} />
      <DaysCalendarCTA {...dayCalendarCTATexts} />
      <GridList<PlanResponse>
        itemLinkCallback={({
          model: {
            content: { id },
          },
        }) => `/subscriptions/single/${id}`}
        path="/orders/subscriptions/filtered/withUser"
        sortingOptions={options}
        extraUpdateSearchParams={extraUpdateSearchParams}
        // extraCriteria={extraCriteria}
        extraQueryParams={finalExtraQueryParams}
        {...gridListTexts}
        ItemExtraHeader={(props) => (
          <PlanExtraHeader {...props} formatFunction={formatIntl.number} />
        )}
        ExtraCriteria={(props) => (
          <PlanExtraCriteria
            {...props}
            dietNoFilterLabel={dietDropdownTexts.noFilterLabel}
            objectiveNoFilterLabel={objectiveDropDownTexts.noFilterLabel}
            setDietType={setDietType}
            dietItems={dietItems}
            setObjectiveType={setObjectiveType}
            objectiveItems={objectiveItems}
          />
        )}
        ItemImageOverlay={(props) => (
          <PlanImageOverlay
            {...props}
            objective={
              objectiveDropDownTexts.labels[props.item.model.content.objective]
            }
          />
        )}
      />
    </section>
  );
}

const PlanExtraCriteria = memo(
  ({
    callback,
    setObjectiveType,
    setDietType,
    dietItems,
    objectiveItems,
    dietNoFilterLabel,
    objectiveNoFilterLabel,
  }: CriteriaProps & {
    dietNoFilterLabel: string;
    objectiveNoFilterLabel: string;
    setDietType: Dispatch<SetStateAction<FilterDropdownItem>>;
    setObjectiveType: Dispatch<SetStateAction<FilterDropdownItem>>;
    dietItems: FilterDropdownItem[];
    objectiveItems: FilterDropdownItem[];
  }) => {
    console.log("PlanExtraCriteria rendered");
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
      </div>
    );
  },
);

PlanExtraCriteria.displayName = "PlanExtraCriteria";
