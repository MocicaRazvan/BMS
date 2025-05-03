import useFetchStream from "@/hoooks/useFetchStream";
import {
  CustomEntityModel,
  DayResponse,
  MealResponse,
  RecipeResponse,
  ResponseWithChildListEntity,
} from "@/types/dto";
import { BaseError } from "@/types/responses";
import { InitialDataType } from "@/components/forms/day-form";
import { useMemo } from "react";

export function useGetDayMealsRecipes(id: string) {
  const {
    messages: day,
    error: dayError,
    isAbsoluteFinished: dayFinished,
  } = useFetchStream<CustomEntityModel<DayResponse>, BaseError>({
    path: `/days/${id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
    refetchOnFocus: false,
  });

  const {
    messages: meals,
    error: mealsError,
    isAbsoluteFinished: mealsFinished,
  } = useFetchStream<
    ResponseWithChildListEntity<MealResponse, RecipeResponse>,
    BaseError
  >({
    path: `/meals/day/recipes/${id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
    refetchOnFocus: false,
  });

  const initialData: InitialDataType = useMemo(
    () =>
      meals.reduce(
        (
          acc,
          {
            entity: {
              content: { period, recipes, id },
            },
            children,
          },
        ) => {
          const periodArr = period.split(":");
          return {
            ...acc,
            [id]: {
              period: {
                hour: parseInt(periodArr[0], 10) || 0,
                minute: parseInt(periodArr[1], 10) || 0,
              },
              recipes,
              id,
              optionRecipes: children.map(({ id, title, type }) => ({
                label: title + " - " + type,
                value: id.toString(),
                type,
              })),
            },
          };
        },
        {} as InitialDataType,
      ),
    [meals],
  );

  return {
    meals,
    mealsError,
    mealsFinished,
    day,
    dayError,
    dayFinished,
    initialData,
  };
}
