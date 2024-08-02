"use client";
import { memo, useCallback, useState } from "react";
import { cn, isDeepEqual } from "@/lib/utils";
import CustomPaginationButtons from "@/components/ui/custom-pagination-buttons";
import useGetRecipeWithIngredients from "@/hoooks/recipes/useGetRecipeWithIngredients";
import { WithUser } from "@/lib/user";
import { AnimatePresence, motion } from "framer-motion";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { IngredientPieChartTexts } from "@/components/charts/ingredient-macros-pie-chart";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import ProseText from "@/components/common/prose-text";
import AuthorProfile from "@/components/common/author-profile";
import CustomVideoCarousel from "@/components/common/custom-videos-crousel";
import RecipeMacros from "@/components/recipes/recipe-macros";
import { fetchStream } from "@/hoooks/fetchStream";
import { CustomEntityModel, PostResponse, RecipeResponse } from "@/types/dto";
import LikesDislikes from "@/components/common/likes-dislikes";

interface Props extends WithUser {
  recipeIds: number[];
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  recipeBasePath?: string;
  showLikes?: boolean;
}

export const RecipePlanList = memo(
  ({
    recipeIds,
    authUser,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    recipeBasePath,
    showLikes,
  }: Props) => {
    const [currentIndex, setCurrentIndex] = useState(0);
    console.log("currentIndex", currentIndex);
    return (
      <div>
        <div className="mb-5">
          <CustomPaginationButtons
            items={recipeIds}
            currentIndex={currentIndex}
            setCurrentIndex={setCurrentIndex}
          />
        </div>
        <AnimatePresence mode="wait">
          {recipeIds.map((recipeId, index) => (
            <motion.div
              key={recipeId + "-" + index}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: index === currentIndex ? 1 : 0 }}
              exit={{ opacity: 0, y: -20 }}
              transition={{ duration: 0.3 }}
              className={cn(index !== currentIndex ? "hidden" : "block")}
            >
              <RecipePlanItem
                recipeId={recipeId}
                authUser={authUser}
                nutritionalTableTexts={nutritionalTableTexts}
                ingredientPieChartTexts={ingredientPieChartTexts}
                recipeBasePath={recipeBasePath}
                showLikes={showLikes}
              />
            </motion.div>
          ))}
        </AnimatePresence>
        <div className="mt-10">
          <CustomPaginationButtons
            items={recipeIds}
            currentIndex={currentIndex}
            setCurrentIndex={setCurrentIndex}
          />
        </div>
      </div>
    );
  },
  (prevProps, nextProps) => isDeepEqual(prevProps, nextProps),
);

RecipePlanList.displayName = "RecipePlanList";

interface RecipePlanItemProps extends WithUser {
  recipeId: number;
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  recipeBasePath?: string;
  showLikes?: boolean;
}
export const RecipePlanItem = memo(
  ({
    recipeId,
    authUser,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    recipeBasePath,
    showLikes = false,
  }: RecipePlanItemProps) => {
    const {
      recipeState,
      setRecipeState,
      messages,
      recipeError,
      recipe,
      user,
      router,
      recipeIsFinished,
      isLiked,
      isDisliked,
      IQMessage,
      IQError,
      IQIsFinished,
    } = useGetRecipeWithIngredients(recipeId, authUser, recipeBasePath);

    console.log(`Rendering RecipePlanItem for recipeId: ${recipeId}`);

    const react = useCallback(
      async (type: "like" | "dislike") => {
        try {
          const resp = await fetchStream<CustomEntityModel<RecipeResponse>>({
            path: `/recipes/${type}/${recipeId}`,
            method: "PATCH",
            token: authUser.token,
          });
          console.log(resp);
          const resR = resp.messages[0]?.content;
          setRecipeState((prev) =>
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
      [authUser.token, recipeId, setRecipeState],
    );

    const colorMap = {
      VEGAN: "success",
      OMNIVORE: "secondary",
      VEGETARIAN: "accent",
    };
    if (!recipe || !recipeState) return null;
    return (
      <div className="w-full px-5">
        <div className=" gap-5 md:gap-0 flex flex-col md:flex-row items-center justify-between w-full max-w-screen-lg mx-auto">
          <div className="order-1 md:order-0 flex items-center justify-center w-[250px] gap-4 ">
            {showLikes && (
              <LikesDislikes
                react={react}
                likes={recipeState?.userLikes || []}
                dislikes={recipeState?.userDislikes || []}
                isLiked={isLiked || false}
                isDisliked={isDisliked || false}
                disabled={false}
              />
            )}
          </div>
          <div className=" order-0 md:order-1 flex   items-center justify-center w-full ">
            <h1 className="text-6xl tracking-tighter font-bold text-center md:translate-x-[-125px]">
              {recipeState?.title}
            </h1>
            <p
              className={cn(
                `ms-5 px-3 py-1 bg-${colorMap[recipeState.type]} text-${colorMap[recipeState.type]}-foreground rounded-full
                 md:translate-x-[-125px] font-bold w-fit text-lg mt-2`,
              )}
            >
              {recipeState.type}
            </p>
          </div>
          <div className="w-32 "></div>
        </div>
        {recipeState?.images.length > 0 && (
          <div className="mt-10">
            <CustomImageCarousel images={recipeState?.images} />
          </div>
        )}
        <div className="mt-20 px-10">
          <ProseText html={recipeState?.body} />
        </div>{" "}
        {recipeState?.videos.length > 0 && (
          <div className="mt-20">
            <CustomVideoCarousel videos={recipeState?.videos} />
          </div>
        )}
        <div className="mt-20">
          <RecipeMacros
            ingredients={IQMessage}
            nutritionalTableTexts={nutritionalTableTexts}
            ingredientPieChartTexts={ingredientPieChartTexts}
          />
        </div>
      </div>
    );
  },
  (prevProps, nextProps) => isDeepEqual(prevProps, nextProps),
);

RecipePlanItem.displayName = "RecipePlanItem";
