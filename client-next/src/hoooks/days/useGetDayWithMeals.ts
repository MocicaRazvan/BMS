import { Session } from "next-auth";
import { useGetTitleBodyUser } from "@/hoooks/useGetTitleBodyUser";
import { CustomEntityModel, DayResponse, MealResponse } from "@/types/dto";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import { useMemo } from "react";

interface Args {
  dayId: number | string;
  authUser: NonNullable<Session["user"]>;
  dayBasePath?: string;
  mealsBasePath?: string;
  useAbortController?: boolean;
}
export default function useGetDaysWithMeals({
  dayId,
  authUser,
  dayBasePath,
  mealsBasePath,
  useAbortController = false,
}: Args) {
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
