"use client";

import RecommendationList, {
  RecommendationListTexts,
} from "@/components/recomandation/recommendation-list";
import PostItemRenderer, {
  PostItemRendererTexts,
} from "@/components/recomandation/post-item-renderer";
import PlanItemRenderer, {
  PlanItemRendererTexts,
} from "@/components/recomandation/plan-item-renderer";
import { usePlansSubscription } from "@/context/subscriptions-context";

export default function PageContent({
  texts,
  texts2,
}: {
  texts: RecommendationListTexts;
  texts2: PostItemRendererTexts;
}) {
  return (
    <RecommendationList
      itemId={5}
      ItemRenderer={PostItemRenderer}
      itemRendererProps={{ texts: texts2 }}
      texts={texts}
      fetchArgs={{
        path: "/posts/similar/92",
        method: "GET",
        authToken: true,
      }}
    />
  );
}
