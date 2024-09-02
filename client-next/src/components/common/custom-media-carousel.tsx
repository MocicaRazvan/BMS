"use client";
import Image from "next/image";
import {
  Carousel,
  CarouselContent,
  CarouselItem,
  CarouselNext,
  CarouselPrevious,
} from "../ui/carousel";
import { useState } from "react";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Skeleton } from "@/components/ui/skeleton";
import CustomImage from "@/components/common/custom-image";

interface Media {
  type: "image" | "video";
  src: string;
}

interface Props {
  media: Media[];
}

export default function CustomMediaCarousel({ media }: Props) {
  const [isFullScreen, setIsFullScreen] = useState(false);
  const [currentImage, setCurrentImage] = useState<string | null>(null);

  const handleImageClick = (src: string) => {
    setIsFullScreen(true);
    setCurrentImage(src);
  };

  return (
    <div className="w-full flex justify-center items-center overflow-hidden">
      <Carousel className="w-full max-w-4xl">
        <CarouselContent>
          {media.map((item, i) => (
            <CarouselItem key={item.src + i}>
              <div
                className="rounded-lg overflow-hidden h-[450px] max-h-[450px] w-full cursor-grab relative"
                onClick={() => {
                  if (item.type === "image") {
                    handleImageClick(item.src);
                  }
                }}
              >
                {item.type === "image" ? (
                  <CustomImage
                    thumblinator
                    quality={100}
                    src={item.src}
                    alt="media image"
                    fill={true}
                    className="w-full max-w-[1000px] object-cover h-full"
                    priority={i === 0}
                    // width={1000}
                  />
                ) : (
                  <video
                    src={item.src}
                    controls
                    className="w-full max-w-[1000px] object-cover h-full "
                    preload="auto"
                  />
                )}
              </div>
            </CarouselItem>
          ))}
        </CarouselContent>
        <CarouselPrevious />
        <CarouselNext />
      </Carousel>
      {currentImage && (
        <Dialog open={isFullScreen} onOpenChange={setIsFullScreen}>
          <DialogContent
            className="h-[80vh] min-w-[90%] p-0 overflow-hidden"
            closeButtonClassName={
              "bg-background w-6 h-6 flex items-center justify-center font-bold"
            }
          >
            <CustomImage
              thumblinator
              quality={100}
              src={currentImage}
              alt="current image"
              fill={true}
              className="w-full object-cover h-full"
            />
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}
