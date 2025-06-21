"use client";
import LikesDislikes from "@/components/common/likes-dislikes";
import { cn } from "@/lib/utils";
import AuthorProfile from "@/components/common/author-profile";
import MealsList, { MealListProps } from "@/components/days/meals-list";
import React, { ReactNode, useCallback, useEffect, useState } from "react";
import { CustomEntityModel, DayResponse, DietType, UserDto } from "@/types/dto";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { IngredientPieChartTexts } from "@/components/charts/ingredient-macros-pie-chart";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import useClientNotFound from "@/hoooks/useClientNotFound";
import DietBadge from "@/components/common/diet-badge";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import DayTypeBadge, {
  DayTypeBadgeTexts,
} from "@/components/days/day-type-badge";
import LoadingSpinner from "@/components/common/loading-spinner";
import { AnswerFromBodyFormTexts } from "@/components/forms/answer-from-body-form";
import ItemBodyQa from "@/components/common/item-body-qa";

export interface SingleDayTexts {
  meals: string;
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  showIngredients: string;
  dayBadgeTexts: DayTypeBadgeTexts;
  answerFromBodyFormTexts: AnswerFromBodyFormTexts;
}

export interface SingleDayProps
  extends Omit<
    MealListProps,
    | "nutritionalTableTexts"
    | "ingredientPieChartTexts"
    | "showIngredients"
    | "answerFromBodyFormTexts"
  > {
  day: DayResponse;
  author: UserDto;
  disableLikes?: boolean;
  showRecipeLikes?: boolean;
  texts: SingleDayTexts;
  hideAuthor?: boolean;
  onReactCallback?: (userLikes: number[], userDislikes: number[]) => void;
  subHeaderContent?: (day: DayResponse) => ReactNode;
}

const SingleDay = ({
  day,
  author,
  disableLikes = true,
  showRecipeLikes = true,
  texts,
  authUser,
  recipeBasePath,
  hideAuthor = false,
  onReactCallback,
  subHeaderContent,
  ...rest
}: SingleDayProps) => {
  const [dayState, setDayState] = useState<DayResponse>(day);
  useEffect(() => {
    setDayState(day);
  }, [JSON.stringify(day)]);

  const { messages, error, isFinished } = useFetchStream<DietType>({
    path: "/meals/day/dietType",
    method: "GET",
    authToken: true,
    arrayQueryParam: {
      ids: [day.id.toString()],
    },
  });
  const { navigateToNotFound } = useClientNotFound();
  const react = useCallback(
    async (type: "like" | "dislike") => {
      try {
        const resp = await fetchStream<CustomEntityModel<DayResponse>>({
          path: `/days/${type}/${day.id}`,
          method: "PATCH",
          token: authUser.token,
        });
        const resR = resp.messages[0]?.content;
        setDayState((prev) =>
          !prev
            ? prev
            : {
                ...prev,
                userLikes: resR.userLikes,
                userDislikes: resR.userDislikes,
              },
        );
        onReactCallback?.(resR.userLikes, resR.userDislikes);
      } catch (error) {
        console.log(error);
      }
    },
    [authUser.token, day.id, onReactCallback],
  );

  //todo test good
  if (!isFinished) {
    return (
      <section className="w-full min-h-[30vh] flex items-center justify-center overflow-hidden my-2">
        <LoadingSpinner />
      </section>
    );
  }

  if (error?.status) {
    return navigateToNotFound();
  }

  if (!dayState) return null;

  const diet = messages[0];
  const isLiked = dayState.userLikes.includes(Number(authUser.id));
  const isDisliked = dayState.userDislikes.includes(Number(authUser.id));

  return (
    <section className="w-full mx-auto max-w-[1500px] flex-col items-center justify-center px-1 md:px-6 py-10 relative ">
      <div className="w-3/4 mx-auto flex flex-col md:flex-row items-center justify-between gap-10 md:gap-20 mb-2 ">
        <div className="order-1 flex items-center justify-center gap-3">
          <div className="flex items-center justify-center gap-4 flex-1">
            <LikesDislikes
              likes={dayState.userLikes}
              dislikes={dayState.userDislikes}
              isLiked={isLiked || false}
              isDisliked={isDisliked || false}
              // disabled={disableLikes}
              react={react}
            />
          </div>
          <DayTypeBadge type={day.type} {...texts.dayBadgeTexts} />
        </div>
        <div className=" flex items-center justify-center order-0 md:order-1 flex-1 ">
          <h1
            className={cn(
              "text-2xl md:text-4xl text-balance tracking-tighter font-bold text-center  ",
            )}
          >
            {dayState.title}
          </h1>
        </div>
        <div className="order-3">
          <DietBadge dietType={diet} />
        </div>
      </div>
      {subHeaderContent !== undefined && subHeaderContent(day)}
      <div className="mt-20 px-14">
        <ItemBodyQa
          html={dayState.body}
          formProps={{
            body: dayState.body,
            texts: texts.answerFromBodyFormTexts,
          }}
        />
        {!hideAuthor && <AuthorProfile author={author} />}
      </div>
      <div className="mt-20 px-2">
        <h2 className="text-2xl lg:text-4xl text-center font-bold tracking-tighter my-10">
          {texts.meals}
        </h2>
        <MealsList
          {...rest}
          ingredientPieChartTexts={texts.ingredientPieChartTexts}
          nutritionalTableTexts={texts.nutritionalTableTexts}
          disableLikes={disableLikes}
          showLikes={showRecipeLikes}
          authUser={authUser}
          recipeBasePath={
            recipeBasePath ? recipeBasePath + `/${day.id}` : undefined
          }
          showIngredients={texts.showIngredients}
          answerFromBodyFormTexts={texts.answerFromBodyFormTexts}
        />
      </div>
    </section>
  );
};
SingleDay.displayName = "SingleDay";

export default SingleDay;
