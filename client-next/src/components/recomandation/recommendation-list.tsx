"use client";

import { useFetchStream, UseFetchStreamProps } from "@/hoooks/useFetchStream";
import LoadingSpinner from "@/components/common/loading-spinner";
import React from "react";
import {
  CustomEntityModel,
  IdGenerateDto,
  Similarity,
  TitleBodyUserDto,
} from "@/types/dto";
import { Card, CardContent, CardHeader } from "@/components/ui/card";

type MinRecT = IdGenerateDto & TitleBodyUserDto & Similarity;

export interface RecommendationListTexts {
  similarity: string;
  title: string;
}
interface Props<T extends MinRecT, P extends object = Record<string, never>> {
  itemId: number;
  fetchArgs: UseFetchStreamProps;
  ItemRenderer: React.ComponentType<{ item: T } & P>;
  texts: RecommendationListTexts;
  itemRendererProps?: P;
}

export default function RecommendationList<
  T extends MinRecT,
  P extends object = Record<string, never>,
>({ fetchArgs, itemId, ItemRenderer, texts, itemRendererProps }: Props<T, P>) {
  const { messages, error, isFinished, refetch } = useFetchStream<
    CustomEntityModel<T>
  >({
    method: "GET",
    authToken: true,
    useAbortController: true,
    ...fetchArgs,
  });

  if (!isFinished) {
    return <LoadingSpinner sectionClassName=" w-full h-full" />;
  }

  if (!messages.length || messages.length === 1) {
    return null;
  }

  const filteredMessages = messages
    .map((m) => m.content)
    .filter((m) => m.id !== itemId);

  return (
    <div className="space-y-10 overflow-hidden">
      <h3 className="font-semibold tracking-tighter text-lg md:text-[26px]">
        {texts.title}
      </h3>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 lg:grid-cols-3 p-2">
        {filteredMessages.map((item) => (
          <div key={item.id}>
            <RecommendationCard
              item={item}
              itemRendererProps={itemRendererProps}
              ItemRenderer={ItemRenderer}
              similarityText={texts.similarity}
            />
          </div>
        ))}
      </div>
    </div>
  );
}

export function RecommendationCard<
  T extends MinRecT,
  P extends object = Record<string, never>,
>({
  similarityText,
  item,
  ItemRenderer,
  itemRendererProps,
}: {
  item: T;
  similarityText: string;
  ItemRenderer: React.ComponentType<{ item: T } & P>;
  itemRendererProps?: P;
}) {
  return (
    <Card className="flex items-center justify-between  hover:shadow-md transition-all duration-300 hover:shadow-foreground/40 hover:scale-[1.015]">
      <CardHeader className="flex flex-row items-center justify-between w-full">
        <ItemRenderer
          {...({ item, ...itemRendererProps } as { item: T } & P)}
        />
      </CardHeader>
      <CardContent className="flex items-center justify-center">
        <div className="pt-4 flex flex-col items-center justify-center gap-2">
          <span className="text-sm font-medium">{similarityText}</span>
          <SimilarityIndicator similarity={item.similarity} />
        </div>
      </CardContent>
    </Card>
  );
}

interface SimilarityIndicatorProps {
  similarity: number;
  size?: number;
}

function SimilarityIndicator({
  similarity,
  size = 80,
}: SimilarityIndicatorProps) {
  const clampedPercentage = Math.max(
    0,
    Math.min(100, Math.round(similarity * 100)),
  );

  const getColor = (percent: number) => {
    if (percent < 33) return "hsl(var(--destructive))";
    if (percent < 66) return "hsl(var(--amber))";
    return "hsl(var(--success))";
  };

  const strokeWidth = size * 0.1;
  const radius = (size - strokeWidth) / 2;
  const circumference = radius * 2 * Math.PI;
  const strokeDashoffset =
    circumference - (clampedPercentage / 100) * circumference;

  return (
    <div className="relative" style={{ width: size, height: size }}>
      <svg
        className="w-full h-full"
        viewBox={`0 0 ${size} ${size}`}
        shapeRendering="geometricPrecision"
        textRendering="geometricPrecision"
        imageRendering="optimizeQuality"
        fillRule="evenodd"
        clipRule="evenodd"
      >
        <circle
          className="text-foreground"
          strokeWidth={strokeWidth}
          stroke="currentColor"
          fill="transparent"
          r={radius}
          cx={size / 2}
          cy={size / 2}
        />
        <circle
          className="transition-all duration-300 ease-in-out"
          strokeWidth={strokeWidth + 0.55}
          strokeDasharray={circumference}
          strokeDashoffset={strokeDashoffset}
          strokeLinecap="round"
          stroke={getColor(clampedPercentage)}
          fill="transparent"
          r={radius}
          cx={size / 2}
          cy={size / 2}
          style={{
            transformOrigin: "50% 50%",
            transform: "rotate(-90deg)",
          }}
        />
      </svg>
      <div
        className="absolute inset-0 flex flex-col items-center justify-center text-lg font-semibold"
        style={{ color: getColor(clampedPercentage) }}
      >
        <span>
          {clampedPercentage}
          {"%"}
        </span>
      </div>
    </div>
  );
}
