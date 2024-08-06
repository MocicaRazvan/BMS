"use client";

import { WithUser } from "@/lib/user";
import { CustomEntityModel, RecipeResponse } from "@/types/dto";
import LoadingSpinner from "@/components/common/loading-spinner";
import { checkOwnerOrAdmin, cn } from "@/lib/utils";
import { useCallback, useEffect, useMemo, useState } from "react";
import ElementHeader, {
  ElementHeaderTexts,
} from "@/components/common/element-header";
import { fetchStream } from "@/hoooks/fetchStream";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import ProseText from "@/components/common/prose-text";
import AuthorProfile from "@/components/common/author-profile";
import CustomVideoCarousel from "@/components/common/custom-videos-crousel";
import NutritionalTable, {
  NutritionalTableTexts,
} from "@/components/common/nutritional-table";
import IngredientMacrosPieChart, {
  IngredientPieChartTexts,
  MacroChartElement,
} from "@/components/charts/ingredient-macros-pie-chart";
import { useDebounce } from "@/components/ui/multiple-selector";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { Badge } from "@/components/ui/badge";
import useGetRecipeWithIngredients from "@/hoooks/recipes/useGetRecipeWithIngredients";
import RecipeMacros from "@/components/recipes/recipe-macros";

export interface SingleRecipePageTexts {
  elementHeaderTexts: ElementHeaderTexts;
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  showIngredients: string;
}

