"use client";

import {
  RecommendationListProps,
  RecommendationListTexts,
} from "@/components/recomandation/recommendation-list";
import PostItemRenderer, {
  PostItemRendererTexts,
} from "@/components/recomandation/post-item-renderer";
import dynamic from "next/dynamic";
import { Skeleton } from "@/components/ui/skeleton";
import { PostReposeWithSimilarity } from "@/types/dto";
import { ComponentProps } from "react";

const DynamicRecommendationList = dynamic<
  RecommendationListProps<
    PostReposeWithSimilarity,
    Omit<ComponentProps<typeof PostItemRenderer>, "item">
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
    <DynamicRecommendationList
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
