"use client";

import {
  Control,
  FieldValues,
  Path,
  PathValue,
  useFormContext,
  useWatch,
} from "react-hook-form";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import React, { useCallback, useEffect, useState } from "react";
import { v4 as uuidv4 } from "uuid";
import SortableList, { SortableItem } from "@/components/dnd/sortable-list";
import { arrayMove } from "@dnd-kit/sortable";
import { useDropzone } from "react-dropzone";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { motion } from "framer-motion";
import { ItemTexts } from "@/components/dnd/item";
import { DownloadIcon, Loader2 } from "lucide-react";
import { UploadIcon } from "@radix-ui/react-icons";
import DotPattern from "@/components/magicui/dot-pattern";
import ProgressText from "@/components/common/progres-text";
import { ImageCropTexts } from "@/components/common/image-cropper";
import { saveAs } from "file-saver";
import JSZip from "jszip";

export type InputFieldName = "images" | "videos";

export interface FieldInputTexts {
  title: string;
  clearAll: string;
  draggingActive: string;
  draggingInactive: string;
  showList: string;
  hideList: string;
  itemTexts: ItemTexts;
  loadCount1: string;
  loadCountMany: string;
  loading: string;
  imageCropTexts: ImageCropTexts;
}

interface Props<T extends FieldValues> {
  control: Control<T>;
  fieldName: keyof T & string;
  fieldTexts: FieldInputTexts;
  multiple?: boolean;
  initialLength: number;
  parentListCollapsed?: boolean;
  loadingProgress?: number;
  cropShape?: "rect" | "round";
}

export interface FieldInputItem extends SortableItem {
  file: File;
}

const variantOne = {
  initial: {
    x: 20,
    y: -20,
    opacity: 1,
  },
  animate: {
    x: 20,
    y: -20,
    opacity: 1,
  },
};

const variantTwo = {
  initial: {
    opacity: 0,
  },
  animate: {
    opacity: 1,
  },
};

