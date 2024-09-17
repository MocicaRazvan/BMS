"use client";

import {
  BaseFormProps,
  getPlanSchema,
  PlanSchemaTexts,
  PlanSchemaType,
} from "@/types/forms";
import { WithUser } from "@/lib/user";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import React, {
  CSSProperties,
  forwardRef,
  HTMLAttributes,
  memo,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";
import { Option } from "@/components/ui/multiple-selector";
import { Card, CardContent, CardTitle } from "@/components/ui/card";
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { TitleBodyForm, TitleBodyTexts } from "@/components/forms/title-body";
import ChildInputMultipleSelector, {
  ChildInputMultipleSelectorTexts,
} from "@/components/forms/child-input-multipleselector";
import {
  CustomEntityModel,
  DayResponse,
  DietType,
  ObjectiveType,
  PageableResponse,
  PlanBody,
  planObjectives,
  PlanResponse,
} from "@/types/dto";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import InputFile, { FieldInputTexts } from "@/components/forms/input-file";
import { cn, handleBaseError } from "@/lib/utils";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import ErrorMessage from "@/components/forms/error-message";
import { fetchWithFiles } from "@/hoooks/fetchWithFiles";
import { BaseError } from "@/types/responses";
import { toast } from "@/components/ui/use-toast";
import { BaseFormTexts } from "@/texts/components/forms";
import useFilesBase64 from "@/hoooks/useFilesObjectURL";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { fetchStream } from "@/hoooks/fetchStream";
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { Grip, Trash2 } from "lucide-react";
import {
  closestCenter,
  DndContext,
  DragEndEvent,
  DraggableAttributes,
  DragOverlay,
  DragStartEvent,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import { AnimatePresence, motion } from "framer-motion";
import { SyntheticListenerMap } from "@dnd-kit/core/dist/hooks/utilities";
import { v4 as uuidv4 } from "uuid";
import useProgressWebSocket from "@/hoooks/useProgressWebSocket";
import UploadingProgress, {
  UploadingProgressTexts,
} from "@/components/forms/uploading-progress";

export interface PlanFormTexts {
  titleBodyTexts: TitleBodyTexts;
  childInputMultipleSelectorTexts: ChildInputMultipleSelectorTexts;
  imagesText: FieldInputTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  planSchemaTexts: PlanSchemaTexts;
  baseFormTexts: BaseFormTexts;
  priceLabel: string;
  pricePlaceholder: string;
  daysLabel: string;
  daysPlaceholder: string;
  objectiveLabel: string;
  objectivePlaceholder: string;
  dietMessage: string;
  dayIndex: string;
  objectives: Record<(typeof planObjectives)[number], string>;
  loadedImages: UploadingProgressTexts;
}
export interface PlanFormProps
  extends WithUser,
    PlanFormTexts,
    BaseFormProps,
    Partial<Omit<PlanSchemaType, "images">> {
  images?: string[];
  initialOptions?: (Option & { dragId: string })[];
}

export default function PlanForm({
  titleBodyTexts,
  authUser,
  childInputMultipleSelectorTexts,
  imagesText,
  buttonSubmitTexts,
  planSchemaTexts,
  baseFormTexts: { descriptionToast, toastAction, altToast, header, error },
  priceLabel,
  pricePlaceholder,
  objectiveLabel,
  objectivePlaceholder,
  daysLabel,
  daysPlaceholder,
  dietMessage,
  path,
  price = 0,
  title = "",
  body = "",
  days = [],
  objective = "",
  images = [],
  initialOptions = [],
  dayIndex,
  objectives,
  loadedImages,
}: PlanFormProps) {
  const planSchema = useMemo(
    () => getPlanSchema(planSchemaTexts),
    [planSchemaTexts],
  );
  const [clientId] = useState(uuidv4);
  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();

  const { messages: messagesImages } = useProgressWebSocket(
    authUser.token,
    clientId,
    "IMAGE",
  );

  const [selectedOptions, setSelectedOptions] = useState<
    (Option & { dragId: string })[]
  >(initialOptions || []);
  const [dietType, setDietType] = useState<DietType | null>();

  const form = useForm<PlanSchemaType>({
    resolver: zodResolver(planSchema),
    defaultValues: {
      price,
      title,
      days,
      objective,
      body,
      images: [],
    },
  });

  const daysWatch = form.watch("days");
  const watchImages = form.watch("images");

  useEffect(() => {
    if (daysWatch.length) {
      fetchStream<DietType>({
        path: "/meals/day/dietType",
        method: "GET",
        token: authUser.token,
        arrayQueryParam: {
          ids: daysWatch.map((d) => d.toString()),
        },
        successCallback: (diet) => {
          setDietType(diet);
        },
      });
    }
  }, [JSON.stringify(daysWatch)]);

  console.log("DAYS", form.getValues("days"));

  const { fileCleanup } = useFilesBase64({
    files: images,
    fieldName: "images",
    setValue: form.setValue,
    getValues: form.getValues,
  });

  useEffect(() => {
    return () => {
      fileCleanup();
    };
  }, [fileCleanup]);

  const moveDays = useCallback(
    (
      items: (Option & { dragId: string })[],
      activeIndex: number,
      overIndex: number,
    ) => {
      const newItems = arrayMove<Option & { dragId: string }>(
        items,
        activeIndex,
        overIndex,
      );
      setSelectedOptions(newItems);
      form.setValue(
        "days",
        newItems.map((o) => parseInt(o.value)),
      );
    },
    [form],
  );

  const deleteDay = useCallback(
    (item: Option & { dragId: string }) => {
      const newOptions = selectedOptions.filter(
        (o) => o.dragId !== item.dragId,
      );
      setSelectedOptions(newOptions);
      form.setValue(
        "days",
        newOptions.map((o) => parseInt(o.value)),
      );
    },
    [selectedOptions, form],
  );

  const onSubmit = useCallback(
    async (data: PlanSchemaType) => {
      if (!dietType) return;
      setIsLoading(true);
      setErrorMsg("");
      const body: PlanBody = {
        ...data,
        objective: data.objective as ObjectiveType,
        type: dietType,
      };
      const files = data.images.map((image) => image.file);
      try {
        const res = await fetchWithFiles<
          PlanBody,
          BaseError,
          CustomEntityModel<PlanResponse>
        >({
          // path: "/plans/createWithImages",
          path,
          token: authUser.token,
          data: {
            files,
            body,
          },
          clientId,
        });
        toast({
          title: data.title,
          description: descriptionToast,
          variant: "success",
          // action: (
          //   <ToastAction
          //     altText={altToast}
          //     onClick={() =>
          //       router.push(`/trainer/plans/single/${res.content.id}`)
          //     }
          //   >
          //     {toastAction}
          //   </ToastAction>
          // ),
        });
        router.push(`/trainer/plans/single/${res.content.id}`);
      } catch (e) {
        handleBaseError(e, setErrorMsg, error);
      } finally {
        setIsLoading(false);
      }
    },
    [
      altToast,
      authUser.token,
      descriptionToast,
      error,
      setErrorMsg,
      setIsLoading,
      toastAction,
      path,
      dietType,
      clientId,
      router,
    ],
  );

  console.log("selectedOptions", selectedOptions);
  return (
    <Card className="max-w-7xl w-full sm:px-2 md:px-5 py-6">
      <CardTitle className="font-bold text-2xl text-center capitalize">
        {header}
        {/*{title && <p className="inline ms-2">{title}</p>}*/}
      </CardTitle>
      <CardContent className="w-full">
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-8 lg:space-y-12 w-full"
            noValidate
          >
            <TitleBodyForm<PlanSchemaType>
              control={form.control}
              titleBodyTexts={titleBodyTexts}
            />

            <FormField
              control={form.control}
              name="objective"
              render={({ field }) => (
                <FormItem className="flex-1 w-full">
                  <FormLabel className="capitalize">{objectiveLabel}</FormLabel>
                  <Select
                    onValueChange={field.onChange}
                    defaultValue={field.value}
                  >
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder={objectivePlaceholder} />
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {planObjectives.map((value) => (
                        <SelectItem
                          key={value}
                          value={value}
                          className="cursor-pointer"
                        >
                          {objectives[value]}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>{" "}
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name={"days"}
              render={() => (
                <FormItem>
                  <FormLabel className={"capitalize"}>{daysLabel}</FormLabel>
                  <FormControl>
                    <ChildInputMultipleSelector<
                      PageableResponse<CustomEntityModel<DayResponse>>
                    >
                      disabled={false}
                      allowDuplicates={true}
                      path={`/days/trainer/filtered/${authUser.id}`}
                      sortingCriteria={{ title: "asc" }}
                      // extraQueryParams={{ approved: "true" }}
                      pageSize={20}
                      valueKey={"title"}
                      mapping={(r) => ({
                        value: r.content.content.id.toString(),
                        label: r.content.content.title,
                        // type: r.content.content.type,
                      })}
                      giveUnselectedValue={false}
                      value={selectedOptions}
                      onChange={(options) => {
                        console.log("options", options);
                        if (options.length > 0) {
                          form.clearErrors("days");
                        }
                        setSelectedOptions(
                          options.map((o, i) => ({
                            ...o,
                            dragId: o.value + "_" + i,
                          })),
                        );
                        form.setValue(
                          "days",
                          options.map((o) => parseInt(o.value)),
                        );
                      }}
                      authUser={authUser}
                      {...childInputMultipleSelectorTexts}
                    />
                  </FormControl>{" "}
                  {selectedOptions.length > 0 && <div></div>}
                  <FormMessage />
                  {selectedOptions.length > 0 && dietType && (
                    <FormDescription className="tex-lg">
                      {/*The diet will be for{" "}*/}
                      {dietMessage}
                      <span className="text-xl font-semibold ms-2">
                        {dietType}
                      </span>
                    </FormDescription>
                  )}
                </FormItem>
              )}
            />
            <DaySortableList
              items={selectedOptions}
              moveItems={moveDays}
              deleteItem={deleteDay}
              dayIndex={dayIndex}
            />
            {/*{selectedOptions.length > 0 && (*/}
            {/*  <span className="text-2xl font-semibold ms-2">*/}
            {/*    {determineMostRestrictiveDiet(*/}
            {/*      selectedOptions.map((o) => o.type as DietType),*/}
            {/*    )}*/}
            {/*  </span>*/}
            {/*)}*/}
            <FormField
              control={form.control}
              name={"price"}
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="capitalize">{priceLabel}</FormLabel>
                  <FormControl>
                    <Input
                      placeholder={pricePlaceholder}
                      type={"number"}
                      // min={1}
                      {...field}
                      // value={field.value || ""}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <InputFile<PlanSchemaType>
              control={form.control}
              fieldName={"images"}
              fieldTexts={imagesText}
              initialLength={images?.length || 0}
            />
            <ErrorMessage message={error} show={!!errorMsg} />
            <ButtonSubmit
              isLoading={isLoading}
              disable={false}
              buttonSubmitTexts={buttonSubmitTexts}
            />
            {isLoading && (
              <UploadingProgress
                total={watchImages.length}
                loaded={messagesImages.length}
                {...loadedImages}
              />
            )}
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}
//todo delete day from here also and make desing
interface DaySortableListProps {
  items: (Option & { dragId: string })[];
  moveItems: (
    items: (Option & { dragId: string })[],
    activeIndex: number,
    overIndex: number,
  ) => void;
  deleteItem: (item: Option & { dragId: string }) => void;
  dayIndex: string;
}
function DaySortableList({
  items,
  moveItems,
  deleteItem,
  dayIndex,
}: DaySortableListProps) {
  const [activeItem, setActiveItem] = useState<Option & { dragId: string }>();
  const itemIds = useMemo(() => items.map((item) => item.dragId), [items]);
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 10,
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    }),
  );

  const handleDragStart = useCallback(
    (event: DragStartEvent) => {
      if (event.active.data.current?.type === "SortableDay") {
        setActiveItem(items.find((item) => item.dragId === event.active.id));
      }
    },
    [items],
  );

  const handleDragEnd = useCallback(
    (event: DragEndEvent) => {
      const { active, over } = event;
      if (!over) return;

      const activeItem = items.find((item) => item.dragId === active.id);
      const overItem = items.find((item) => item.dragId === over.id);

      if (!activeItem || !overItem) {
        return;
      }

      const activeIndex = items.findIndex((item) => item.dragId === active.id);
      const overIndex = items.findIndex((item) => item.dragId === over.id);

      if (activeIndex !== overIndex) {
        // setItems((prev) =>
        //   arrayMove<SortableItem>(prev, activeIndex, overIndex),
        // );
        moveItems(items, activeIndex, overIndex);
      }
      setActiveItem(undefined);
    },
    [items, moveItems],
  );
  const handleDragCancel = useCallback(() => {
    setActiveItem(undefined);
  }, []);

  if (items.length === 0) return null;

  return (
    <AnimatePresence>
      <motion.div
        initial={{
          height: 0,
          opacity: 0,
        }}
        animate={{
          height: "auto",
          opacity: 1,
        }}
        exit={{
          height: 0,
          opacity: 0,
        }}
        transition={{ duration: 0.5 }}
        className="overflow-hidden w-full "
      >
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragStart={handleDragStart}
          onDragEnd={handleDragEnd}
          onDragCancel={handleDragCancel}
        >
          <SortableContext items={itemIds}>
            <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4 w-full max-w-[1200px] mx-auto mt-4 p-2 py-4 ">
              {items.map((item, i) => (
                <SortableDayItem
                  key={item.dragId}
                  item={item}
                  itemCount={items.length}
                  index={i}
                  deleteItem={deleteItem}
                  dayIndex={dayIndex}
                />
              ))}
            </div>
          </SortableContext>
          <DragOverlay adjustScale style={{ transformOrigin: "0 0 " }}>
            {activeItem ? <DayItem item={activeItem} isDragging /> : null}
          </DragOverlay>
        </DndContext>
      </motion.div>
    </AnimatePresence>
  );
}

interface SortableDayItemProps extends HTMLAttributes<HTMLDivElement> {
  item: Option & { dragId: string };
  itemCount: number | undefined;
  preview?: boolean;
  index: number;
  deleteItem: (item: Option & { dragId: string }) => void;
  dayIndex: string;
}
const SortableDayItem = memo(
  ({ itemCount, item, ...props }: SortableDayItemProps) => {
    const {
      attributes,
      isDragging,
      listeners,
      setNodeRef,
      transform,
      transition,
    } = useSortable({
      id: item.dragId,
      data: {
        type: "SortableDay",
      },
      disabled: itemCount === 1,
    });

    const styles = {
      transform: CSS.Transform.toString(transform),
      transition: transition,
    };

    return (
      // <div ref={setNodeRef} style={styles} {...props}>
      //   <div {...attributes} {...listeners}>
      //     <Grip />
      //   </div>
      //   Day Item
      // </div>

      <DayItem
        item={item}
        ref={setNodeRef}
        style={styles}
        {...props}
        isOpacityEnabled={isDragging}
        attributes={attributes}
        listeners={listeners}
        itemCount={itemCount}
      />
    );
  },
);
SortableDayItem.displayName = "SortableDayItem";
interface DayItemProps extends HTMLAttributes<HTMLDivElement> {
  isOpacityEnabled?: boolean;
  isDragging?: boolean;
  itemCount?: number;
  index?: number;
  item: Option & { dragId: string };
  deleteItem?: (item: Option & { dragId: string }) => void;
  attributes?: DraggableAttributes;
  listeners?: SyntheticListenerMap | undefined;
  dayIndex?: string;
}

const DayItem = forwardRef<HTMLDivElement, DayItemProps>(
  (
    {
      deleteItem,
      index,
      itemCount,
      isDragging,
      isOpacityEnabled,
      style,
      item,
      listeners,
      attributes,
      dayIndex,
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
          : "default"
        : "default",
      lineHeight: "0.5",
      // transform: isDragging ? "scale(1.05)" : "scale(1)",
      ...style,
    };

    return (
      <div
        ref={ref}
        style={styles}
        {...props}
        className={cn(
          "relative  h-[170px] w-full flex flex-col border-2 rounded-xl bg-background/60",
        )}
      >
        <div className={"flex items-center justify-between w-full p-2"}>
          {listeners && attributes && (
            <div {...listeners} {...attributes}>
              <Grip />
            </div>
          )}
          {deleteItem && (
            <Button
              size={"icon"}
              variant={"destructive"}
              onClick={() => deleteItem(item)}
            >
              <Trash2 />
            </Button>
          )}
        </div>
        <div className="flex-1 flex flex-col items-center justify-between p-4">
          {index !== undefined && dayIndex && (
            <p className="font-semibold">
              {dayIndex} {index + 1}
            </p>
          )}
          <p className="text-lg font-bold">{item.label}</p>
        </div>
      </div>
    );
  },
);

DayItem.displayName = "DayItem";
