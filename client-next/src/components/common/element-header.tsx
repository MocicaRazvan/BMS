"use client";

import { ApproveDto } from "@/types/dto";
import { cn } from "@/lib/utils";
import LikesDislikes from "@/components/common/likes-dislikes";
import { ReactNode } from "react";

export interface ElementHeaderTexts {
  notApproved: string;
  approved: string;
}

interface Props<T extends ApproveDto> extends ElementHeaderTexts {
  elementState: T;
  react?: (type: "like" | "dislike") => Promise<void>;
  isLiked?: boolean;
  isDisliked?: boolean;
  likesDisabled?: boolean;
  extraContent?: ReactNode;
}

export default function ElementHeader<T extends ApproveDto>({
  elementState,
  react,
  isLiked,
  isDisliked,
  notApproved,
  likesDisabled = false,
  extraContent,
}: Props<T>) {
  // const showLikes = react && isLiked !== undefined && isDisliked !== undefined;
  const showLikes = true;
  return (
    <div className="w-3/4 mx-auto flex flex-col md:flex-row items-center md:items-start justify-center gap-4 md:gap-20 mb-2 relative">
      {elementState?.approved ? (
        showLikes ? (
          <div className="w-[250px] flex items-center justify-center gap-4 order-1 md:order-0">
            <LikesDislikes
              react={react}
              likes={elementState?.userLikes || []}
              dislikes={elementState?.userDislikes || []}
              isLiked={isLiked || false}
              isDisliked={isDisliked || false}
              disabled={likesDisabled}
            />
          </div>
        ) : null
      ) : (
        <h2 className="text-2xl font-bold text-center md:text-start text-destructive w-[250px] order-1 md:order-0">
          {notApproved}
        </h2>
      )}

      <div className="flex-1 flex items-center justify-center order-0 md:order-1">
        <h1
          className={cn(
            "text-2xl md:text-4xl text-balance tracking-tighter font-bold text-center",
            showLikes && "md:translate-x-[-125px]",
          )}
        >
          {elementState?.title}
        </h1>
      </div>

      {extraContent && (
        <div className="order-2 md:order-2 my-1 md:my-0 md:absolute md:right-0 md:top-1/2 md:-translate-y-1/2">
          {extraContent}
        </div>
      )}
    </div>
  );
}
