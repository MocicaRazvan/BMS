import { Session } from "next-auth";
import { useGetTitleBodyUser } from "@/hoooks/useGetTitleBodyUser";
import { CustomEntityModel, DayResponse, MealResponse } from "@/types/dto";
import useFetchStream from "@/hoooks/useFetchStream";
import { useMemo } from "react";

export default function useGetDaysWithMeals(
  dayId: number | string,
  authUser: NonNullable<Session["user"]>,
  dayBasePath?: string,
  mealsBasePath?: string,
  useAbortController = false,
) {
  const {
    itemState: dayState,
    setItemState: setDayState,
    messages,
    error: dayError,
    item: day,
    user,
    router,
    isFinished: dayIsFinished,
    isLiked,
    isDisliked,
  } = useGetTitleBodyUser<DayResponse>({
    authUser,
    basePath: dayBasePath || `/days/withUser`,
    itemId: dayId,
  });

  const {
    messages: mealsMessage,
    error: mealsError,
    isFinished: mealsIsFinished,
  } = useFetchStream<CustomEntityModel<MealResponse>>({
    path: mealsBasePath ? mealsBasePath + `/${dayId}` : `/meals/day/${dayId}`,
    method: "GET",
    authToken: true,
    useAbortController,
  });

  const meals = useMemo(
    () => mealsMessage.map(({ content }) => content),
    [mealsMessage],
  );

  return {
    dayState,
    setDayState,
    messages,
    dayError,
    day,
    user,
    router,
    dayIsFinished,
    isLiked,
    isDisliked,
    mealsMessage,
    mealsError,
    mealsIsFinished,
    meals,
  };
}
