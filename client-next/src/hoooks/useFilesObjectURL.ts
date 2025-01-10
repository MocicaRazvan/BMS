// import { useCallback, useEffect, useState } from "react";
// import { FieldInputItem } from "@/components/forms/input-file";
// import { v4 as uuidv4 } from "uuid";
// import {
//   FieldValues,
//   Path,
//   PathValue,
//   UseFormGetValues,
//   UseFormSetValue,
// } from "react-hook-form";
//
// interface Args<T extends FieldValues> {
//   files: string[];
//   fieldName: keyof T & string;
//   setValue: UseFormSetValue<T>;
//   getValues: UseFormGetValues<T>;
// }
//
// async function fetchFileObjectURL(
//   url: string,
//   chunkProgress?: (percent: number) => void,
// ): Promise<FieldInputItem> {
//   const res = await fetch(url, {
//     priority: "high",
//     method: "GET",
//   });
//   const fileName = url.split("/").pop() || "file";
//   const mimeType =
//     res.headers.get("content-type") || "application/octet-stream";
//
//   let blob: Blob;
//   if (res.body) {
//     const reader = res.body.getReader();
//     const chunks: Uint8Array[] = [];
//     let done = false;
//
//     const contentLength = parseInt(
//       res?.headers?.get("Content-Length") || "-1",
//       10,
//     );
//     let receivedLength = 0;
//     while (!done) {
//       const { value, done: doneReading } = await reader.read();
//       if (value) {
//         chunks.push(value);
//         if (contentLength > 0) {
//           receivedLength += value.length;
//           if (chunkProgress) {
//             chunkProgress(Math.floor((receivedLength / contentLength) * 100));
//           }
//         }
//       }
//       done = doneReading;
//     }
//
//     blob = new Blob(chunks, { type: mimeType });
//     console.log("Chunks", url);
//   } else {
//     blob = await res.blob();
//   }
//
//   const file = new File([blob], fileName, { type: mimeType });
//   const objectURL = URL.createObjectURL(blob);
//
//   return {
//     id: uuidv4(),
//     src: objectURL,
//     file,
//   };
// }
//
// const fetchFilesObjectURL = async (
//   urls: string[],
//   chunkProgress?: (percent: number) => void,
// ): Promise<FieldInputItem[]> => {
//   const progressMap = new Map<string, number>();
//   const totalFiles = urls.length;
//
//   const updateOverallProgress = () => {
//     if (chunkProgress) {
//       const totalProgress = Array.from(progressMap.values()).reduce(
//         (sum, progress) => sum + progress,
//         0,
//       );
//       chunkProgress(totalProgress / totalFiles);
//     }
//   };
//
//   return Promise.all(
//     urls.map((url) =>
//       fetchFileObjectURL(url, (percent) => {
//         progressMap.set(url, percent);
//         updateOverallProgress();
//       }),
//     ),
//   );
// };
//
// export default function useFilesObjectURL<T extends FieldValues>({
//   files,
//   fieldName,
//   setValue,
//   getValues,
// }: Args<T>) {
//   const [firstRun, setFirstRun] = useState(true);
//   const springClient = process.env.NEXT_PUBLIC_SPRING_CLIENT;
//   const springServer = process.env.NEXT_PUBLIC_SPRING;
//   const [chunkProgressValue, setChunkProgressValue] = useState(0);
//
//   if (!springClient) {
//     throw new Error("Missing environment variable NEXT_PUBLIC_SPRING_CLIENT");
//   }
//
//   if (!springServer) {
//     throw new Error("Missing environment variable NEXT_PUBLIC_SPRING");
//   }
//
//   if (!(fieldName in getValues())) {
//     throw new Error(`Invalid field name: ${fieldName}`);
//   }
//
//   useEffect(() => {
//     if (files.length > 0 && firstRun) {
//       const filesForFront = files.map((f) => {
//         if (!f.startsWith(springServer)) {
//           return f;
//         }
//
//         const url = new URL(f);
//         const newUrl = new URL(
//           springClient + url.pathname + url.search + url.hash,
//         );
//         return newUrl.toString();
//       });
//
//       fetchFilesObjectURL(filesForFront, setChunkProgressValue).then((fs) => {
//         setFirstRun(false);
//         setValue(fieldName as Path<T>, fs as PathValue<T, Path<T>>);
//       });
//     }
//   }, [
//     fieldName,
//     files,
//     firstRun,
//     springClient,
//     setValue,
//     springServer,
//     getValues,
//   ]);
//
//   const fileCleanup = useCallback(() => {
//     const currentFiles = getValues(fieldName as Path<T>) as FieldInputItem[];
//     currentFiles?.forEach((item) => {
//       URL.revokeObjectURL(item.src);
//     });
//   }, [fieldName, getValues]);
//   return {
//     fileCleanup,
//     chunkProgressValue,
//   };
// }
import { useCallback, useEffect, useState } from "react";
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

