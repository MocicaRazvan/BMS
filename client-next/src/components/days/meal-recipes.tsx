"use client";
import { memo, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { cn, isDeepEqual } from "@/lib/utils";
import CustomPaginationButtons from "@/components/ui/custom-pagination-buttons";
import useGetRecipeWithIngredients from "@/hoooks/recipes/useGetRecipeWithIngredients";
import { WithUser } from "@/lib/user";
import { AnimatePresence, motion } from "framer-motion";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { IngredientPieChartTexts } from "@/components/charts/ingredient-macros-pie-chart";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import CustomVideoCarousel from "@/components/common/custom-videos-crousel";
import RecipeMacros from "@/components/recipes/recipe-macros";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import {
  CustomEntityModel,
  IngredientNutritionalFactResponseWithCount,
  RecipeResponse,
} from "@/types/dto";
import LikesDislikes from "@/components/common/likes-dislikes";
import LoadingSpinner from "@/components/common/loading-spinner";
import useClientNotFound from "@/hoooks/useClientNotFound";
import RecipeIngredients from "@/components/recipes/recipe-ingredients";
import DietBadge from "@/components/common/diet-badge";
import { AnswerFromBodyFormTexts } from "@/components/forms/answer-from-body-form";
import ItemBodyQa from "@/components/common/item-body-qa";

export type SetRecipeOpenType = (
  recipeId: number,
  recipe: RecipeResponse,
  iqItems: IngredientNutritionalFactResponseWithCount[],
) => void;
export interface MealRecipeProps extends WithUser {
  recipeIds: number[];
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  recipeBasePath?: string;
  showLikes?: boolean;
  disableLikes?: boolean;
  showIngredients: string;
  answerFromBodyFormTexts: AnswerFromBodyFormTexts;
  setRecipeOpen: SetRecipeOpenType;
  openedRecipes: OpenedRecipeItemState;
}

type OpenedRecipeItem = {
  triggered: boolean;
  recipe?: RecipeResponse;
  iqItems?: IngredientNutritionalFactResponseWithCount[];
};

export type OpenedRecipeItemState = Record<number, OpenedRecipeItem>;

export const MealRecipeList = memo(
  ({
    recipeIds,
    authUser,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    recipeBasePath,
    showLikes,
    disableLikes = false,
    showIngredients,
    answerFromBodyFormTexts,
    setRecipeOpen,
    openedRecipes,
  }: MealRecipeProps) => {
    const [currentIndex, setCurrentIndex] = useState(0);
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
              className={cn(
                "w-full",
                index !== currentIndex ? "hidden" : "block",
              )}
            >
              <RecipePlanItem
                recipeId={recipeId}
                authUser={authUser}
                nutritionalTableTexts={nutritionalTableTexts}
                ingredientPieChartTexts={ingredientPieChartTexts}
                recipeBasePath={recipeBasePath}
                showLikes={showLikes}
                disableLikes={disableLikes}
                showIngredients={showIngredients}
                answerFromBodyFormTexts={answerFromBodyFormTexts}
                setRecipeOpen={setRecipeOpen}
                itemState={openedRecipes[recipeId] || { triggered: false }}
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
  (
    { setRecipeOpen: prevSetRecipeOpen, ...prevProps },
    { setRecipeOpen, ...nextProps },
  ) => isDeepEqual(prevProps, nextProps) && prevSetRecipeOpen === setRecipeOpen,
);

MealRecipeList.displayName = "RecipePlanList";

interface RecipePlanItemProps extends WithUser {
  recipeId: number;
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  recipeBasePath?: string;
  showLikes?: boolean;
  disableLikes?: boolean;
  showIngredients: string;
  answerFromBodyFormTexts: AnswerFromBodyFormTexts;
  setRecipeOpen: SetRecipeOpenType;
  itemState: OpenedRecipeItem;
}
export const RecipePlanItem = memo(
  ({
    recipeId,
    authUser,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    recipeBasePath,
    showLikes = true,
    disableLikes = false,
    showIngredients,
    answerFromBodyFormTexts,
    setRecipeOpen,
    itemState,
  }: RecipePlanItemProps) => {
    const shouldNotTrigger =
      itemState.triggered && itemState.recipe && !!itemState.iqItems;
    const [IQMessagesState, setIQMessagesState] = useState<
      IngredientNutritionalFactResponseWithCount[]
    >([]);
    const [recipeState, setRecipeState] = useState<RecipeResponse | null>(
      itemState.recipe || null,
    );

    const wasTriggeredThisMount = useRef(false);
    const isLiked = useMemo(
      () => recipeState?.userLikes.includes(parseInt(authUser.id)),
      [recipeState, authUser.id],
    );
    const isDisliked = useMemo(
      () => recipeState?.userDislikes.includes(parseInt(authUser.id)),
      [recipeState, authUser.id],
    );
    const {
      recipeError,
      recipe,
      recipeIsFinished,
      IQMessage,
      IQError,
      IQIsFinished,
      recipeIsAbsoluteFinished,
      IQIsAbsoluteFinished,
    } = useGetRecipeWithIngredients(
      recipeId,
      authUser,
      recipeBasePath,
      undefined,
      !shouldNotTrigger,
    );

    useEffect(() => {
      if (
        recipeIsAbsoluteFinished &&
        IQIsAbsoluteFinished &&
        recipe &&
        !wasTriggeredThisMount.current
      ) {
        // console.log("MealRecipes setting absolute finished", {
        //   recipeId,
        //   recipeIsFinished,
        //   IQIsAbsoluteFinished,
        // });
        setRecipeOpen(recipeId, recipe, IQMessage);
        setIQMessagesState(IQMessage);
        setRecipeState(recipe);
        wasTriggeredThisMount.current = true;
      }
    }, [
      IQIsAbsoluteFinished,
      IQMessage,
      recipe,
      recipeId,
      recipeIsAbsoluteFinished,
      recipeIsFinished,
      recipeState,
      setRecipeOpen,
    ]);

    useEffect(() => {
      if (shouldNotTrigger && !wasTriggeredThisMount.current) {
        // console.log("MealRecipes setting not triggered", { recipeId });
        setRecipeState(itemState.recipe || null);
        setIQMessagesState(itemState.iqItems || []);
        wasTriggeredThisMount.current = true;
      }
    }, [shouldNotTrigger]);

    const { navigateToNotFound } = useClientNotFound();

    const react = useCallback(
      async (type: "like" | "dislike") => {
        try {
          const resp = await fetchStream<CustomEntityModel<RecipeResponse>>({
            path: `/recipes/${type}/${recipeId}`,
            method: "PATCH",
            token: authUser.token,
          });
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
          setRecipeOpen(recipeId, resR, IQMessagesState);
        } catch (error) {
          console.log(error);
        }
      },
      [
        IQMessagesState,
        authUser.token,
        recipeId,
        setRecipeOpen,
        setRecipeState,
      ],
    );

    if (
      (!IQIsFinished && !IQMessagesState.length) ||
      (!recipeIsFinished && !recipeState)
    ) {
      return (
        <section className="w-full min-h-[40vh] flex items-center justify-center transition-all overflow-hidden my-2">
          <LoadingSpinner />
        </section>
      );
    }

    if (recipeError || IQError) {
      return navigateToNotFound();
    }

    if (!recipeState || !IQMessagesState) return null;

    return (
      <div className="w-full px-5">
        <div className="w-3/4 mx-auto flex flex-col md:flex-row items-center justify-between gap-10 md:gap-20 mb-2 ">
          <div className="order-1 flex items-center justify-center gap-3">
            <div className="flex flex-row md:flex-col items-center justify-center gap-4 flex-1">
              <div className="flex items-center justify-center gap-4">
                <LikesDislikes
                  likes={recipeState.userLikes}
                  dislikes={recipeState.userDislikes}
                  isLiked={isLiked || false}
                  isDisliked={isDisliked || false}
                  react={react}
                />
              </div>
            </div>
          </div>
          <div className=" flex items-center justify-center order-0 md:order-1 flex-1">
            <h1
              className={cn(
                "text-3xl md:text-4xl tracking-tighter font-bold text-center",
              )}
            >
              {recipeState.title}
            </h1>
          </div>
          <div className="order-3">
            <DietBadge dietType={recipeState.type} />
          </div>
        </div>
        {recipeState?.images.length > 0 && (
          <div className="mt-10">
            <CustomImageCarousel images={recipeState?.images} />
          </div>
        )}
        <div className="mt-20 px-10">
          <ItemBodyQa
            html={recipeState?.body}
            formProps={{
              body: recipeState?.body,
              texts: answerFromBodyFormTexts,
            }}
          />
        </div>
        {recipeState?.videos.length > 0 && (
          <div className="mt-20">
            <CustomVideoCarousel videos={recipeState?.videos} />
          </div>
        )}
        <div className="mt-20">
          <RecipeMacros
            ingredients={IQMessagesState}
            nutritionalTableTexts={nutritionalTableTexts}
            ingredientPieChartTexts={ingredientPieChartTexts}
          />
        </div>
        {IQMessagesState.length > 0 && (
          <RecipeIngredients
            showIngredients={showIngredients}
            IQMessage={IQMessagesState}
          />
        )}
      </div>
    );
  },
  (
    { setRecipeOpen: prevSetRecipeOpen, ...prevProps },
    { setRecipeOpen, ...nextProps },
  ) => isDeepEqual(prevProps, nextProps) && prevSetRecipeOpen === setRecipeOpen,
);

RecipePlanItem.displayName = "RecipePlanItem";
