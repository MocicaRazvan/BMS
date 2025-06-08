"use client";
import { cn } from "@/lib/utils";
import {
  MediaControlBar,
  MediaController,
  MediaMuteButton,
  MediaPlayButton,
  MediaSeekBackwardButton,
  MediaSeekForwardButton,
  MediaTimeDisplay,
  MediaTimeRange,
  MediaVolumeRange,
  MediaFullscreenButton,
} from "media-chrome/react";
import { CSSProperties, ComponentProps, forwardRef } from "react";
export type VideoPlayerProps = ComponentProps<typeof MediaController>;
const variables = {
  "--media-primary-color": "hsl(var(--primary))",
  "--media-secondary-color": "hsl(var(--background))",
  "--media-text-color": "hsl(var(--foreground))",
  "--media-background-color": "hsl(var(--background))",
  "--media-control-hover-background": "hsl(var(--accent))",
  "--media-live-button-icon-color": "hsl(var(--muted-foreground))",
  "--media-live-button-indicator-color": "hsl(var(--destructive))",
  "--media-range-track-background": "hsl(var(--border))",
  "--media-font-family": "var(--font-sans)",
} as CSSProperties;
export const VideoPlayer = ({
  style,
  className,
  ...props
}: VideoPlayerProps) => (
  <MediaController
    style={{
      ...variables,
      ...style,
    }}
    className={cn(
      "w-full max-w-screen overflow-hidden border rounded-lg",
      className,
    )}
    {...props}
  />
);
export type VideoPlayerControlBarProps = ComponentProps<typeof MediaControlBar>;
export const VideoPlayerControlBar = (props: VideoPlayerControlBarProps) => (
  <MediaControlBar {...props} />
);
export type VideoPlayerTimeRangeProps = ComponentProps<typeof MediaTimeRange>;
export const VideoPlayerTimeRange = ({
  className,
  ...props
}: VideoPlayerTimeRangeProps) => (
  <MediaTimeRange className={cn("p-2.5", className)} {...props} />
);
export type VideoPlayerTimeDisplayProps = ComponentProps<
  typeof MediaTimeDisplay
>;
export const VideoPlayerTimeDisplay = ({
  className,
  ...props
}: VideoPlayerTimeDisplayProps) => (
  <MediaTimeDisplay
    className={cn("p-2.5", className)}
    showDuration
    {...props}
  />
);
export type VideoPlayerVolumeRangeProps = ComponentProps<
  typeof MediaVolumeRange
>;
export const VideoPlayerVolumeRange = ({
  className,
  ...props
}: VideoPlayerVolumeRangeProps) => (
  <MediaVolumeRange className={cn("p-2.5", className)} {...props} />
);
export type VideoPlayerPlayButtonProps = ComponentProps<typeof MediaPlayButton>;
export const VideoPlayerPlayButton = ({
  className,
  ...props
}: VideoPlayerPlayButtonProps) => (
  <MediaPlayButton noTooltip className={cn("p-2.5", className)} {...props} />
);
export type VideoPlayerSeekBackwardButtonProps = ComponentProps<
  typeof MediaSeekBackwardButton
>;
export const VideoPlayerSeekBackwardButton = ({
  className,
  ...props
}: VideoPlayerSeekBackwardButtonProps) => (
  <MediaSeekBackwardButton
    noTooltip
    seekOffset={10}
    className={cn("p-2.5", className)}
    {...props}
  />
);
export type VideoPlayerSeekForwardButtonProps = ComponentProps<
  typeof MediaSeekForwardButton
>;
export const VideoPlayerSeekForwardButton = ({
  className,
  ...props
}: VideoPlayerSeekForwardButtonProps) => (
  <MediaSeekForwardButton
    noTooltip
    seekOffset={10}
    className={cn("p-2.5", className)}
    {...props}
  />
);
export type VideoPlayerMuteButtonProps = ComponentProps<typeof MediaMuteButton>;
export const VideoPlayerMuteButton = ({
  className,
  ...props
}: VideoPlayerMuteButtonProps) => (
  <MediaMuteButton noTooltip className={cn("p-2.5", className)} {...props} />
);

export type VideoPlayerFullscreenButtonProps = ComponentProps<
  typeof MediaFullscreenButton
>;

export const VideoPlayerFullscreenButton = ({
  className,
  ...props
}: VideoPlayerFullscreenButtonProps) => (
  <MediaFullscreenButton
    noTooltip
    className={cn("p-2.5", className)}
    {...props}
  />
);

export const VideoPlayerContent = forwardRef<
  HTMLVideoElement,
  ComponentProps<"video">
>(({ className, ...props }, ref) => (
  <video
    ref={ref}
    className={cn("mt-0 mb-0", className)}
    slot="media"
    crossOrigin="anonymous"
    {...props}
  />
));

VideoPlayerContent.displayName = "VideoPlayerContent";
