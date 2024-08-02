import { useEffect, useState } from "react";
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

async function fetchFileBase64(url: string): Promise<FieldInputItem> {
  const res = await fetch(url);
  const blob = await res.blob();
  const fileName = url.split("/").pop() || "file";
  const mimeType =
    res.headers.get("content-type") || "application/octet-stream";
  const file = new File([blob], fileName, { type: mimeType });

  const base64 = await new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onloadend = () => resolve(reader.result as string);
    reader.onerror = reject;
    reader.readAsDataURL(blob);
  });

  return {
    id: uuidv4(),
    src: base64,
    file,
  };
}

const fetchFilesBase64 = async (urls: string[]): Promise<FieldInputItem[]> =>
  Promise.all(urls.map(fetchFileBase64));

export default function useFilesBase64<T extends FieldValues>({
  files,
  fieldName,
  setValue,
  getValues,
}: Args<T>) {
  const [firstRun, setFirstRun] = useState(true);
  if (!(fieldName in getValues())) {
    throw new Error(`Invalid field name: ${fieldName}`);
  }
  useEffect(() => {
    if (files.length > 0 && firstRun) {
      fetchFilesBase64(files).then((fs) => {
        setFirstRun(false);
        setValue(fieldName as Path<T>, fs as PathValue<T, Path<T>>);
      });
    }
  }, [fieldName, files, firstRun, setValue]);
}
