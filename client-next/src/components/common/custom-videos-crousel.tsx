"use client";
import CustomMediaCarousel from "@/components/common/custom-media-carousel";

interface Props {
  videos: string[];
}

export default function CustomVideoCarousel({ videos }: Props) {
  return (
    <CustomMediaCarousel
      media={videos.map((src) => ({ type: "video", src }))}
    />
  );
}
