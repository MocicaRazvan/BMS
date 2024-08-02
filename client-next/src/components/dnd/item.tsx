import { CSSProperties, forwardRef, HTMLAttributes } from "react";
import { SortableItem, SortableListType } from "@/components/dnd/sortable-list";
import Image from "next/image";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Trash2 } from "lucide-react";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";

type Props = {
  item: SortableItem;
  isOpacityEnabled?: boolean;
  isDragging?: boolean;
  type: SortableListType;
  index?: number;
  preview?: boolean;
  deleteItem?: (id: string | number) => void;
  itemCount?: number;
  itemTexts: ItemTexts;
  multiple?: boolean;
} & HTMLAttributes<HTMLDivElement>;

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
            <Image
              src={item.src}
              alt={`${item.id}`}
              className={cn(
                "rounded-lg max-w-100 object-cover w-full h-full h-[250px]",
                isDragging ? "shadow-none" : "shadow-md",
              )}
              height={0}
              width={0}
            />
          ) : (
            <video
              src={item.src}
              controls={!preview}
              preload={"auto"}
              className={cn(
                "rounded-lg max-w-100 object-cover w-full  h-[250px]",
                isDragging ? "shadow-none" : "shadow-md",
              )}
            />
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
          {props?.deleteItem && (
            <Button
              size={"icon"}
              variant={"destructive"}
              className="absolute top-1 right-2"
              onClick={(e) => {
                e.stopPropagation();
                props?.deleteItem?.(item.id);
              }}
            >
              <Trash2 />
            </Button>
          )}
        </div>
      </>
    );
  },
);
Item.displayName = "Item";
export default Item;