interface Args<T extends FieldValues> {
  files: string[];
  fieldName: keyof T & string;
  setValue: UseFormSetValue<T>;
  getValues: UseFormGetValues<T>;
}

const fetchFilesObjectURL = async (urls: string[]) => {
  async function fetchFileObjectURLWorker(url: string) {
    const res = await fetch(url, {
      priority: "high",
      method: "GET",
    });
    const mimeType =
      res.headers.get("content-type") || "application/octet-stream";

    let blob: Blob;
    if (res.body) {
      const reader = res.body.getReader();
      const chunks: Uint8Array[] = [];
      let done = false;

      while (!done) {
        const { value, done: doneReading } = await reader.read();
        if (value) {
          chunks.push(value);
        }
        done = doneReading;
      }

      blob = new Blob(chunks, { type: mimeType });
      console.log("Chunks", url);
    } else {
      blob = await res.blob();
    }

    return {
      blob,
      mimeType,
      url,
    };
  }

  return await Promise.all(urls.map((url) => fetchFileObjectURLWorker(url)));
};
export default function useFilesObjectURL<T extends FieldValues>({
  files,
  fieldName,
  setValue,
  getValues,
}: Args<T>) {
  const [firstRun, setFirstRun] = useState(true);
  const springClient = process.env.NEXT_PUBLIC_SPRING_CLIENT;
  const springServer = process.env.NEXT_PUBLIC_SPRING;
  const [chunkProgressValue, setChunkProgressValue] = useState(0);
  const [fetchWorker] = useWorker(fetchFilesObjectURL);

  if (!springClient) {
    throw new Error("Missing environment variable NEXT_PUBLIC_SPRING_CLIENT");
  }

  if (!springServer) {
    throw new Error("Missing environment variable NEXT_PUBLIC_SPRING");
  }

  if (!(fieldName in getValues())) {
    throw new Error(`Invalid field name: ${fieldName}`);
  }

  useEffect(() => {
    if (files.length > 0 && firstRun) {
      const filesForFront = files.map((f) => {
        if (!f.startsWith(springServer)) {
          return f;
        }

        const url = new URL(f);
        const newUrl = new URL(
          springClient + url.pathname + url.search + url.hash,
        );
        return newUrl.toString();
      });

      fetchWorker(filesForFront)
        .then(async (reps) =>
          (await reps).map((fs) => {
            console.log("fs", fs);
            const file = new File(
              [fs.blob],
              fs.url.split("/").pop() || "file",
              {
                type: fs.mimeType,
              },
            );
            const objectURL = URL.createObjectURL(fs.blob);
            setChunkProgressValue((prev) => ((prev + 1) / files.length) * 100);
            return {
              id: uuidv4(),
              src: objectURL,
              file,
            };
          }),
        )
        .then((fs) => {
          setFirstRun(false);
          setValue(fieldName as Path<T>, fs as PathValue<T, Path<T>>);
        });
    }
  }, [
    fieldName,
    files,
    firstRun,
    springClient,
    setValue,
    springServer,
    getValues,
    fetchWorker,
  ]);

  const fileCleanup = useCallback(() => {
    const currentFiles = getValues(fieldName as Path<T>) as FieldInputItem[];
    currentFiles?.forEach((item) => {
      URL.revokeObjectURL(item.src);
    });
  }, [fieldName, getValues]);
  return {
    fileCleanup,
    chunkProgressValue: undefined, // cant return an array of promises from worker
  };
}
