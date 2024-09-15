"use client";
import LikesDislikes from "@/components/common/likes-dislikes";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import ProseText from "@/components/common/prose-text";
import AuthorProfile from "@/components/common/author-profile";
import MealsList, { MealListProps } from "@/components/days/meals-list";
import React, { memo, useCallback, useState } from "react";
import { CustomEntityModel, DayResponse, DietType, UserDto } from "@/types/dto";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { IngredientPieChartTexts } from "@/components/charts/ingredient-macros-pie-chart";
import useFetchStream from "@/hoooks/useFetchStream";
import useClientNotFound from "@/hoooks/useClientNotFound";
import DietBadge from "@/components/common/diet-badge";
import { fetchStream } from "@/hoooks/fetchStream";
import DayTypeBadge, {
  DayTypeBadgeTexts,
} from "@/components/days/day-type-badge";

export interface SingleDayTexts {
  meals: string;
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  showIngredients: string;
  dayBadgeTexts: DayTypeBadgeTexts;
}

export interface SingleDayProps
  extends Omit<
    MealListProps,
    "nutritionalTableTexts" | "ingredientPieChartTexts" | "showIngredients"
  > {
  day: DayResponse;
  author: UserDto;
  disableLikes?: boolean;
  showRecipeLikes?: boolean;
  texts: SingleDayTexts;
  hideAuthor?: boolean;
}

const SingleDay = memo(
  ({
    day,
    author,
    disableLikes = true,
    showRecipeLikes = true,
    texts,
    authUser,
    recipeBasePath,
    hideAuthor = false,
    ...rest
  }: SingleDayProps) => {
    const [dayState, setDayState] = useState<DayResponse>(day);

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
        } catch (error) {
          console.log(error);
        }
      },
      [authUser.token, day.id],
    );

    if (!isFinished) {
      return null;
    }

    if (error?.status) {
      return navigateToNotFound();
    }

    if (!dayState) return null;

    const diet = messages[0];
    const isLiked = dayState.userLikes.includes(Number(authUser.id));
    const isDisliked = dayState.userDislikes.includes(Number(authUser.id));

    return (
      <section className="w-full mx-auto max-w-[1500px] flex-col items-center justify-center transition-all px-1 md:px-6 py-10 relative ">
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
                "text-2xl md:text-6xl text-balance tracking-tighter font-bold text-center  ",
              )}
            >
              {dayState.title}
            </h1>
          </div>
          <div className="order-3">
            <DietBadge dietType={diet} />
          </div>
        </div>
        <div className="mt-20 px-14">
          <ProseText html={dayState.body} />
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
          />
        </div>
      </section>
    );
  },
);

SingleDay.displayName = "SingleDay";

export default SingleDay;
