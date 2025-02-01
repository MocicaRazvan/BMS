"use client";

import RecommendationList, {
  RecommendationListTexts,
} from "@/components/recomandation/recommendation-list";
import PostItemRenderer, {
  PostItemRendererTexts,
} from "@/components/recomandation/post-item-renderer";

export interface PostRecommendationListTexts {
  recommendationListTexts: RecommendationListTexts;
  postItemRendererTexts: PostItemRendererTexts;
}
interface Props extends PostRecommendationListTexts {
  id: number;
}
export default function PostRecommendationList({
  recommendationListTexts,
  postItemRendererTexts,
  id,
}: Props) {
  return (
    <RecommendationList
      itemId={id}
      ItemRenderer={PostItemRenderer}
      itemRendererProps={{ texts: postItemRendererTexts }}
      texts={recommendationListTexts}
      fetchArgs={{
        path: `/posts/similar/${id}`,
        method: "GET",
        authToken: true,
      }}
    />
  );
}
