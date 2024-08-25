import { ComponentType, Dispatch, SetStateAction } from "react";
import { Button } from "./button";
import {
  DoubleArrowLeftIcon,
  DoubleArrowRightIcon,
} from "@radix-ui/react-icons";
import { ChevronLeftIcon, ChevronRightIcon } from "lucide-react";

export interface CustomDisplayProps {
  currentIndex: number;
  items: any[];
  setCurrentIndex: Dispatch<SetStateAction<number>>;
}
interface Props {
  setCurrentIndex: Dispatch<SetStateAction<number>>;
  currentIndex: number;
  items: any[];
  CustomDisplay?: ComponentType<CustomDisplayProps>;
}

export default function CustomPaginationButtons({
  setCurrentIndex,
  currentIndex,
  items,
  CustomDisplay,
}: Props) {
  return (
    <div className="flex items-center justify-end gap-1">
      <Button
        variant="outline"
        onClick={() => setCurrentIndex(0)}
        disabled={currentIndex === 0}
      >
        <DoubleArrowLeftIcon className="w-4 h-4" />
      </Button>
      <Button
        variant="outline"
        disabled={currentIndex === 0}
        onClick={() => setCurrentIndex((prev) => --prev)}
      >
        <ChevronLeftIcon className="w-5 h-5" />
      </Button>
      {CustomDisplay ? (
        <CustomDisplay
          currentIndex={currentIndex}
          items={items}
          setCurrentIndex={setCurrentIndex}
        />
      ) : (
        <p className="border p-2 rounded-lg mx-3">{`${currentIndex + 1} / ${
          items.length
        }`}</p>
      )}
      <Button
        variant="outline"
        disabled={currentIndex === items.length - 1}
        onClick={() => setCurrentIndex((prev) => ++prev)}
      >
        <ChevronRightIcon className="w-5 h-5" />
      </Button>
      <Button
        variant="outline"
        disabled={currentIndex === items.length - 1}
        onClick={() => setCurrentIndex(items.length - 1)}
      >
        <DoubleArrowRightIcon className="w-4 h-4" />
      </Button>
    </div>
  );
}
