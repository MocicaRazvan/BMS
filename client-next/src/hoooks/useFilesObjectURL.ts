// import { useEffect, useState } from "react";
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
// async function fetchFileBase64(url: string): Promise<FieldInputItem> {
//   const res = await fetch(url);
//   const blob = await res.blob();
//   const fileName = url.split("/").pop() || "file";
//   const mimeType =
//     res.headers.get("content-type") || "application/octet-stream";
//   const file = new File([blob], fileName, { type: mimeType });
//
//   const base64 = await new Promise<string>((resolve, reject) => {
//     const reader = new FileReader();
//     reader.onloadend = () => resolve(reader.result as string);
//     reader.onerror = reject;
//     reader.readAsDataURL(blob);
//   });
//
//   return {
//     id: uuidv4(),
//     src: base64,
//     file,
//   };
// }
//
// const fetchFilesBase64 = async (urls: string[]): Promise<FieldInputItem[]> =>
//   Promise.all(urls.map(fetchFileBase64));
//
// export default function useFilesBase64<T extends FieldValues>({
//   files,
//   fieldName,
//   setValue,
//   getValues,
// }: Args<T>) {
//   const [firstRun, setFirstRun] = useState(true);
//   const springClient = process.env.NEXT_PUBLIC_SPRING_CLIENT;
//   const springServer = process.env.NEXT_PUBLIC_SPRING;
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
//   useEffect(() => {
//     if (files.length > 0 && firstRun) {
//       const filesForFront = files.map((f) => {
//         // const baseUrl = new URL(f).origin;
//         // if (baseUrl !== springClient) {
//         //   return f.replace(baseUrl, springClient);
//         // }
//         // return f;
//
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
//       fetchFilesBase64(filesForFront).then((fs) => {
//         setFirstRun(false);
//         setValue(fieldName as Path<T>, fs as PathValue<T, Path<T>>);
//       });
//     }
//   }, [fieldName, files, firstRun, springClient, setValue, springServer]);
// }
import { useCallback, useEffect, useState } from "react";
import { FieldInputItem } from "@/components/forms/input-file";
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

async function fetchFileObjectURL(url: string): Promise<FieldInputItem> {
  const res = await fetch(url);
  const blob = await res.blob();
  const fileName = url.split("/").pop() || "file";
  const mimeType =
    res.headers.get("content-type") || "application/octet-stream";
  const file = new File([blob], fileName, { type: mimeType });
  const objectURL = URL.createObjectURL(blob);

  return {
    id: uuidv4(),
    src: objectURL,
    file,
  };
}

const fetchFilesObjectURL = async (urls: string[]): Promise<FieldInputItem[]> =>
  Promise.all(urls.map(fetchFileObjectURL));

export default function useFilesObjectURL<T extends FieldValues>({
  files,
  fieldName,
  setValue,
  getValues,
}: Args<T>) {
  const [firstRun, setFirstRun] = useState(true);
  const springClient = process.env.NEXT_PUBLIC_SPRING_CLIENT;
  const springServer = process.env.NEXT_PUBLIC_SPRING;

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

      fetchFilesObjectURL(filesForFront).then((fs) => {
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
  ]);

  const fileCleanup = useCallback(() => {
    const currentFiles = getValues(fieldName as Path<T>) as FieldInputItem[];
    currentFiles?.forEach((item) => {
      URL.revokeObjectURL(item.src);
    });
  }, [fieldName, getValues]);
  return {
    fileCleanup,
  };
}
