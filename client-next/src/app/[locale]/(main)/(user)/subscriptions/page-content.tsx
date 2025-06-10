"use client";

import GridList, {
  GridListTexts,
  SortingOption,
} from "@/components/list/grid-list";
import { SortingOptionsTexts } from "@/types/constants";
import useFilterDropdown, {
  DropDownFieldFilterCriteriaCallback,
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import { dietTypes } from "@/types/forms";
import { useCallback } from "react";
import {
  planObjectives,
  PlanResponse,
  ResponseWithUserDtoEntity,
} from "@/types/dto";
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

  const imageOverlay = useCallback(
    (item: ResponseWithUserDtoEntity<PlanResponse>) =>
      PlanImageOverlay(
        item,
        objectiveDropDownTexts.labels[item.model.content.objective],
      ),
    [objectiveDropDownTexts.labels],
  );

  const extraHeader = useCallback(
    (item: ResponseWithUserDtoEntity<PlanResponse>) =>
      PlanExtraHeader(item, formatIntl.number),
    [formatIntl.number],
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
        sizeOptions={[6, 12, 18]}
        path="/orders/subscriptions/filtered/withUser"
        sortingOptions={options}
        extraUpdateSearchParams={(p) => {
          updateDietType(p);
          updateObjectiveType(p);
        }}
        // extraCriteria={extraCriteria}
        extraQueryParams={{
          ...(dietType ? { type: dietType } : {}),
          ...(objectiveType ? { objective: objectiveType } : {}),
        }}
        {...gridListTexts}
        passExtraHeader={extraHeader}
        extraCriteriaWithCallBack={(callback) => (
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
          </div>
        )}
        passExtraImageOverlay={imageOverlay}
      />
    </section>
  );
}
