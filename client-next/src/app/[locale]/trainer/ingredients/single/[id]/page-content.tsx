"use client";

import useFetchStream from "@/hoooks/useFetchStream";
import {
  CustomEntityModel,
  IngredientNutritionalFactResponse,
  NutritionalFactResponse,
} from "@/types/dto";
import LoadingSpinner from "@/components/common/loading-spinner";

import { cn } from "@/lib/utils";

import { IngredientTableColumnTexts } from "@/components/table/ingredients-table";
import IngredientMacrosPieChart, {
  IngredientPieChartTexts,
  MacroChartElement,
} from "@/components/charts/ingredient-macros-pie-chart";
import { useMemo } from "react";
import { useDebounce } from "@/components/ui/multiple-selector";
import NutritionalTable, {
  NutritionalTableTexts,
} from "@/components/common/nutritional-table";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import PageContainer from "@/components/common/page-container";

const tableColsKeys: (keyof NutritionalFactResponse &
  keyof IngredientTableColumnTexts)[] = [
  "fat",
  "saturatedFat",
  "carbohydrates",
  "sugar",
  "protein",
  "salt",
] as const;
const tableCols: (keyof IngredientTableColumnTexts)[] = [
  ...tableColsKeys,
  "calories",
  "unit",
] as const;
export interface SingleIngredientPageTexts {
  ingredientColumnTexts: IngredientTableColumnTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  // tableCaption: string;
  nutritionalTableTexts: NutritionalTableTexts;
}
interface Props extends SingleIngredientPageTexts {
  id: string;
}

export default function SingleIngredientPageContent({
  id,
  nutritionalTableTexts,
  ingredientColumnTexts,
  ingredientPieChartTexts,
}: Props) {
  const { authUser } = useAuthUserMinRole();

  const { messages, error, isFinished } = useFetchStream<
    CustomEntityModel<IngredientNutritionalFactResponse>
  >({
    path: `/ingredients/${id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
  });
  const { navigateToNotFound } = useClientNotFound();

  const debouncedFinish = useDebounce(isFinished, 700);

  const chartItems = useMemo(() => {
    if (messages.length === 0 || !messages?.[0].content.nutritionalFact)
      return [];
    return (
      [
        "protein",
        "fat",
        "carbohydrates",
        "salt",
      ] as (keyof NutritionalFactResponse)[]
    ).map((macro) => ({
      macro,
      value: messages?.[0].content.nutritionalFact[macro],
    }));
  }, [JSON.stringify(messages)]);

  if (!isFinished) return <LoadingSpinner />;
  if (isFinished && messages.length == 0 && error?.status)
    return navigateToNotFound();

  const isAdmin = authUser?.role === "ROLE_ADMIN";
  const ing = messages[0].content;
  if (!ing) return null;
  const ingredient = ing.ingredient;
  const nutritionalFact = ing.nutritionalFact;

  if (!ingredient.display && !isAdmin) return navigateToNotFound();

  const colorMap = {
    CARNIVORE: "destructive",
    VEGAN: "success",
    OMNIVORE: "default",
    VEGETARIAN: "accent",
  };

  return (
    <PageContainer className="pb-14 flex items-center justify-center">
      <div className=" w-full mx-2 md:mx-0 md:w-2/3  border rounded-xl px-6 py-8 space-y-8 lg:space-y-16 ">
        <div className="w-full flex items-center justify-center  mb-12 gap-10 ">
          <h1 className="text-2xl lg:text-4xl tracking-tighter font-bold text-center ">
            {ingredient.name}
          </h1>
          <p
            className={cn(
              `px-3 py-1 bg-${colorMap[ingredient.type]} text-${colorMap[ingredient.type]}-foreground rounded-full font-bold`,
            )}
          >
            {ingredient.type}
          </p>
        </div>
        {isAdmin && (
          <h2
            className={cn(
              "text-lg w-full text-center font-semibold",
              ingredient?.display ? "text-success" : "text-destructive",
            )}
          >
            {`${ingredientColumnTexts.display.header} : ${
              ingredientColumnTexts.display[
                ingredient.display ? "true" : "false"
              ]
            }`}
          </h2>
        )}

        <NutritionalTable ing={ing} {...nutritionalTableTexts} />

        <div className="h-[350px] lg:h-[400px]">
          {debouncedFinish && (
            <IngredientMacrosPieChart
              innerRadius={85}
              items={chartItems as MacroChartElement[]}
              texts={ingredientPieChartTexts}
            />
          )}
        </div>
      </div>
    </PageContainer>
  );
}
