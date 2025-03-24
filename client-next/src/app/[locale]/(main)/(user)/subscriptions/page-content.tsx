"use client";

import GridList, {
  GridListTexts,
  SortingOption,
} from "@/components/list/grid-list";
import { SortingOptionsTexts } from "@/lib/constants";
import useFilterDropdown, {
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import { WithUser } from "@/lib/user";
import { dietTypes } from "@/types/forms";
import { useRouter } from "@/navigation";
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
} from "@/components/dayCalendar/days-calendar-cta";

export interface SubscriptionsPageContentTexts {
  gridListTexts: GridListTexts;
  sortingPlansSortingOptions: SortingOptionsTexts;
  dietDropdownTexts: UseFilterDropdownTexts;
  objectiveDropDownTexts: UseFilterDropdownTexts;
  title: string;
  header: string;
  dayCalendarCTATexts: DaysCalendarCTATexts;
}
interface Props extends WithUser, SubscriptionsPageContentTexts {
  options: SortingOption[];
}

export default function SubscriptionsPageContent({
  header,
  sortingPlansSortingOptions,
  options,
  title,
  gridListTexts,
  dietDropdownTexts,
  authUser,
  objectiveDropDownTexts,
  dayCalendarCTATexts,
}: Props) {
  const router = useRouter();
  const formatIntl = useFormatter();

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
  const {
    value: objectiveType,
    updateFieldDropdownFilter: updateObjectiveType,
    filedFilterCriteriaCallback: objectiveTypeCriteriaCallback,
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
        onItemClick={({
          model: {
            content: { id },
          },
        }) => {
          router.push(`/subscriptions/single/${id}`);
        }}
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
          <div className="flex items-center justify-center flex-wrap gap-10">
            {dietTypeCriteriaCallback(callback)}
            {objectiveTypeCriteriaCallback(callback)}
          </div>
        )}
        passExtraImageOverlay={imageOverlay}
      />
    </section>
  );
}
