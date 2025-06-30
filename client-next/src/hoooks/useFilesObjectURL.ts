import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { FieldInputItem } from "@/components/forms/input-file";
import { useWorker } from "@koale/useworker";
import { v4 as uuidv4 } from "uuid";
import {
  FieldValues,
  Path,
  PathValue,
  UseFormGetValues,
  UseFormSetValue,
} from "react-hook-form";
import { toShuffleArray } from "@/lib/utils";

interface Args<T extends FieldValues> {
  files: string[];
  fieldName: keyof T & string;
  setValue: UseFormSetValue<T>;
  getValues: UseFormGetValues<T>;
  currentItems?: FieldInputItem[];
  trigger?: boolean;
}

const fetchFilesObjectURL = async (urls: string[]) => {
  async function fetchFileObjectURLWorker(url: string) {
    const res = await fetch(url, {
      priority: "high",
      method: "GET",
      headers: {
        "X-Bypass-Cache": "1",
        Accept: "image/png,image/jpeg,video/mp4",
      },
    });
    const mimeType =
      res.headers.get("content-type") || "application/octet-stream";

    const contentDisposition = res.headers.get("content-disposition");
    let filename = undefined;
    if (contentDisposition) {
      const match = contentDisposition.match(/filename="?([^"]+)"?/);
      if (match && match[1]?.trim()) {
        filename = match[1];
      }
    }

    const buffer = await res.arrayBuffer();

    return {
      buffer,
      mimeType,
      url,
      filename,
    };
  }

  return await Promise.all(urls.map((url) => fetchFileObjectURLWorker(url)));
};

const springClient = process.env.NEXT_PUBLIC_SPRING_CLIENT;
const springServer = process.env.NEXT_PUBLIC_SPRING;

export default function useFilesObjectURL<T extends FieldValues>({
  files,
  fieldName,
  setValue,
  getValues,
  currentItems = [],
  trigger = true,
}: Args<T>) {
  if (!springClient) {
    throw new Error("Missing environment variable NEXT_PUBLIC_SPRING_CLIENT");
  }

  if (!springServer) {
    throw new Error("Missing environment variable NEXT_PUBLIC_SPRING");
  }

  const firstRunRef = useRef(true);
  const [chunkProgressValue, setChunkProgressValue] = useState(0);
  const [fetchWorker] = useWorker(fetchFilesObjectURL);
  const [fetchWorker2] = useWorker(fetchFilesObjectURL);
  const itemSourcesRef = useRef<Set<string>>(new Set());

  if (!(fieldName in getValues())) {
    throw new Error(`Invalid field name: ${fieldName}`);
  }

  const updateProgressWrapper = useCallback(
    async (worker: typeof fetchWorker, urls: string[]) => {
      const r = await worker(urls);
      setChunkProgressValue((prev) => ((prev + r.length) / files.length) * 100);
      return r;
    },
    [files.length],
  );

  useEffect(() => {
    if (!trigger) return;
    if (files.length > 0 && firstRunRef.current) {
      setChunkProgressValue(0);
      const filesForFront = files.map((f, index) => {
        if (!f.startsWith(springServer)) {
          return {
            url: f,
            index,
          };
        }

        const url = new URL(f);
        const newUrl = new URL(
          springClient + url.pathname + url.search + url.hash,
        );
        return {
          url: newUrl.toString(),
          index,
        };
      });

      const [workerUrls, workerUrls2] = toShuffleArray(filesForFront).reduce(
        (acc, item, index) => {
          if (index % 2 === 0) {
            acc[0].push(item);
          } else {
            acc[1].push(item);
          }

          return acc;
        },
        [[], []] as { url: string; index: number }[][],
      );

      const worker1Resp = updateProgressWrapper(
        fetchWorker,
        workerUrls.map((i) => i.url),
      );
      const worker2Resp =
        workerUrls2.length > 0
          ? updateProgressWrapper(
              fetchWorker2,
              workerUrls2.map((i) => i.url),
            )
          : Promise.resolve([]);
      Promise.all([worker1Resp, worker2Resp])
        .then((r) =>
          r.flat().map((r) => ({
            ...r,
            index: filesForFront.find((i) => i.url === r.url)?.index,
          })),
        )
        .then((reps) =>
          reps.map((fs) => {
            const urlPart = fs.url.split("/").pop();
            const ext = fs.mimeType?.split("/").pop();
            const fallbackName = urlPart && ext ? `${urlPart}.${ext}` : "file";

            const file = new File([fs.buffer], fs.filename || fallbackName, {
              type: fs.mimeType,
            });
            const objectURL = URL.createObjectURL(file);
            setChunkProgressValue((prev) => ((prev + 1) / files.length) * 100);
            itemSourcesRef.current.add(objectURL);
            return {
              id: uuidv4(),
              src: objectURL,
              file,
              index: fs.index,
            };
          }),
        )
        .then((fs) => {
          const sortedFs = fs.sort((a, b) =>
            a.index != undefined && b.index != undefined
              ? a.index - b.index
              : 0,
          );

          setValue(fieldName as Path<T>, sortedFs as PathValue<T, Path<T>>);
          firstRunRef.current = false;
        });
    }
  }, [
    fieldName,
    files,
    setValue,
    getValues,
    fetchWorker,
    fetchWorker2,
    updateProgressWrapper,
    trigger,
  ]);

  useEffect(() => {
    if (currentItems && currentItems.length > 0) {
      currentItems.forEach(({ src }) => {
        if (!itemSourcesRef.current.has(src)) {
          itemSourcesRef.current.add(src);
        }
      });
    }
  }, [currentItems]);

  const fileCleanup = useCallback(() => {
    try {
      const currentFiles = getValues(fieldName as Path<T>) as FieldInputItem[];
      currentFiles?.forEach((item) => {
        itemSourcesRef.current.add(item.src);
      });
      itemSourcesRef.current.forEach((src) => {
        URL.revokeObjectURL(src);
      });
    } catch (error) {
      console.log("Error during file cleanup:", error);
    }
  }, [fieldName, getValues]);

  return {
    fileCleanup,
    chunkProgressValue,
  };
}
