// "use client";
//
// import {
//   Control,
//   FieldValues,
//   Path,
//   PathValue,
//   useFormContext,
//   useWatch,
// } from "react-hook-form";
// import {
//   FormControl,
//   FormField,
//   FormItem,
//   FormLabel,
//   FormMessage,
// } from "@/components/ui/form";
// import { Input } from "@/components/ui/input";
// import { ChangeEvent, useCallback, useEffect, useState } from "react";
// import { v4 as uuidv4 } from "uuid";
// import SortableList, { SortableItem } from "@/components/dnd/sortable-list";
// import { arrayMove } from "@dnd-kit/sortable";
// import { useDropzone } from "react-dropzone";
// import { cn } from "@/lib/utils";
// import { Button } from "@/components/ui/button";
// import { motion, AnimatePresence } from "framer-motion";
// import { ItemTexts } from "@/components/dnd/item";
//
// export type InputFieldName = "images" | "videos";
//
// export interface FieldInputTexts {
//   title: string;
//   clearAll: string;
//   draggingActive: string;
//   draggingInactive: string;
//   showList: string;
//   hideList: string;
//   itemTexts: ItemTexts;
//   loadCount1: string;
//   loadCountMany: string;
// }
//
// interface Props<T extends FieldValues> {
//   control: Control<T>;
//   fieldName: keyof T & string;
//   fieldTexts: FieldInputTexts;
//   multiple?: boolean;
// }
//
// export interface FieldInputItem extends SortableItem {
//   file: File;
// }
//
// export default function InputFile<T extends FieldValues>({
//   control,
//   fieldName,
//   fieldTexts: {
//     draggingActive,
//     draggingInactive,
//     hideList,
//     showList,
//     title,
//     clearAll,
//     itemTexts,
//     loadCount1,
//     loadCountMany,
//   },
//   multiple = true,
// }: Props<T>) {
//   const fieldValue = useWatch({
//     control,
//     name: fieldName as Path<T>,
//   });
//   const { setValue, clearErrors, getValues } = useFormContext<T>();
//   const [isListCollapsed, setIsListCollapsed] = useState(true);
//
//   if (!(fieldName in getValues())) {
//     throw new Error(`Invalid field name: ${fieldName}`);
//   }
//
//   const onDrop = useCallback(
//     (acceptedFiles: File[]) => {
//       const mediaSrcs = acceptedFiles.map((file) => {
//         const reader = new FileReader();
//         return new Promise<{ src: string; file: File }>((resolve) => {
//           reader.onload = (event) => {
//             resolve({ src: event?.target?.result as string, file });
//           };
//           reader.readAsDataURL(file);
//         });
//       });
//       Promise.all(mediaSrcs).then((items) => {
//         const mappedItems = items.map((item) => ({
//           src: item.src,
//           file: item.file,
//           id: uuidv4(),
//         }));
//         console.log("mappedItems", mappedItems);
//         console.log("mappedItems FV", fieldValue);
//
//         setValue(
//           fieldName as Path<T>,
//           (multiple
//             ? [...fieldValue, ...mappedItems]
//             : mappedItems) as PathValue<T, Path<T>>,
//         );
//
//         clearErrors(fieldName as Path<T>);
//       });
//     },
//     [clearErrors, fieldName, fieldValue, multiple, setValue],
//   );
//
//   const { getRootProps, getInputProps, isDragActive } = useDropzone({
//     onDrop,
//     accept: fieldName.includes("image")
//       ? { "image/*": [".jpeg", ".jpg", ".png", ".gif"] }
//       : { "video/mp4": [".mp4"] },
//     multiple,
//   });
//
//   const moveItems = useCallback(
//     (items: FieldInputItem[], activeIndex: number, overIndex: number) => {
//       const newItems = arrayMove<FieldInputItem>(items, activeIndex, overIndex);
//       setValue(fieldName as Path<T>, newItems as PathValue<T, Path<T>>);
//     },
//     [fieldName, setValue],
//   );
//
//   const deleteItem = useCallback(
//     (id: string | number) => {
//       setValue(
//         fieldName as Path<T>,
//         fieldValue.filter(
//           (item: FieldInputItem) => item.id !== id,
//         ) as PathValue<T, Path<T>>,
//       );
//     },
//     [fieldName, fieldValue, setValue],
//   );
//
//   const deleteAllItems = useCallback(() => {
//     setValue(fieldName as Path<T>, [] as PathValue<T, Path<T>>);
//     clearErrors(fieldName as Path<T>);
//   }, [clearErrors, fieldName, setValue]);
//
//   useEffect(() => {
//     if (fieldValue?.length === 0) {
//       setIsListCollapsed(true);
//     }
//   }, [fieldValue?.length]);
//
//   return (
//     <div className="w-full ">
//       <FormField
//         control={control}
//         name={fieldName as Path<T>}
//         render={({ field: { value, onChange, ...fieldProps } }) => (
//           <FormItem>
//             <div className="flex w-full h-full min-h-[50px] justify-between items-center">
//               <FormLabel className="capitalize">{title}</FormLabel>
//               {multiple && fieldValue.length > 0 && (
//                 <Button
//                   variant={"destructive"}
//                   size={"sm"}
//                   onClick={deleteAllItems}
//                 >
//                   {/*Clear All{" "}*/}
//                   {clearAll}
//                 </Button>
//               )}
//             </div>
//             <FormControl>
//               <div>
//                 <div className="w-full h-full flex items-center justify-between gap-5">
//                   <div
//                     {...getRootProps()}
//                     className={cn(
//                       "border-2 border-dashed  p-6 cursor-pointer mx-auto  my-2 rounded-md flex-1",
//                       {
//                         "bg-accent": isDragActive,
//                       },
//                     )}
//                   >
//                     <input {...getInputProps()} multiple={multiple} />
//                     {isDragActive ? (
//                       <p className="text-center">
//                         {/*Drop the files here ...*/}
//                         {draggingActive}
//                       </p>
//                     ) : (
//                       <p className="text-center">
//                         {/*Drag 'n' drop some files here, or click to select files*/}
//                         {draggingInactive}
//                       </p>
//                     )}
//                   </div>
//                   {fieldValue?.length > 0 && (
//                     <Button
//                       type="button"
//                       onClick={() => setIsListCollapsed((prev) => !prev)}
//                     >
//                       {isListCollapsed
//                         ? // "Show"
//                           showList
//                         : // "Hide"
//                           hideList}
//                     </Button>
//                   )}
//                 </div>
//                 {multiple && fieldValue?.length > 0 && (
//                   <p>
//                     {fieldValue.length === 1
//                       ? loadCount1
//                       : `${fieldValue.length}${loadCountMany}`}
//                   </p>
//                 )}
//               </div>
//             </FormControl>
//
//             <FormMessage />
//           </FormItem>
//         )}
//       />
//       {fieldValue?.length > 0 && (
//         <motion.div
//           initial={false}
//           animate={{
//             height: isListCollapsed ? 0 : "auto",
//             opacity: isListCollapsed ? 0 : 1,
//           }}
//           transition={{ duration: 0.5 }}
//           className="overflow-hidden w-full "
//         >
//           <SortableList
//             items={fieldValue}
//             moveItems={moveItems}
//             deleteItem={deleteItem}
//             type={fieldName.includes("image") ? "IMAGE" : "VIDEO"}
//             itemTexts={itemTexts}
//             multiple={multiple}
//           />
//         </motion.div>
//       )}
//     </div>
//   );
// }
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
import { Input } from "@/components/ui/input";
import { ChangeEvent, useCallback, useEffect, useState } from "react";
import { v4 as uuidv4 } from "uuid";
import SortableList, { SortableItem } from "@/components/dnd/sortable-list";
import { arrayMove } from "@dnd-kit/sortable";
import { useDropzone } from "react-dropzone";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { motion } from "framer-motion";
import { ItemTexts } from "@/components/dnd/item";
import Loader from "@/components/ui/spinner";
import { Loader2 } from "lucide-react";

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
}

