import { SortableItem, SortableListType } from "@/components/dnd/sortable-list";
import { HTMLAttributes } from "react";
import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import Item, { ItemTexts } from "@/components/dnd/item";

type Props = {
  item: SortableItem;
  type: SortableListType;
  index: number;
  deleteItem: (id: string | number) => void;
  itemCount: number;
  itemTexts: ItemTexts;
  multiple?: boolean;
} & HTMLAttributes<HTMLDivElement>;
const SortableItemWrapper = ({ item, itemCount, ...props }: Props) => {
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
    disabled: itemCount === 1 || !props.multiple,
  });

  const styles = {
    transform: CSS.Transform.toString(transform),
    transition: transition || undefined,
  };

  return (
    <Item
      item={item}
      ref={setNodeRef}
      style={styles}
      isOpacityEnabled={isDragging}
      {...props}
      {...attributes}
      {...listeners}
      itemCount={itemCount}
    />
  );
};

export default SortableItemWrapper;
