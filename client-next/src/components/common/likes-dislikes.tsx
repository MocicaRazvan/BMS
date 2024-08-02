import { cn } from "@/lib/utils";
import { ThumbsDown, ThumbsUp } from "lucide-react";

interface Props {
  react?: (type: "like" | "dislike") => Promise<void>;
  likes: number[];
  dislikes: number[];
  isLiked: boolean;
  isDisliked: boolean;
  disabled?: boolean;
  size?: "sm" | "lg";
}

export default function LikesDislikes({
  react,
  likes,
  dislikes,
  isLiked,
  isDisliked,
  size = "lg",
  disabled = false,
}: Props) {
  return (
    <>
      <div
        className={cn(
          " flex items-center justify-center gap-2  group",
          !disabled && "cursor-pointer",
        )}
        onClick={() => react?.("like")}
      >
        <ThumbsUp
          size={size === "lg" ? 28 : 22}
          className={cn(
            (isLiked || disabled) && "text-success",
            !disabled &&
              "group-hover:scale-105 group-active:scale-95 transition-all",
          )}
        />
        <p
          className={cn(
            "text-success  font-bold group-hover:scale-105 group-active:scale-95 transition-all",
            size === "lg" && "text-lg",
          )}
        >
          {likes.length}
        </p>
      </div>
      <div
        className={cn(
          " flex items-center justify-center gap-2  group",
          !disabled && "cursor-pointer",
        )}
        onClick={() => react?.("dislike")}
      >
        <ThumbsDown
          size={size === "lg" ? 28 : 22}
          className={cn(
            (isDisliked || disabled) && "text-destructive ",
            !disabled &&
              "group-hover:scale-105 group-active:scale-95 transition-all",
          )}
        />
        <p
          className={cn(
            "text-destructive  font-bold group-hover:scale-105 group-active:scale-95 transition-all",
            size === "lg" && "text-lg",
          )}
        >
          {dislikes.length}
        </p>
      </div>
    </>
  );
}
