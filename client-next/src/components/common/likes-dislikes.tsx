import { cn } from "@/lib/utils";
import { ThumbsDown, ThumbsUp } from "lucide-react";
import { useRef, useState } from "react";

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
  const [showBurst, setShowBurst] = useState(false);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const handleLike = () => {
    if (!isLiked) {
      setShowBurst(true);
      intervalRef.current = setTimeout(() => {
        setShowBurst(false);
      }, 1510); // animation duration +10ms
    } else if (showBurst) {
      if (intervalRef.current) clearTimeout(intervalRef.current);
      setShowBurst(false);
    }
    react?.("like");
  };

  const handleDislike = () => {
    if (showBurst) {
      if (intervalRef.current) clearTimeout(intervalRef.current);
      setShowBurst(false);
    }
    react?.("dislike");
  };
  return (
    <>
      <div
        className={cn(
          " flex items-center justify-center gap-2  group",
          !disabled && "cursor-pointer",
        )}
        onClick={handleLike}
      >
        <div className="relative  cursor-pointer">
          <div className={`burst ${showBurst ? "show" : ""}`}>
            {Array.from({ length: 10 }).map((_, i) => (
              <span key={i} className={`particle particle${i + 1}`} />
            ))}
          </div>
          <ThumbsUp
            size={size === "lg" ? 28 : 22}
            className={cn(
              (isLiked || disabled) && "text-success",
              !disabled &&
                "group-hover:scale-110 group-active:scale-90 transition-transform duration-200 ease-out",
            )}
          />
        </div>
        <p
          className={cn(
            "text-success  font-bold group-hover:scale-105 group-active:scale-95 transition-all tabular-nums",
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
        onClick={handleDislike}
      >
        <ThumbsDown
          size={size === "lg" ? 28 : 22}
          className={cn(
            (isDisliked || disabled) && "text-destructive ",
            !disabled &&
              "group-hover:scale-110 group-active:scale-90 transition-transform duration-200 ease-out",
          )}
        />
        <p
          className={cn(
            "text-destructive  font-bold group-hover:scale-105 group-active:scale-95 transition-all tabular-nums",
            size === "lg" && "text-lg",
          )}
        >
          {dislikes.length}
        </p>
      </div>
    </>
  );
}
