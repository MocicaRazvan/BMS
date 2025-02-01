import { getTranslations } from "next-intl/server";
import { RecommendationListTexts } from "@/components/recomandation/recommendation-list";
import { PlanItemRendererTexts } from "@/components/recomandation/plan-item-renderer";
import { PlanRecommendationListTexts } from "@/components/recomandation/plan-recommendation-list";
import { PostItemRendererTexts } from "@/components/recomandation/post-item-renderer";
import { PostRecommendationListTexts } from "@/components/recomandation/post-recommendation-list";

export async function getRecommendationListTexts(
  itemType: "post" | "plan",
): Promise<RecommendationListTexts> {
  const t = await getTranslations(
    "components.recommendation.RecommendationListTexts",
  );
  const type = t(`type.${itemType}`);
  return {
    title: t("title", { type }),
    similarity: t("similarity"),
  };
}

export async function getPlanItemRendererTexts(): Promise<PlanItemRendererTexts> {
  const t = await getTranslations(
    "components.recommendation.PlanItemRendererTexts",
  );
  return {
    objective: t("objective"),
  };
}

export async function getPlanRecommendationListTexts(): Promise<PlanRecommendationListTexts> {
  const [recommendationListTexts, planItemRendererTexts] = await Promise.all([
    getRecommendationListTexts("plan"),
    getPlanItemRendererTexts(),
  ]);
  return {
    recommendationListTexts,
    planItemRendererTexts,
  };
}

export async function getPostItemRendererTexts(): Promise<PostItemRendererTexts> {
  const t = await getTranslations(
    "components.recommendation.PostItemRendererTexts",
  );
  return {
    likes: t("likes"),
    dislikes: t("dislikes"),
  };
}

export async function getPostRecommendationListTexts(): Promise<PostRecommendationListTexts> {
  const [recommendationListTexts, postItemRendererTexts] = await Promise.all([
    getRecommendationListTexts("post"),
    getPostItemRendererTexts(),
  ]);
  return {
    recommendationListTexts,
    postItemRendererTexts,
  };
}
