"use client";

import { MouseEventHandler, useState } from "react";
import { Button } from "@/components/ui/button";
import { CircleAlert, Trash2 } from "lucide-react";
import { useDebounce } from "react-use";

interface Props {
  onClick?: MouseEventHandler<HTMLButtonElement>;
}

export default function TwoStepDeleteButton({ onClick }: Props) {
  const [isDeletePressed, setIsDeletePressed] = useState(false);
  useDebounce(
    () => {
      if (isDeletePressed) {
        setIsDeletePressed(false);
      }
    },
    3000,
    [isDeletePressed],
  );

  return !isDeletePressed ? (
    <Button
      size="icon"
      variant="destructive"
      className="absolute top-1 right-2"
      onClick={(e) => {
        e.stopPropagation();
        setIsDeletePressed(true);
      }}
      type="button"
    >
      <Trash2 />
    </Button>
  ) : (
    <Button
      size="icon"
      variant="amber"
      className="absolute top-1 right-2"
      onClick={(e) => {
        e.stopPropagation();
        onClick?.(e);
      }}
      type="button"
    >
      <CircleAlert />
    </Button>
  );
}
