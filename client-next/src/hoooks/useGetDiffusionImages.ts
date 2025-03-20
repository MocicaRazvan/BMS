"use client";
import { useCallback } from "react";
import JSZip from "jszip";
import { FieldInputItem } from "@/components/forms/input-file";
import { v4 as uuidv4 } from "uuid";
import {
  Path,
  PathValue,
  UseFormGetValues,
  UseFormReturn,
} from "react-hook-form";
import { ImageType } from "@/types/forms";
import { getCsrfToken } from "next-auth/react";
import { NEXT_CSRF_HEADER } from "@/lib/constants";
import fetchFactory from "@/lib/fetchers/fetchWithRetry";

export type DiffusionCallback = (images: FieldInputItem[]) => void;

interface GetImagesArgs {
  prompt: string;
  negative_prompt: string;
  num_images: number;
  successCallback?: DiffusionCallback;
}

export interface CleanUpArgs<T extends ImageType = ImageType> {
  getValues: UseFormGetValues<T>;
  fieldName: string;
}

export default function useGetDiffusionImages<T extends ImageType = ImageType>({
  cleanUpArgs: { fieldName, getValues },
}: {
  cleanUpArgs: CleanUpArgs<T>;
}) {
  const getImages = useCallback(
    async ({
      prompt,
      negative_prompt,
      num_images,
      successCallback,
    }: GetImagesArgs) => {
      if (num_images < 1) {
        num_images = 1;
      } else if (num_images > 3) {
        num_images = 3;
      }
      const response = await fetchFactory(fetch)("/api/diffusion", {
        method: "POST",
        body: JSON.stringify({
          num_images,
          prompt,
          negative_prompt,
          width: 512,
          height: 512,
        }),
        headers: {
          [NEXT_CSRF_HEADER]: (await getCsrfToken()) ?? "",
        },
      });
      if (!response.ok) {
        return {
          success: false,
          urls: [],
        };
      }

      const zipBuffer = await response.arrayBuffer();
      const zip = await JSZip.loadAsync(zipBuffer);

      const urls = await Promise.all(
        Object.values(zip.files)
          .filter((file) => file.name.endsWith(".png"))
          .map(async (file) => {
            const fileData = await file.async("blob");
            const objectUrl = URL.createObjectURL(fileData);
            const jsFile = new File([fileData], file.name, {
              type: "image/png",
            });
            console.log("jsFile", jsFile);
            console.log("jsFile objectUrl", objectUrl);
            return { id: uuidv4(), file: jsFile, src: objectUrl };
          }),
      );

      if (successCallback) {
        successCallback(urls);
      }

      return {
        success: true,
        urls,
      };
    },
    [],
  );

  const invalidateImages = useCallback(() => {
    const currentFiles = getValues(fieldName as Path<T>) as FieldInputItem[];
    currentFiles?.forEach((item) => {
      URL.revokeObjectURL(item.src);
    });
  }, [fieldName, getValues]);

  const addImagesCallback = useCallback(
    (images: FieldInputItem[], form: UseFormReturn<T>) => {
      form.setValue(
        "images" as Path<T>,
        [...form.getValues("images" as Path<T>), ...images] as PathValue<
          T,
          Path<T>
        >,
      );
    },
    [],
  );

  return { addImagesCallback, getImages, invalidateImages };
}
