import useFetchStream from "@/lib/fetchers/useFetchStream";
import { CustomEntityModel, DayResponse, PlanResponse } from "@/types/dto";
import { BaseError } from "@/types/responses";
import { Option } from "@/components/ui/multiple-selector";
import { useMemo } from "react";

export function useGetPlanWithDays(id: string) {
  const {
    messages: plan,
    error: planError,
    isAbsoluteFinished: planFinished,
  } = useFetchStream<CustomEntityModel<PlanResponse>, BaseError>({
    path: `/plans/${id}`,
    method: "GET",
    authToken: true,
    refetchOnFocus: false,
  });

  const {
    messages: days,
    error: dayError,
    isAbsoluteFinished: dayFinished,
  } = useFetchStream<DayResponse, BaseError>({
    path: `/plans/days/${id}`,
    method: "GET",
    authToken: true,
    refetchOnFocus: false,
  });

  const initialOptions: (Option & { dragId: string })[] = useMemo(
    () =>
      days.length == 0
        ? []
        : days.map(({ id, title }, i) => ({
            label: title,
            value: id.toString(),
            dragId: id + "_" + i,
          })),
    [days],
  );

  return {
    plan,
    planError,
    planFinished,
    days,
    dayError,
    dayFinished,
    initialOptions,
  };
}
