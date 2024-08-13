"use client";
import { Button } from "@/components/ui/button";

interface Props {
  items: any;
}

export default function GarBtn({ items }: Props) {
  return (
    <Button
      onClick={() => {
        console.log(items);
      }}
    >
      Gar
    </Button>
  );
}
