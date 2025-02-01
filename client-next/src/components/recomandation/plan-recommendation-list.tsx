"use client";

import RecommendationList, {
  RecommendationListTexts,
} from "@/components/recomandation/recommendation-list";
import PlanItemRenderer, {
  PlanItemRendererTexts,
} from "@/components/recomandation/plan-item-renderer";
import { usePlansSubscription } from "@/context/subscriptions-context";

export interface PlanRecommendationListTexts {
  recommendationListTexts: RecommendationListTexts;
  planItemRendererTexts: PlanItemRendererTexts;
}
interface Props extends PlanRecommendationListTexts {
  id: number;
}
export default function PlanRecommendationList({
  recommendationListTexts,
  planItemRendererTexts,
  id,
}: Props) {
  const { getSubscriptionPlanIds } = usePlansSubscription();
  return (
    <RecommendationList
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