interface Props<T extends FieldValues> {
  control: Control<T>;
  fieldName: keyof T & string;
  fieldTexts: FieldInputTexts;
  multiple?: boolean;
  initialLength: number;
}

export interface FieldInputItem extends SortableItem {
  file: File;
  src: string;
}

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
  },
  initialLength = 0,
  multiple = true,
}: Props<T>) {
  const fieldValue = useWatch({
    control,
    name: fieldName as Path<T>,
  });
  const { setValue, clearErrors, getValues } = useFormContext<T>();
  const [isListCollapsed, setIsListCollapsed] = useState(true);

  if (!(fieldName in getValues())) {
    throw new Error(`Invalid field name: ${fieldName}`);
  }

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
      ? { "image/*": [".jpeg", ".jpg", ".png", ".gif"] }
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

  useEffect(() => {
    if (fieldValue?.length === 0) {
      setIsListCollapsed(true);
    }
  }, [fieldValue?.length]);

  return (
    <div className="w-full">
      <FormField
        control={control}
        name={fieldName as Path<T>}
        render={({ field: { value, onChange, ...fieldProps } }) => (
          <FormItem>
            <div className="flex w-full h-full min-h-[50px] justify-between items-center">
              <FormLabel className="capitalize">{title}</FormLabel>
              {multiple && fieldValue.length > 0 && (
                <Button
                  variant={"destructive"}
                  size={"sm"}
                  onClick={deleteAllItems}
                  disabled={initialLength > 0 && fieldValue?.length === 0}
                >
                  {clearAll}
                </Button>
              )}
            </div>
            <FormControl>
              {initialLength > 0 && fieldValue?.length === 0 ? (
                <div className="w-full flex items-center justify-center h-full gap-3">
                  <Loader2 className=" h-16 w-16 text-primary/60 animate-spin" />
                  <p className="font-semibold">{loading}</p>
                </div>
              ) : (
                <div>
                  <div className="w-full h-full flex items-center justify-between gap-5">
                    <div
                      {...getRootProps()}
                      className={cn(
                        "border-2 border-dashed p-6 cursor-pointer mx-auto my-2 rounded-md flex-1",
                        {
                          "bg-accent": isDragActive,
                        },
                      )}
                    >
                      <input {...getInputProps()} multiple={multiple} />
                      {isDragActive ? (
                        <p className="text-center">{draggingActive}</p>
                      ) : (
                        <p className="text-center">{draggingInactive}</p>
                      )}
                    </div>
                    {fieldValue?.length > 0 && (
                      <Button
                        type="button"
                        onClick={() => setIsListCollapsed((prev) => !prev)}
                      >
                        {isListCollapsed ? showList : hideList}
                      </Button>
                    )}
                  </div>
                  {multiple && fieldValue?.length > 0 && (
                    <p>
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
          />
        </motion.div>
      )}
    </div>
  );
}
