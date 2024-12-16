"use client";

import { useState } from "react";
import Image from "next/image";
import { FieldInputItem } from "@/components/forms/input-file";
import { DiffusionSchemaTexts } from "@/types/forms";

interface DiffusionImage {
  name: string;
  url: string;
}
interface Props {
  texts: DiffusionSchemaTexts;
}
export default function TestPageContent({ texts }: Props) {
  const [imageUrls, setImageUrls] = useState<FieldInputItem[]>([]);
  return (
    <div className="w-full h-full flex items-center justify-center p-20">
      {/*<DiffusionImagesForm texts={texts} callback={setImageUrls} />*/}
      <div className="flex flex-col items-center justify-center gap-5">
        {imageUrls.map(({ id, src, file }) => (
          <div key={id} className="text-center">
            <Image src={src} alt={file.name} width={512} height={512} />
            <p>{file.name}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
// export default function TestPageContent() {
//   const [imageUrls, setImageUrls] = useState<FieldInputItem[]>([]);
//
//   const { invalidateImages, getImages } = useGetDiffusionImages();
//
//   const handleClick = async () => {
//     const urls = await getImages({
//       prompt: "chicken salad with tomatoes, realistic",
//       negative_prompt: "low quality, blurry, animated",
//       num_images: 2,
//       successCallback: setImageUrls,
//     });
//   };
//
//   // const fetchImages = async () => {
//   //   try {
//   //     const response = await fetch("/api/diffusion", {
//   //       method: "POST",
//   //       body: JSON.stringify({
//   //         num_images: 2,
//   //         prompt: "chicken salad with tomatoes, realistic",
//   //         negative_prompt: "low quality, blurry, animated",
//   //         width: 512,
//   //         height: 512,
//   //       }),
//   //     });
//   //
//   //     if (!response.ok) {
//   //       throw new Error("Failed to fetch ZIP file");
//   //     }
//   //
//   //     const zipBuffer = await response.arrayBuffer();
//   //
//   //     const zip = await JSZip.loadAsync(zipBuffer);
//   //
//   //     // const urls: DiffusionImage[] = [];
//   //     //
//   //     // // Iterate through each file in the ZIP
//   //     // for (const fileName of Object.keys(zip.files)) {
//   //     //   const file = zip.files[fileName];
//   //     //
//   //     //   if (fileName.endsWith(".png")) {
//   //     //     // Convert file content to a Blob
//   //     //     const fileData = await file.async("blob");
//   //     //
//   //     //     // Create an object URL
//   //     //     const objectUrl = URL.createObjectURL(fileData);
//   //     //
//   //     //     urls.push({ name: fileName, url: objectUrl });
//   //     //   }
//   //     // }
//   //
//   //     const urls = await Promise.all(
//   //       Object.values(zip.files)
//   //         .filter((file) => file.name.endsWith(".png"))
//   //         .map(async (file) => {
//   //           const fileData = await file.async("blob");
//   //           const objectUrl = URL.createObjectURL(fileData);
//   //           return { name: file.name, url: objectUrl };
//   //         }),
//   //     );
//   //
//   //     setImageUrls((p) => {
//   //       p.forEach((i) => URL.revokeObjectURL(i.url));
//   //       return urls;
//   //     });
//   //   } catch (error) {
//   //     throw new Error("Failed to fetch ZIP file");
//   //     console.error("Error fetching or processing ZIP file:", error);
//   //   }
//   // };
//   //
//   // useEffect(() => {
//   //   return () => {
//   //     imageUrls.forEach((i) => URL.revokeObjectURL(i.url));
//   //   };
//   // }, [JSON.stringify(imageUrls)]);
//
//   return (
//     <div className="flex flex-col items-center justify-center p-10 gap-4">
//       <h1>Diffusion</h1>
//       <Button onClick={handleClick}>Fetch Images</Button>
//       <div className="flex flex-col items-center justify-center gap-5">
//         {imageUrls.map(({ id, src, file }) => (
//           <div key={id} className="text-center">
//             <Image src={src} alt={file.name} width={512} height={512} />
//             <p>{file.name}</p>
//           </div>
//         ))}
//       </div>
//     </div>
//   );
// }
