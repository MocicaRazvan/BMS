"use client";
import { SortableListType } from "@/components/dnd/sortable-list";
import { HTMLAttributes, useState } from "react";
import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import Item, { ItemTexts } from "@/components/dnd/item";
import { ImageCropTexts } from "@/components/common/image-cropper";
import { FieldInputItem } from "@/components/forms/input-file";

type Props = {
  item: FieldInputItem;
  type: SortableListType;
  index: number;
  deleteItem: (id: string | number) => void;
  itemCount: number;
  itemTexts: ItemTexts;
  multiple?: boolean;
  cropImage?: (id: string | number, src: string, blob: Blob) => void;
  cropShape?: "rect" | "round";
  imageCropTexts: ImageCropTexts;
} & HTMLAttributes<HTMLDivElement>;
const SortableItemWrapper = ({
  item,
  itemCount,
  cropShape = "rect",
  ...props
}: Props) => {
  const [isCropActive, setIsCropActive] = useState<boolean>(false);
  const {
    attributes,
    isDragging,
    listeners,
    setNodeRef,
    transform,
    transition,
  } = useSortable({
    id: item.id,
    data: {
      type: "SortableList",
    },
    disabled: itemCount === 1 || isCropActive || !props.multiple,
  });

  const styles = {
    transform: CSS.Transform.toString(transform),
    transition: transition || undefined,
  };

  return (
    <Item
      item={item}
      ref={setNodeRef}
      dialogOpenObserver={setIsCropActive}
      style={styles}
      isOpacityEnabled={isDragging}
      {...props}
      {...attributes}
      {...listeners}
      itemCount={itemCount}
      cropShape={cropShape}
    />
  );
};

export default SortableItemWrapper;
