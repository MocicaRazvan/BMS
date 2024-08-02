// "use server";
// import { FieldInputItem } from "@/components/forms/input-file";
// import { v4 as uuidv4 } from "uuid";
//
// export async function fetchFileBase64(url: string): Promise<FieldInputItem> {
//   const res = await fetch(url);
//   const arrayBuffer = await res.arrayBuffer();
//   const buffer = Buffer.from(arrayBuffer);
//   const base64 = buffer.toString("base64");
//   const mimeType =
//     res.headers.get("content-type") || "application/octet-stream";
//   const fileName = url.split("/").pop() || "file";
//   const file = new File([buffer], fileName, { type: mimeType });
//
//   return {
//     id: uuidv4(),
//     src: `data:${mimeType};base64,${base64}`,
//     file,
//   };
// }
//
// export const fetchFilesBase64 = async (
//   urls: string[],
// ): Promise<FieldInputItem[]> => Promise.all(urls.map(fetchFileBase64));
