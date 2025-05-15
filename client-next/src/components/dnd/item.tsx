import {
  CSSProperties,
  forwardRef,
  HTMLAttributes,
  useEffect,
  useState,
} from "react";
import { SortableListType } from "@/components/dnd/sortable-list";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { CircleAlert, Trash2 } from "lucide-react";
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
import { useDebounce } from "react-use";
import { FieldInputItem } from "@/components/forms/input-file";
import { Skeleton } from "@/components/ui/skeleton";
import Image from "next/image";

type Props = {
  item: FieldInputItem;
  isOpacityEnabled?: boolean;
  isDragging?: boolean;
  type: SortableListType;
  index?: number;
  preview?: boolean;
  deleteItem?: (id: string | number) => void;
  itemCount?: number;
  itemTexts: ItemTexts;
  multiple?: boolean;
  cropImage?: (id: string | number, src: string, blob: Blob) => void;
  imageCropTexts: ImageCropTexts;
} & HTMLAttributes<HTMLDivElement> &
  Partial<Pick<ImageCropperProps, "dialogOpenObserver" | "cropShape">>;

export interface ItemTexts {
  header: string;
  tooltipContent: string;
}

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
      itemTexts: { header, tooltipContent },
      deleteItem,
      cropImage,
      dialogOpenObserver,
      cropShape = "rect",
      imageCropTexts,
      ...props
    },
    ref,
  ) => {
    const [videoSrc, setVideoSrc] = useState<string | null>(null);
    const [isDeletePressed, setIsDeletePressed] = useState(false);
    const [isImageLoaded, setIsImageLoaded] = useState(false);

    useDebounce(
      () => {
        if (isDeletePressed) {
          setIsDeletePressed(false);
        }
      },
      3000,
      [isDeletePressed],
    );

    useEffect(() => {
      if (type === "VIDEO" && item.src) {
        setVideoSrc(item.src);
      }
    }, [item.src, type]);

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
                    cropShape === "round" &&
                      "rounded-full max-w-[120px] h-[120px]",
                  )}
                />
              )}
              <Image
                src={item.src}
                alt={`${item.id}`}
                className={cn(
                  "rounded-lg max-w-100 object-cover w-full  h-[250px]",
                  isDragging ? "shadow-none" : "shadow-md",
                  cropShape === "round" &&
                    "rounded-full max-w-[120px] h-[120px]",
                )}
                loading="eager"
                decoding="async"
                onLoad={() => setIsImageLoaded(true)}
                height={cropShape === "round" ? 120 : 250}
                width={cropShape === "round" ? 120 : 250}
                quality={70}
              />
            </div>
          ) : (
            videoSrc && (
              <video
                src={videoSrc}
                controls={!preview}
                preload="auto"
                // typeof={"blob"}
                className={cn(
                  "rounded-lg max-w-100 object-cover w-full  h-[250px]",
                  isDragging ? "shadow-none" : "shadow-md",
                )}
                onError={() => {
                  console.error(`Failed to load video: ${item.src}`);
                }}
                autoPlay={false}
              />
            )
          )}
          {index === 0 && multiple && type === "IMAGE" && (
            <div className="absolute top-1 left-2 transition-all text-4xl">
              <TooltipProvider>
                <Tooltip>
                  <TooltipTrigger className="bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/70 p-2 rounded-md">
                    <p className="text-destructive ">{header}</p>
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
          {deleteItem &&
            (!isDeletePressed ? (
              <Button
                size="icon"
                variant="destructive"
                className="absolute top-1 right-2"
                onClick={(e) => {
                  e.stopPropagation();
                  setIsDeletePressed(true);
                }}
                type="button"
              >
                <Trash2 />
              </Button>
            ) : (
              <Button
                size="icon"
                variant="amber"
                className="absolute top-1 right-2"
                onClick={(e) => {
                  e.stopPropagation();
                  deleteItem?.(item.id);
                }}
                type="button"
              >
                <CircleAlert />
              </Button>
            ))}
          {type === "IMAGE" && cropImage && (
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
        </div>
      </>
    );
  },
);
Item.displayName = "Item";
export default Item;
