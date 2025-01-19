"use client";
import {
  Carousel,
  CarouselApi,
  CarouselContent,
  CarouselItem,
  CarouselNext,
  CarouselPrevious,
} from "../ui/carousel";
import { useCallback, useEffect, useRef, useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import CustomImage from "@/components/common/custom-image";
import CarouselVideo from "@/components/common/carousel-video";
import { VisuallyHidden } from "@radix-ui/react-visually-hidden";

export interface Media {
  type: "image" | "video";
  src: string;
}

interface Props {
  media: Media[];
}

export default function CustomMediaCarousel({ media }: Props) {
  const [isFullScreen, setIsFullScreen] = useState(false);
  const [currentImage, setCurrentImage] = useState<string | null>(null);
  const [carouselApi, setCarouselApi] = useState<CarouselApi>(undefined);
  const [activeIndex, setActiveIndex] = useState<number>(0); // Track active index

  const videoRefs = useRef<(HTMLVideoElement | null)[]>(
    new Array(media.length).fill(null),
  );
  const handleImageClick = (src: string) => {
    setIsFullScreen(true);
    setCurrentImage(src);
  };

  useEffect(() => {
    if (!carouselApi) return;

    const handleSlideChange = () => {
      if (!carouselApi) return;
      videoRefs.current?.forEach((video) => {
        if (video) {
          video.pause();
        }
      });
    };

    carouselApi.on("select", handleSlideChange);

    return () => {
      carouselApi.off("select", handleSlideChange);
    };
  }, [carouselApi]);

  const scrollPrev = useCallback(
    () => setActiveIndex((prev) => Math.max(prev - 1, 0)),
    [],
  );
  const scrollNext = useCallback(
    () => setActiveIndex((prev) => Math.min(prev + 1, media.length - 1)),
    [media.length],
  );

  const setVideoRef = useCallback(
    (index: number, video: HTMLVideoElement | null) => {
      videoRefs.current[index] = video;
    },
    [videoRefs],
  );
  console.log("activeIndex", activeIndex);
  return (
    <div className="w-full flex justify-center items-center overflow-hidden">
      <Carousel
        className="w-full max-w-4xl"
        setApi={setCarouselApi}
        scrollNextCallback={scrollNext}
        scrollPrevCallback={scrollPrev}
      >
        <CarouselContent>
          {media.map((item, i) => {
            return (
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
                      sizes="(max-width: 640px) 100vw, (max-width: 768px) 100vw, (max-width: 1024px) 100vw, 1000px"
                    />
                  ) : (
                    <CarouselVideo
                      item={{
                        type: "video",
                        src: item.src,
                      }}
                      index={i}
                      setVideoRef={setVideoRef}
                      activeIndex={activeIndex}
                    />
                  )}
                </div>
              </CarouselItem>
            );
          })}
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
            <VisuallyHidden>
              <DialogTitle>{"Image"}</DialogTitle>
              <DialogDescription>{currentImage}</DialogDescription>
            </VisuallyHidden>
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
