"use client";

import { useEffect, useState } from "react";
import JSZip from "jszip";
import Image from "next/image";
import { Button } from "@/components/ui/button";

interface DiffusionImage {
  name: string;
  url: string;
}

export default function TestPageContent() {
  const [imageUrls, setImageUrls] = useState<DiffusionImage[]>([]);

  const fetchImages = async () => {
    try {
      const response = await fetch("/api/diffusion", {
        method: "POST",
        body: JSON.stringify({
          num_images: 2,
          prompt: "chicken salad with tomatoes, realistic",
          negative_prompt: "low quality, blurry, animated",
        }),
      });

      if (!response.ok) {
        throw new Error("Failed to fetch ZIP file");
      }

      const zipBuffer = await response.arrayBuffer();

      const zip = await JSZip.loadAsync(zipBuffer);

      // const urls: DiffusionImage[] = [];
      //
      // // Iterate through each file in the ZIP
      // for (const fileName of Object.keys(zip.files)) {
      //   const file = zip.files[fileName];
      //
      //   if (fileName.endsWith(".png")) {
      //     // Convert file content to a Blob
      //     const fileData = await file.async("blob");
      //
      //     // Create an object URL
      //     const objectUrl = URL.createObjectURL(fileData);
      //
      //     urls.push({ name: fileName, url: objectUrl });
      //   }
      // }

      const urls = await Promise.all(
        Object.values(zip.files)
          .filter((file) => file.name.endsWith(".png"))
          .map(async (file) => {
            const fileData = await file.async("blob");
            const objectUrl = URL.createObjectURL(fileData);
            return { name: file.name, url: objectUrl };
          }),
      );

      setImageUrls((p) => {
        p.forEach((i) => URL.revokeObjectURL(i.url));
        return urls;
      });
    } catch (error) {
      throw new Error("Failed to fetch ZIP file");
      console.error("Error fetching or processing ZIP file:", error);
    }
  };

  useEffect(() => {
    return () => {
      imageUrls.forEach((i) => URL.revokeObjectURL(i.url));
    };
  }, [JSON.stringify(imageUrls)]);

  return (
    <div className="flex flex-col items-center justify-center p-10 gap-4">
      <h1>Diffusion</h1>
      <Button onClick={fetchImages}>Fetch Images</Button>
      <div className="flex flex-col items-center justify-center gap-5">
        {imageUrls.map(({ name, url }) => (
          <div key={name} className="text-center">
            <Image src={url} alt={name} width={512} height={512} />
            <p>{name}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
