"use client";
import DayForm, {
  DayFormProps,
  InitialDataType,
} from "@/components/forms/day-form";
import useFetchStream from "@/hoooks/useFetchStream";
import {
  CustomEntityModel,
  DayResponse,
  MealResponse,
  RecipeResponse,
  ResponseWithChildListEntity,
} from "@/types/dto";
import { BaseError } from "@/types/responses";
import useClientNotFound from "@/hoooks/useClientNotFound";
import LoadingSpinner from "@/components/common/loading-spinner";
import React, { useMemo } from "react";
import { checkOwner } from "@/lib/utils";

interface Props extends DayFormProps {
  id: string;
}
export default function UpdateDayPageContent({
  id,
  authUser,
  ...props
}: Props) {
  const {
    messages: day,
    error: dayError,
    isFinished: dayFinished,
  } = useFetchStream<CustomEntityModel<DayResponse>, BaseError>({
    path: `/days/${id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
  });

  const {
    messages: meals,
    error: mealsError,
    isFinished: mealsFinished,
  } = useFetchStream<
    ResponseWithChildListEntity<MealResponse, RecipeResponse>,
    BaseError
  >({
    path: `/meals/day/recipes/${id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
  });

  const { navigateToNotFound } = useClientNotFound();

  const initialData: InitialDataType = useMemo(
    () =>
      // meals.map(({ entity: { content } }) => {
      //   const period = content.period.split(":");
      //   return {
      //     period: {
      //       hour: parseInt(period[0]) || 0,
      //       minute: parseInt(period[1]) || 0,
      //     },
      //     recipes: content.recipes,
      //     id: content.id.toString(),
      //   };
      // }),

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

  if (!mealsFinished || !dayFinished) return <LoadingSpinner />;

  if (dayError || mealsError) {
    return navigateToNotFound();
  }

  if (dayFinished && !day[0]?.content) {
    return navigateToNotFound();
  }

  if (mealsFinished && !meals[0]?.entity?.content) {
    return navigateToNotFound();
  }

  console.log("UPD day", day);
  console.log("UPD meals", meals);

  const dayResponse = day[0].content;
  const ownerReturn = checkOwner(authUser, dayResponse, navigateToNotFound);

  if (React.isValidElement(ownerReturn)) {
    return ownerReturn;
  }

  return (
    <DayForm
      {...props}
      authUser={authUser}
      path={"/days/update/meals/" + id}
      initialData={initialData}
      existingDay={dayResponse}
    />
  );
}
