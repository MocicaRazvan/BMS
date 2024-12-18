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
