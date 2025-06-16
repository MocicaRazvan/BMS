import React, {
  CSSProperties,
  forwardRef,
  HTMLAttributes,
  useEffect,
  useRef,
  useState,
} from "react";
import { SortableListType } from "@/components/dnd/sortable-list";
import { cn } from "@/lib/utils";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import ImageCropper, {
  ImageCropperProps,
  ImageCropTexts,
} from "@/components/common/image-cropper";
import { FieldInputItem } from "@/components/forms/input-file";
import { Skeleton } from "@/components/ui/skeleton";
import Image from "next/image";
import TwoStepDeleteButton from "@/components/common/two-step-delete-button";
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

export interface ItemTexts {
  header: string;
  tooltipContent: string;
}

interface BaseItemProps {
  isDragging?: boolean;
  item: FieldInputItem;
}

type ImageItemProps = BaseItemProps &
  Partial<Pick<ImageCropperProps, "dialogOpenObserver" | "cropShape">> & {
    cropImage?: (id: string | number, src: string, blob: Blob) => void;
    imageCropTexts: ImageCropTexts;
    index?: number;
    itemTexts: ItemTexts;
    multiple?: boolean;
  };

type Props = {
  isOpacityEnabled?: boolean;
  type: SortableListType;
  preview?: boolean;
  deleteItem?: (id: string | number) => void;
  itemCount?: number;
  cropImage?: (id: string | number, src: string, blob: Blob) => void;
} & ImageItemProps &
  HTMLAttributes<HTMLDivElement>;

const Item = forwardRef<HTMLDivElement, Props>(
  (
    {
      multiple = true,
      preview = false,
      index,
      type,
      item,
      isOpacityEnabled,
      isDragging,
      style,
      itemCount,
      itemTexts,
      deleteItem,
      cropImage,
      dialogOpenObserver,
      cropShape = "rect",
      imageCropTexts,
      ...props
    },
    ref,
  ) => {
    const isListMoreThenOne = !itemCount || itemCount > 1;
    const styles: CSSProperties = {
      opacity: isOpacityEnabled ? "0.4" : "1",
      cursor: isListMoreThenOne
        ? isDragging
          ? "grabbing"
          : "grab"
        : "default",
      lineHeight: "0.5",
      transform: isDragging ? "scale(1.05)" : "scale(1)",
      ...style,
    };

    return (
      <>
        <div
          ref={ref}
          style={styles}
          {...props}
          className="relative hover:scale-105 transition-transform"
        >
          {type === "IMAGE" ? (
            <ItemImageContent
              item={item}
              isDragging={isDragging}
              dialogOpenObserver={dialogOpenObserver}
              cropShape={cropShape}
              index={index}
              cropImage={cropImage}
              itemTexts={itemTexts}
              imageCropTexts={imageCropTexts}
              multiple={multiple}
            />
          ) : (
            <ItemVideoContent item={item} isDragging={isDragging} />
          )}

          {deleteItem && (
            <TwoStepDeleteButton onClick={() => deleteItem(item.id)} />
          )}
        </div>
      </>
    );
  },
);
Item.displayName = "Item";

const ItemVideoContent = ({ item, isDragging }: BaseItemProps) => {
  const [videoSrc, setVideoSrc] = useState<string | null>(null);
  const videoRef = useRef<HTMLVideoElement>(null);
  const [isVideoFullScreen, setIsVideoFullScreen] = useState(false);

  useEffect(() => {
    if (item.src) {
      setVideoSrc(item.src);
    }
  }, [item.src]);

  useEffect(() => {
    const handleFullscreenChange = () => {
      const isFull =
        document.fullscreenElement === videoRef.current ||
        videoRef.current?.parentElement?.contains(document.fullscreenElement);
      setIsVideoFullScreen(!!isFull);
    };

    document.addEventListener("fullscreenchange", handleFullscreenChange);

    return () => {
      document.removeEventListener("fullscreenchange", handleFullscreenChange);
    };
  }, []);
  return videoSrc ? (
    <VideoPlayer
      className={cn(
        "rounded-lg max-w-100 w-full h-[250px]",
        isDragging ? "shadow-none" : "shadow-md",
      )}
    >
      <VideoPlayerContent
        ref={videoRef}
        src={videoSrc}
        preload="auto"
        autoPlay={false}
        className="size-full object-cover"
        loaderClassName="size-full object-cover"
      />
      <VideoPlayerLoadingIndicator />
      <VideoPlayerControlBar
        onMouseMove={(e) => {
          e.stopPropagation();
        }}
        className="flex w-full items-center justify-end"
      >
        {!isVideoFullScreen ? (
          <>
            <VideoPlayerFullscreenButton
              className={cn(isDragging && "hidden")}
            />
          </>
        ) : (
          <>
            <VideoPlayerPlayButton />
            <VideoPlayerSeekBackwardButton />
            <VideoPlayerSeekForwardButton />
            <VideoPlayerTimeRange />
            <VideoPlayerTimeDisplay />
            <VideoPlayerMuteButton />
            <VideoPlayerVolumeRange />
            <VideoPlayerFullscreenButton />
          </>
        )}
      </VideoPlayerControlBar>
    </VideoPlayer>
  ) : (
    <></>
  );
};

const ItemImageContent = ({
  item,
  isDragging,
  dialogOpenObserver,
  cropShape,
  index,
  cropImage,
  itemTexts: { header, tooltipContent },
  imageCropTexts,
  multiple,
}: ImageItemProps) => {
  const [isImageLoaded, setIsImageLoaded] = useState(false);

  return (
    <>
      <div
        className={cn(
          "relative w-full h-[250px]",
          isDragging ? "shadow-none" : "shadow-md",
          cropShape === "round" && "rounded-full max-w-[120px] h-[120px]",
        )}
      >
        {!isImageLoaded && (
          <Skeleton
            className={cn(
              "absolute inset-0 size-full",
              isDragging ? "shadow-none" : "shadow-md",
              cropShape === "round" && "rounded-full max-w-[120px] h-[120px]",
            )}
          />
        )}
        <Image
          src={item.src}
          alt={`${item.id}`}
          className={cn(
            "rounded-lg max-w-100 object-cover w-full  h-[250px]",
            isDragging ? "shadow-none" : "shadow-md",
            cropShape === "round" && "rounded-full max-w-[120px] h-[120px]",
          )}
          loading="eager"
          decoding="async"
          onLoad={() => setIsImageLoaded(true)}
          height={cropShape === "round" ? 120 : 250}
          width={cropShape === "round" ? 120 : 250}
          quality={70}
        />
      </div>
      {index === 0 && multiple && (
        <div className="absolute top-0.5 left-2 transition-all text-4xl">
          <TooltipProvider>
            <Tooltip>
              <TooltipTrigger className="bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/70 p-2 rounded-md">
                <p className="text-destructive">{header}</p>
              </TooltipTrigger>
              <TooltipContent
                className="bg-accent text-accent-foreground border w-1/3 mx-auto"
                side={"bottom"}
              >
                {tooltipContent}
              </TooltipContent>
            </Tooltip>
          </TooltipProvider>
        </div>
      )}

      {cropImage && (
        <div className="absolute bottom-1 right-1">
          <ImageCropper
            src={item.src}
            dialogOpenObserver={dialogOpenObserver}
            onCropComplete={(src, blob) => {
              setIsImageLoaded(false);
              cropImage(item.id, src, blob);
            }}
            cropShape={cropShape}
            texts={imageCropTexts}
            originalMime={item.file.type}
          />
        </div>
      )}
    </>
  );
};

export default Item;
