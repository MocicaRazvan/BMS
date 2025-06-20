"use client";

import {
  RecommendationListProps,
  RecommendationListTexts,
} from "@/components/recomandation/recommendation-list";
import PlanItemRenderer, {
  PlanItemRendererTexts,
} from "@/components/recomandation/plan-item-renderer";
import { usePlansSubscription } from "@/context/subscriptions-context";
import dynamic from "next/dynamic";
import { PlanReposeWithSimilarity } from "@/types/dto";
import { ComponentProps } from "react";
import { Skeleton } from "@/components/ui/skeleton";

export interface PlanRecommendationListTexts {
  recommendationListTexts: RecommendationListTexts;
  planItemRendererTexts: PlanItemRendererTexts;
}
interface Props extends PlanRecommendationListTexts {
  id: number;
}

const DynamicRecommendationList = dynamic<
  RecommendationListProps<
    PlanReposeWithSimilarity,
    Omit<ComponentProps<typeof PlanItemRenderer>, "item">
  >
>(() => import("@/components/recomandation/recommendation-list"), {
  ssr: false,
  loading: () => (
    <Skeleton
      className="h-96 md:h-[25vh] mx-auto w-full"
      area-label="Loading recommendations"
    />
  ),
});
export default function PlanRecommendationList({
  recommendationListTexts,
  planItemRendererTexts,
  id,
}: Props) {
  const { getSubscriptionPlanIds } = usePlansSubscription();
  return (
    <DynamicRecommendationList
      itemId={id}
      ItemRenderer={PlanItemRenderer}
      itemRendererProps={{ texts: planItemRendererTexts }}
      texts={recommendationListTexts}
      fetchArgs={{
        path: `/plans/similar/${id}`,
        method: "GET",
        authToken: true,
        arrayQueryParam: {
          excludeIds: getSubscriptionPlanIds().map((id) => id.toString()),
        },
      }}
    />
  );
}
