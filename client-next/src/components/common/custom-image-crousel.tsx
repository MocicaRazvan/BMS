"use client";
import CustomMediaCarousel from "@/components/common/custom-media-carousel";

interface Props {
  images: string[];
}

export default function CustomImageCarousel({ images }: Props) {
  return (
    <CustomMediaCarousel
      media={images.map((src) => ({ type: "image", src }))}
    />
  );
}
