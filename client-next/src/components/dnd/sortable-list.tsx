"use client";

import { useCallback, useMemo, useState } from "react";
import {
  closestCenter,
  DndContext,
  DragEndEvent,
  DragOverlay,
  DragStartEvent,
  KeyboardSensor,
  PointerSensor,
  TouchSensor,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import {
  SortableContext,
  sortableKeyboardCoordinates,
} from "@dnd-kit/sortable";
import Item, { ItemTexts } from "@/components/dnd/item";
import SortableItemWrapper from "@/components/dnd/sortable-item";
import { FieldInputItem } from "@/components/forms/input-file";
import { ImageCropTexts } from "@/components/common/image-cropper";

export type SortableListType = "IMAGE" | "VIDEO";

export interface SortableItem {
  id: string | number;
  src: string;
}

interface Props {
  items: FieldInputItem[];
  type: SortableListType;
  moveItems: (
    items: FieldInputItem[],
    activeIndex: number,
    overIndex: number,
  ) => void;
  deleteItem: (id: string | number) => void;
  itemTexts: ItemTexts;
  multiple?: boolean;
  cropImage: (id: string | number, src: string, blob: Blob) => void;
  cropShape?: "rect" | "round";
  imageCropTexts: ImageCropTexts;
}

const sensorOptions = {
  activationConstraint: {
    distance: 10,
  },
};
const keyboardOptions = {
  coordinateGetter: sortableKeyboardCoordinates,
};

export default function SortableList({
  items,
  type,
  moveItems,
  deleteItem,
  itemTexts,
  multiple = true,
  cropImage,
  cropShape = "rect",
  imageCropTexts,
}: Props) {
  const [activeItem, setActiveItem] = useState<FieldInputItem>();

  const itemIds = useMemo(() => items.map((item) => item.id), [items]);
  const sensors = useSensors(
    useSensor(PointerSensor, sensorOptions),
    useSensor(TouchSensor, sensorOptions),
    useSensor(KeyboardSensor, keyboardOptions),
  );

  const handleDragStart = useCallback(
    (event: DragStartEvent) => {
      if (event.active.data.current?.type === "SortableList") {
        setActiveItem(items.find((item) => item.id === event.active.id));
      }
    },
    [items],
  );

  const handleDragEnd = useCallback(
    (event: DragEndEvent) => {
      const { active, over } = event;
      if (!over) return;

      const { activeIndex, overIndex } = items.reduce(
        (acc, item, index) => {
          if (item.id === active.id) acc.activeIndex = index;
          if (item.id === over.id) acc.overIndex = index;
          return acc;
        },
        { activeIndex: -1, overIndex: -1 },
      );

      if (activeIndex === -1 || overIndex === -1) return;

      if (activeIndex !== overIndex) {
        moveItems(items, activeIndex, overIndex);
      }
      setActiveItem(undefined);
    },
    [items, moveItems],
  );

  const handleDragCancel = useCallback(() => {
    setActiveItem(undefined);
  }, []);

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
      onDragCancel={handleDragCancel}
    >
      <SortableContext items={itemIds}>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 w-full max-w-[1200px] mx-auto mt-4 p-2 py-4 ">
          {items.map((item, i) => (
            <SortableItemWrapper
              key={item.id}
              item={item}
              type={type}
              index={i}
              deleteItem={deleteItem}
              itemCount={items.length}
              itemTexts={itemTexts}
              multiple={multiple}
              cropImage={cropImage}
              cropShape={cropShape}
              imageCropTexts={imageCropTexts}
            />
          ))}
        </div>
      </SortableContext>
      <DragOverlay adjustScale style={{ transformOrigin: "0 0" }}>
        {activeItem ? (
          <div className="animate-in fade-in duration-300">
            <Item
              item={activeItem}
              isDragging
              type={type}
              preview={true}
              itemTexts={itemTexts}
              imageCropTexts={imageCropTexts}
            />
          </div>
        ) : null}
      </DragOverlay>
    </DndContext>
  );
}
