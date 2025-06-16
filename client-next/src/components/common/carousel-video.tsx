"use client";

import React, { useEffect, useLayoutEffect, useRef } from "react";
import { useInView } from "framer-motion";
import {
  VideoPlayer,
  VideoPlayerContent,
  VideoPlayerControlBar,
  VideoPlayerFullscreenButton,
  VideoPlayerLoadingIndicator,
  VideoPlayerMuteButton,
  VideoPlayerPlayButton,
  VideoPlayerSeekBackwardButton,
  VideoPlayerSeekForwardButton,
  VideoPlayerTimeDisplay,
  VideoPlayerTimeRange,
  VideoPlayerVolumeRange,
} from "@/components/ui/video-player";

interface Props {
  item: {
    type: "video";
    src: string;
  };
  setVideoRef: (index: number, video: HTMLVideoElement | null) => void;
  index: number;
  activeIndex: number;
}
export default function CarouselVideo({
  item,
  setVideoRef,
  index,
  activeIndex,
}: Props) {
  const ref = useRef(null);
  const videoRef = useRef<HTMLVideoElement>(null);
  const isInView = useInView(ref, {
    margin: "300px",
    once: true,
    amount: "some",
  });

  useEffect(() => {
    const isCurIndex = activeIndex === index;
    if (
      isInView &&
      videoRef.current &&
      // isCurOrNext &&
      !(videoRef.current.getAttribute("preload") === "auto")
    ) {
      console.log("preload video with index", index, isInView);
      videoRef.current.setAttribute("preload", "auto");
    }

    if (
      isInView &&
      isCurIndex &&
      videoRef.current &&
      videoRef.current.getAttribute("fetchPriority") !== "high"
    ) {
      videoRef.current.setAttribute("fetchPriority", "high");
    } else if (
      !isCurIndex &&
      videoRef.current &&
      videoRef.current.getAttribute("fetchPriority") !== "low"
    ) {
      videoRef.current.setAttribute("fetchPriority", "low");
    }
  }, [activeIndex, index, isInView]);

  useLayoutEffect(() => {
    setVideoRef(index, videoRef.current);
    return () => {
      setVideoRef(index, null);
    };
  }, [index, setVideoRef]);

  return (
    <div ref={ref} className="w-full h-full max-w-[1000px]">
      {/*<video*/}
      {/*  src={item.src}*/}
      {/*  controls*/}
      {/*  className="w-full max-w-[1000px] object-cover h-full "*/}
      {/*  preload="metadata"*/}
      {/*  ref={videoRef}*/}
      {/*/>*/}
      <VideoPlayer className="w-full max-w-[1000px] object-cover h-full overflow-hidden">
        <VideoPlayerContent
          src={item.src}
          autoPlay={false}
          preload={index === 0 ? "auto" : "metadata"}
          ref={videoRef}
          loaderClassName="size-full"
        />
        <VideoPlayerLoadingIndicator />
        <VideoPlayerControlBar
          onMouseMove={(e) => {
            e.stopPropagation();
          }}
        >
          <VideoPlayerPlayButton />
          <VideoPlayerSeekBackwardButton />
          <VideoPlayerSeekForwardButton />
          <VideoPlayerTimeRange />
          <VideoPlayerTimeDisplay />
          <VideoPlayerMuteButton />
          <VideoPlayerVolumeRange />
          <VideoPlayerFullscreenButton />
        </VideoPlayerControlBar>
      </VideoPlayer>
    </div>
  );
}