interface Props extends WithUser, SingleRecipePageTexts {
  id: string;
}
export default function SingeRecipePageContent({
  authUser,
  id,
  elementHeaderTexts,
  nutritionalTableTexts,
  ingredientPieChartTexts,
  showIngredients,
}: Props) {
  // const [isAllFinished, setIsAllFinished] = useState(false);
  // const {
  //   itemState: recipeState,
  //   setItemState: setRecipeState,
  //   messages,
  //   error: recipeError,
  //   // authUser,
  //   post,
  //   user,
  //   router,
  //   isFinished: recipeIsFinished,
  //   isLiked,
  //   isDisliked,
  // } = useGetTitleBodyUser<RecipeResponse>({
  //   authUser,
  //   basePath: `/recipes/withUser`,
  // });
  //
  // const {
  //   messages: IQMessage,
  //   error: IQError,
  //   isFinished: IQIsFinished,
  // } = useFetchStream<IngredientNutritionalFactResponseWithCount>({
  //   path: `/recipes/ingredients/${id}`,
  //   method: "GET",
  //   authToken: true,
  //   useAbortController: false,
  // });

  // useEffect(() => {
  //   if (recipeIsFinished && IQIsFinished) {
  //     setIsAllFinished(true);
  //   }
  // }, [IQIsFinished, recipeIsFinished]);
  //
  // const debouncedFinish = useDebounce(isAllFinished, 700);
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
  } = useGetRecipeWithIngredients(id, authUser);

  // const aggegatedNF: NutritionalFactResponse | null = useMemo(
  //   () =>
  //     IQMessage.length === 0
  //       ? null
  //       : IQMessage.map(
  //           (i) => i.nutritionalFact,
  //         ).reduce<NutritionalFactResponse>((acc, cur) => {
  //           if (!acc?.fat) return cur;
  //           return {
  //             ...acc,
  //             fat: acc.fat + cur.fat,
  //             saturatedFat: acc.saturatedFat + cur.saturatedFat,
  //             carbohydrates: acc.carbohydrates + cur.carbohydrates,
  //             sugar: acc.sugar + cur.sugar,
  //             protein: acc.protein + cur.protein,
  //             salt: acc.salt + cur.salt,
  //           };
  //         }, {} as NutritionalFactResponse),
  //   [IQMessage],
  // );

  const react = useCallback(
    async (type: "like" | "dislike") => {
      try {
        const resp = await fetchStream<CustomEntityModel<RecipeResponse>>({
          path: `/recipes/${type}/${id}`,
          method: "PATCH",
          token: authUser.token,
        });
        console.log(resp);
        const newPost = resp.messages[0]?.content;
        setRecipeState((prev) =>
          !prev
            ? prev
            : {
                ...prev,
                userLikes: newPost.userLikes,
                userDislikes: newPost.userDislikes,
              },
        );
      } catch (error) {
        console.log(error);
      }
    },
    [authUser.token, id, setRecipeState],
  );

  if (!recipeIsFinished || !IQIsFinished)
    return (
      <section className="w-full min-h-[calc(100vh-4rem)] flex items-center justify-center transition-all">
        <LoadingSpinner />
      </section>
    );

  // if (recipeError || IQError) {
  //   notFound();
  // }

  if (!recipeState) return null;

  // if (IQIsFinished && !(IQMessage.length > 0)) {
  //   notFound();
  // }

  // const recipe = messages[0].model.content;
  // const { isOwnerOrAdmin, isAdmin, isOwner } = checkApprovePrivilege(
  //   authUser,
  //   recipeState,
  // );

  const { isOwner, isAdmin } = checkOwnerOrAdmin(authUser, recipeState);
  const colorMap = {
    VEGAN: "success",
    OMNIVORE: "default",
    VEGETARIAN: "accent",
  };

  return (
    <section className="w-full mx-auto max-w-[1500px] min-h-[calc(100vh-4rem)] flex-col items-center justify-center transition-all px-6 py-10 relative ">
      {/*<ElementHeader*/}
      {/*  elementState={recipeState}*/}
      {/*  react={react}*/}
      {/*  isLiked={isLiked}*/}
      {/*  isDisliked={isDisliked}*/}
      {/*  {...elementHeaderTexts}*/}
      {/*/>*/}
      <div className="w-3/4 mx-auto flex flex-col md:flex-row items-center justify-between gap-4 md:gap-20 mb-2 ">
        <h2
          className={cn(
            "text-2xl font-bold text-center md:text-start text-destructive w-[250px] order-1 md:order-0",
            recipe.approved ? "invisible" : "block",
          )}
        >
          {elementHeaderTexts.notApproved}
        </h2>

        <div className=" flex items-center justify-center order-0 md:order-1">
          <h1
            className={cn(
              "text-6xl tracking-tighter font-bold text-center md:translate-x-[-125px] ",
            )}
          >
            {recipe?.title}
          </h1>
        </div>
        <div className="order-3">
          <p
            className={cn(
              `px-3 py-1 bg-${colorMap[recipe.type]} text-${colorMap[recipe.type]}-foreground rounded-full font-bold`,
            )}
          >
            {recipe.type}
          </p>
        </div>
      </div>
      {recipe?.images.length > 0 && (
        <div className="mt-10">
          <CustomImageCarousel images={recipe?.images} />
        </div>
      )}
      <div className="mt-20 px-14">
        <ProseText html={recipe?.body} />
        <AuthorProfile author={user} />
      </div>
      {recipe?.videos.length > 0 && (
        <div className="mt-20">
          <CustomVideoCarousel videos={recipe?.videos} />
        </div>
      )}

      <div className="mt-20">
        <RecipeMacros
          ingredients={IQMessage}
          nutritionalTableTexts={nutritionalTableTexts}
          ingredientPieChartTexts={ingredientPieChartTexts}
        />
      </div>
      {/*{aggegatedNF && (*/}
      {/*  <div className="px-0 lg:px-16 mt-20 space-y-8">*/}
      {/*    <NutritionalTable*/}
      {/*      ing={*/}
      {/*        {*/}
      {/*          nutritionalFact: aggegatedNF,*/}
      {/*        } as IngredientNutritionalFactResponse*/}
      {/*      }*/}
      {/*      {...nutritionalTableTexts}*/}
      {/*      tableClassName={"w-[80%] mx-auto"}*/}
      {/*      showUnit={false}*/}
      {/*    />*/}
      {/*    <div className="h-[350px] lg:h-[400px]">*/}
      {/*      <IngredientMacrosPieChart*/}
      {/*        innerRadius={85}*/}
      {/*        // items={[*/}
      {/*        //   { macro: "protein", value: nutritionalFact.protein },*/}
      {/*        //   { macro: "fat", value: nutritionalFact.fat },*/}
      {/*        //   {*/}
      {/*        //     macro: "carbohydrates",*/}
      {/*        //     value: nutritionalFact.carbohydrates,*/}
      {/*        //   },*/}
      {/*        //   { macro: "salt", value: nutritionalFact.salt },*/}
      {/*        // ]}*/}
      {/*        items={[*/}
      {/*          {*/}
      {/*            macro: "protein",*/}
      {/*            value: aggegatedNF.protein,*/}
      {/*          },*/}
      {/*          {*/}
      {/*            macro: "fat",*/}
      {/*            value: aggegatedNF.fat,*/}
      {/*          },*/}
      {/*          {*/}
      {/*            macro: "carbohydrates",*/}
      {/*            value: aggegatedNF.carbohydrates,*/}
      {/*          },*/}
      {/*          {*/}
      {/*            macro: "salt",*/}
      {/*            value: aggegatedNF.salt,*/}
      {/*          },*/}
      {/*        ]}*/}
      {/*        texts={ingredientPieChartTexts}*/}
      {/*      />*/}
      {/*    </div>*/}
      {/*  </div>*/}
      {/*)}*/}
      {IQMessage.length > 0 && (
        <Accordion
          type={"single"}
          collapsible
          className="w-full md:w-2/3 mx-auto mt-20 lg:mt-25"
        >
          <AccordionItem value={"item"}>
            <AccordionTrigger className="text-lg md:text-xl">
              {showIngredients}
            </AccordionTrigger>
            <AccordionContent className="space-y-10 mt-5">
              {IQMessage.map(
                (
                  {
                    ingredient: { name, type, id },
                    nutritionalFact: { unit },
                    count,
                  },
                  i,
                ) => (
                  <div
                    key={id}
                    className={cn(
                      "w-full gap-10 md:gap-0 flex flex-col md:flex-row items-center justify-between px-0 lg:px-6",
                      i !== IQMessage.length - 1 && "border-b pb-4",
                    )}
                  >
                    <div className="flex w-full items-center justify-around md:justify-between flex-1 md:flex-2">
                      <h3 className="text-lg  ">{name}</h3>
                      <div className=" flex items-center justify-center gap-2">
                        <span className={"font-semibold"}>{count}</span>
                        <Badge
                          variant={unit === "GRAM" ? "secondary" : "default"}
                        >
                          {unit}
                        </Badge>
                      </div>
                    </div>

                    <div className={"flex-1 flex justify-end"}>
                      <p
                        className={cn(
                          `px-3 py-1 bg-${colorMap[type]} text-${colorMap[type]}-foreground rounded-full font-bold`,
                        )}
                      >
                        {type}
                      </p>
                    </div>
                  </div>
                ),
              )}
            </AccordionContent>
          </AccordionItem>
        </Accordion>
      )}
    </section>
  );
}