export default function InputFile<T extends FieldValues>({
  control,
  fieldName,
  fieldTexts: {
    draggingActive,
    draggingInactive,
    hideList,
    showList,
    title,
    clearAll,
    itemTexts,
    loadCount1,
    loadCountMany,
    loading,
    imageCropTexts,
  },
  initialLength = 0,
  multiple = true,
  parentListCollapsed,
  loadingProgress,
  cropShape = "rect",
}: Props<T>) {
  const fieldValue = useWatch({
    control,
    name: fieldName as Path<T>,
  });
  const { setValue, clearErrors, getValues } = useFormContext<T>();
  const [isListCollapsed, setIsListCollapsed] = useState(true);
  const [isLoadingInitial, setIsLoadingInitial] = useState(true);

  if (!(fieldName in getValues())) {
    throw new Error(`Invalid field name: ${fieldName}`);
  }

  useEffect(() => {
    if (typeof parentListCollapsed === "boolean") {
      setIsListCollapsed(parentListCollapsed);
    }
  }, [parentListCollapsed]);

  const onDrop = useCallback(
    (acceptedFiles: File[]) => {
      const mappedItems = acceptedFiles.map((file) => ({
        src: URL.createObjectURL(file),
        file,
        id: uuidv4(),
      }));

      setValue(
        fieldName as Path<T>,
        (multiple ? [...fieldValue, ...mappedItems] : mappedItems) as PathValue<
          T,
          Path<T>
        >,
      );

      clearErrors(fieldName as Path<T>);
    },
    [clearErrors, fieldName, fieldValue, multiple, setValue],
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: fieldName.includes("image")
      ? { "image/png": [".png"], "image/jpeg": [".jpg", ".jpeg"] }
      : { "video/mp4": [".mp4"] },
    multiple,
  });

  const moveItems = useCallback(
    (items: FieldInputItem[], activeIndex: number, overIndex: number) => {
      const newItems = arrayMove<FieldInputItem>(items, activeIndex, overIndex);
      setValue(fieldName as Path<T>, newItems as PathValue<T, Path<T>>);
    },
    [fieldName, setValue],
  );

  const deleteItem = useCallback(
    (id: string | number) => {
      const itemToRemove = fieldValue.find(
        (item: FieldInputItem) => item.id === id,
      );
      if (itemToRemove) {
        URL.revokeObjectURL(itemToRemove.src);
      }
      setValue(
        fieldName as Path<T>,
        fieldValue.filter(
          (item: FieldInputItem) => item.id !== id,
        ) as PathValue<T, Path<T>>,
      );
    },
    [fieldName, fieldValue, setValue],
  );

  const deleteAllItems = useCallback(() => {
    fieldValue.forEach((item: FieldInputItem) => {
      URL.revokeObjectURL(item.src);
    });
    setValue(fieldName as Path<T>, [] as PathValue<T, Path<T>>);
    clearErrors(fieldName as Path<T>);
  }, [clearErrors, fieldName, fieldValue, setValue]);

  const cropItem = useCallback(
    (id: number | string, src: string, blob: Blob) => {
      const itemToCrop = fieldValue.find(
        (item: FieldInputItem) => item.id === id,
      );
      if (itemToCrop) {
        const castedItem = itemToCrop as FieldInputItem;
        URL.revokeObjectURL(castedItem.src);
        castedItem.src = src;
        const ext = blob.type.split("/")[1];
        const base = castedItem.file.name.replace(/\.\w+$/, "");
        const newName = `cropped_${uuidv4()}_${base}.${ext}`;

        castedItem.file = new File([blob], newName, {
          type: blob.type,
        });

        const newItems = fieldValue.map((item: FieldInputItem) =>
          item.id === id ? castedItem : item,
        );

        setValue(fieldName as Path<T>, newItems as PathValue<T, Path<T>>);
      }
    },
    [fieldName, fieldValue, setValue],
  );

  const downloadAllFiles = useCallback(() => {
    const zip = new JSZip();
    const isImage = fieldName.includes("image");
    fieldValue.forEach((item: FieldInputItem) => {
      zip.file(item.file.name, item.file);
    });
    const now = new Date();
    zip
      .generateAsync({
        type: "blob",
        compression: isImage ? "DEFLATE" : "STORE",
        compressionOptions: isImage ? { level: 9 } : undefined,
      })
      .then((content) => {
        saveAs(content, `${fieldName}_${now.toISOString()}.zip`);
      });
  }, [fieldName, fieldValue]);

  useEffect(() => {
    if (fieldValue?.length === 0) {
      setIsListCollapsed(true);
    }
  }, [fieldValue?.length]);

  useEffect(() => {
    if (!(initialLength > 0 && fieldValue?.length === 0) && isLoadingInitial) {
      setIsLoadingInitial(false);
    }
  }, [fieldValue?.length, initialLength, isLoadingInitial]);

  return (
    <div className="w-full">
      <FormField
        control={control}
        name={fieldName as Path<T>}
        render={({ field: { value, onChange, ...fieldProps } }) => (
          <FormItem>
            <div className="flex  w-full h-full min-h-[50px] gap-5 justify-between items-center">
              <FormLabel className="capitalize sr-only">{title}</FormLabel>
              {fieldValue.length > 0 && (
                <Button
                  variant="outline"
                  onClick={downloadAllFiles}
                  type="button"
                >
                  <DownloadIcon />
                </Button>
              )}
              {multiple && fieldValue.length > 0 && (
                <Button
                  variant="destructive"
                  size="sm"
                  onClick={deleteAllItems}
                  disabled={isLoadingInitial}
                  className="md:text-[16px]"
                  type="button"
                >
                  {clearAll}
                </Button>
              )}
            </div>
            <FormControl>
              {isLoadingInitial ? (
                <div className="w-full flex items-center justify-center h-full gap-3 relative min-h-52 rounded-md">
                  <DotPattern
                    className={cn(
                      "[mask-image:radial-gradient(300px_circle_at_center,rgba(255,255,255,0.65),rgba(255,255,255,0.20))]",
                    )}
                  />
                  <Loader2 className=" h-16 w-16 text-primary/60 animate-spin" />
                  <p className="font-semibold">{loading}</p>
                  <ProgressText progress={loadingProgress} />
                </div>
              ) : (
                <div className="relative rounded-md pb-5">
                  <DotPattern
                    className={cn(
                      ` [mask-image:radial-gradient(300px_circle_at_center,rgba(255,255,255,0.65),rgba(255,255,255,0.20))]
                      md:[mask-image:radial-gradient(450px_circle_at_center,rgba(255,255,255,0.65),rgba(255,255,255,0.20))]
                      lg:[mask-image:radial-gradient(650px_circle_at_center,rgba(255,255,255,0.65),rgba(255,255,255,0.20))]
                      `,
                    )}
                  />
                  <div className="w-full h-full flex flex-col  items-center justify-between gap-5">
                    <div {...getRootProps()} className="w-full h-full ">
                      <motion.div
                        whileHover="animate"
                        className="p-6 group/file block rounded-lg cursor-pointer w-full relative overflow-hidden "
                      >
                        <div className="flex flex-col items-center justify-center ">
                          <p className="relative z-20 font-sans font-bold  capitalize text-xl md:text-2xl tracking-tighter">
                            {title}
                          </p>
                          <p className="relative z-20 font-sans font-normal text-neutral-400 dark:text-neutral-400 text-lg mt-2">
                            {draggingInactive}
                          </p>
                          <div className="flex flex-col items-center justify-center  relative w-full h-full">
                            <motion.div
                              layout={false}
                              variants={variantOne}
                              transition={{
                                type: "spring",
                                stiffness: 350,
                                damping: 20,
                              }}
                              className={cn(
                                "relative group-hover/file:shadow-2xl z-[35] bg-background supports-[backdrop-filter]:bg-primary/20  flex items-center justify-center h-36 mt-4 w-full max-w-[12rem] mx-auto rounded-md",
                                "shadow-[0px_10px_50px_rgba(0,0,0,0.1)]",
                              )}
                            >
                              {isDragActive ? (
                                <motion.p
                                  initial={{ opacity: 0 }}
                                  animate={{ opacity: 1 }}
                                  className="font-semibold text-center flex flex-col items-center w-full text-lg tracking-tighter"
                                >
                                  {draggingActive}
                                  <UploadIcon className="h-6 w-6 text-neutral-600 dark:text-neutral-400 font-semibold" />
                                </motion.p>
                              ) : (
                                <UploadIcon className="h-6 w-6 text-neutral-600 dark:text-neutral-300 font-semibold" />
                              )}
                            </motion.div>
                            <motion.div
                              variants={variantTwo}
                              className="absolute opacity-0 border border-dashed border-foreground inset-0 z-30 bg-transparent flex items-center justify-center h-36 mt-4 w-full max-w-[12rem] mx-auto rounded-md"
                            ></motion.div>
                          </div>
                        </div>
                      </motion.div>
                      <input {...getInputProps()} multiple={multiple} />
                    </div>
                    {fieldValue?.length > 0 && (
                      <Button
                        type="button"
                        className=" mt-5 md:mt-10 text-[16px]"
                        onClick={() => setIsListCollapsed((prev) => !prev)}
                      >
                        {isListCollapsed ? showList : hideList}
                      </Button>
                    )}
                  </div>
                  {multiple && fieldValue?.length > 0 && (
                    <p className="text-lg md:text-xl font-semibold tracking-tight">
                      {fieldValue.length === 1
                        ? loadCount1
                        : `${fieldValue.length}${loadCountMany}`}
                    </p>
                  )}
                </div>
              )}
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />
      {fieldValue?.length > 0 && (
        <motion.div
          initial={false}
          animate={{
            height: isListCollapsed ? 0 : "auto",
            opacity: isListCollapsed ? 0 : 1,
          }}
          transition={{ duration: 0.5 }}
          className="overflow-hidden w-full"
        >
          <SortableList
            items={fieldValue}
            moveItems={moveItems}
            deleteItem={deleteItem}
            type={fieldName.includes("image") ? "IMAGE" : "VIDEO"}
            itemTexts={itemTexts}
            multiple={multiple}
            cropImage={cropItem}
            cropShape={cropShape}
            imageCropTexts={imageCropTexts}
          />
        </motion.div>
      )}
    </div>
  );
}
