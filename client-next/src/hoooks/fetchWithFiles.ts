import { BaseError } from "@/types/responses";
import { getCsrfNextAuthHeader } from "@/actions/get-csr-next-auth";

interface BaseArgs {
  token: string;
  path: string;
  method?: "POST" | "PUT";
  clientId: string;
}

export interface BaseBodyMultipleFiles<T extends object> {
  filesObj: Record<string, File[]>;
  body: T;
}

interface FetchWithFilesArgsMultipleFiles<T extends object> extends BaseArgs {
  data: BaseBodyMultipleFiles<T>;
}
export async function fetchWithFilesMultipleFiles<
  T extends object,
  E extends BaseError,
  R,
>({
  path,
  data: { body, filesObj },
  token,
  method = "POST",
  clientId,
}: FetchWithFilesArgsMultipleFiles<T>) {
  const formData = new FormData();
  formData.append("body", JSON.stringify(body));
  const crfHeader = await getCsrfNextAuthHeader();

  Object.entries(filesObj).forEach(([key, files]) => {
    if (files.length > 0) {
      files.forEach((f) => formData.append(key, f));
    }
  });

  const res = await fetch(
    `${process.env.NEXT_PUBLIC_SPRING_CLIENT}${path}?clientId=${clientId}`,
    {
      method,
      body: formData,
      credentials: "include",
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: "application/json",
        ...crfHeader,
      },
    },
  );
  if (res.ok) {
    console.log("res.ok");
    return (await res.json()) as R;
  } else {
    console.log("res not ok");
    throw (await res.json()) as E;
  }
}

export interface BaseBody<T extends object> {
  files: File[];
  body: T;
}
interface FetchWithFilesArgs<T extends object> extends BaseArgs {
  data: BaseBody<T>;
}
export async function fetchWithFiles<T extends object, E extends BaseError, R>({
  data: { body, files },
  ...rest
}: FetchWithFilesArgs<T>) {
  return fetchWithFilesMultipleFiles<T, E, R>({
    data: { body, filesObj: { files } },
    ...rest,
  });
}
