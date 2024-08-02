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
import { PlanResponse, ResponseWithUserDtoEntity } from "@/types/dto";
import { cn } from "@/lib/utils";
import Heading from "@/components/common/heading";
import { useFormatter } from "next-intl";

export interface SubscriptionsPageContentTexts {
  gridListTexts: GridListTexts;
  sortingPlansSortingOptions: SortingOptionsTexts;
  dietDropdownTexts: UseFilterDropdownTexts;
  title: string;
  header: string;
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

  return (
    <section className="w-full min-h-[calc(100vh-4rem)] transition-all py-5 px-4 max-w-[1300px] mx-auto ">
      <Heading title={title} header={header} />
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
        extraUpdateSearchParams={updateDietType}
        // extraCriteria={extraCriteria}
        extraQueryParams={{
          ...(dietType ? { type: dietType } : {}),
        }}
        {...gridListTexts}
        passExtraHeader={extraHeader}
        extraCriteriaWithCallBack={dietTypeCriteriaCallback}
        passExtraImageOverlay={imageOverlay}
      />
    </section>
  );
}
