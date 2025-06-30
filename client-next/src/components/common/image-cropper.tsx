"use client";
import { memo, useCallback, useEffect, useState } from "react";
import Cropper, { Area, Point } from "react-easy-crop";
import {
  Dialog,
  DialogContentNoResize,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Crop, RotateCcw, RotateCw, ZoomIn, ZoomOut } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Slider } from "@/components/ui/slider";
import { useDebounceWithCallBack } from "@/hoooks/useDebounceWithCallback";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import Loader from "@/components/ui/spinner";
import { usePrevious } from "react-use";

export interface ImageCropTexts {
  tooltipText: string;
  buttonText: string;
}
export interface ImageCropperProps {
  src: string;
  onCropComplete: (src: string, blob: Blob) => void;
  dialogOpenObserver?: (isOpen: boolean) => void;
  cropShape?: "rect" | "round";
  originalMime: string;
  texts: ImageCropTexts;
}
const PNG_MIME = "image/png" as const;
const ImageCropper = memo(
  ({
    src,
    onCropComplete,
    dialogOpenObserver,
    cropShape = "rect",
    originalMime,
    texts: { tooltipText, buttonText },
  }: ImageCropperProps) => {
    const [crop, setCrop] = useState<Point>({ x: 0, y: 0 });
    const [zoom, setZoom] = useState<number>(1);
    const [rotation, setRotation] = useState<number>(0);
    const [dialogOpen, setDialogOpen] = useState(false);
    const previousDialogOpen = usePrevious(dialogOpen);
    const [aspect, setAspect] = useState<number | undefined>(undefined);
    const [croppedAreaPixels, setCroppedAreaPixels] = useState<Area>({
      x: 0,
      y: 0,
      width: 256,
      height: 256,
    });
    const [isCropping, setIsCropping] = useState(false);

    const resetStates = useCallback(() => {
      setCrop({ x: 0, y: 0 });
      setZoom(1);
      setRotation(0);
    }, []);
    const debounceSrc = useDebounceWithCallBack(src, 200, resetStates);

    const handleDialogOpen = useCallback(
      (v: boolean) => {
        setDialogOpen(v);
        dialogOpenObserver?.(v);
      },
      [dialogOpenObserver],
    );

    useEffect(() => {
      if (cropShape === "rect") {
        const img = new Image();
        img.src = debounceSrc;
        img.onload = () => {
          const { width, height } = img;
          setAspect(width / height);
        };
      } else {
        setAspect(1);
      }
    }, [cropShape, debounceSrc]);

    const handleCrop = useCallback(async () => {
      setIsCropping(true);
      try {
        const croppedImage = await getCroppedImg(
          src,
          croppedAreaPixels,
          rotation,
          originalMime,
        );
        if (!croppedImage) {
          throw new Error("Failed to crop image");
        }
        onCropComplete(croppedImage.url, croppedImage.blob);
      } catch (e) {
        console.error(e);
      }
      setIsCropping(false);
      handleDialogOpen(false);
    }, [
      croppedAreaPixels,
      handleDialogOpen,
      onCropComplete,
      originalMime,
      rotation,
      src,
    ]);

    const isNoTooltip = !dialogOpen && previousDialogOpen === true;

    return (
      <Dialog open={dialogOpen} onOpenChange={handleDialogOpen}>
        {isNoTooltip ? (
          <DialogTrigger asChild={true}>
            <Button
              variant="outline"
              size="icon"
              type="button"
              onClick={() => handleDialogOpen(true)}
            >
              <Crop className="h-6 w-6" />
            </Button>
          </DialogTrigger>
        ) : (
          <TooltipProvider>
            <Tooltip>
              <TooltipTrigger asChild={true}>
                <DialogTrigger asChild={true}>
                  <Button
                    variant="outline"
                    size="icon"
                    type="button"
                    onClick={() => handleDialogOpen(true)}
                  >
                    <Crop className="h-6 w-6" />
                  </Button>
                </DialogTrigger>
              </TooltipTrigger>
              <TooltipContent
                className="bg-accent text-accent-foreground border"
                side="left"
              >
                {tooltipText}
              </TooltipContent>
            </Tooltip>
          </TooltipProvider>
        )}
        <DialogContentNoResize
          className="w-full max-w-2xl"
          closeButtonClassName="border bg-primary text-background p-0.5"
        >
          <DialogTitle className="sr-only">{"Image cropper"}</DialogTitle>
          <div className="relative w-full p-0.5 h-full min-h-[420px]">
            <Cropper
              showGrid={false}
              crop={crop}
              onCropChange={setCrop}
              image={debounceSrc}
              zoom={zoom}
              onZoomChange={setZoom}
              rotation={rotation}
              onRotationChange={setRotation}
              aspect={aspect}
              cropShape={cropShape}
              onCropComplete={(croppedArea, croppedAreaPixels) => {
                setCroppedAreaPixels(croppedAreaPixels);
              }}
            />
          </div>
          <div className="mt-4 flex flex-col gap-4">
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                onClick={() => setZoom(Math.max(1, zoom - 0.1))}
                type="button"
              >
                <ZoomOut size={16} />
              </Button>
              <Slider
                defaultValue={[1]}
                min={1}
                max={3}
                step={0.1}
                value={[zoom]}
                onValueChange={(val) => setZoom(val[0])}
              />
              <Button
                variant="outline"
                onClick={() => setZoom(Math.min(3, zoom + 0.1))}
                type="button"
              >
                <ZoomIn size={16} />
              </Button>
            </div>

            <div className="flex justify-between">
              <Button
                variant="outline"
                onClick={() => setRotation(rotation - 90)}
                type="button"
              >
                <RotateCcw size={16} />
              </Button>
              <Button
                variant="outline"
                onClick={() => setRotation(rotation + 90)}
                type="button"
              >
                <RotateCw size={16} />
              </Button>
            </div>
            <Button
              disabled={isCropping}
              onClick={handleCrop}
              className="w-full mt-1.5 font-semibold text-[16px]"
              type="button"
              size="sm"
            >
              {isCropping ? (
                <Loader className="text-background size-7" />
              ) : (
                buttonText
              )}
            </Button>
          </div>
        </DialogContentNoResize>
      </Dialog>
    );
  },
);
ImageCropper.displayName = "ImageCropper";
export default ImageCropper;

export const createImage = (url: string): Promise<HTMLImageElement> =>
  new Promise((resolve, reject) => {
    const image = new Image();
    image.addEventListener("load", () => resolve(image));
    image.addEventListener("error", (error) => reject(error));
    image.src = url;
  });

export function getRadianAngle(degreeValue: number) {
  return (degreeValue * Math.PI) / 180;
}

export function rotateSize(width: number, height: number, rotation: number) {
  const rotRad = getRadianAngle(rotation);

  return {
    width:
      Math.abs(Math.cos(rotRad) * width) + Math.abs(Math.sin(rotRad) * height),
    height:
      Math.abs(Math.sin(rotRad) * width) + Math.abs(Math.cos(rotRad) * height),
  };
}

export async function getCroppedImg(
  imageSrc: string,
  pixelCrop: Area,
  rotation = 0,
  mime: string,
  flip = { horizontal: false, vertical: false },
): Promise<{
  url: string;
  blob: Blob;
} | null> {
  const image = await createImage(imageSrc);
  const canvas = document.createElement("canvas");
  const ctx = canvas.getContext("2d");

  if (!ctx) {
    return null;
  }

  const rotRad = getRadianAngle(rotation);

  // calculate bounding box of the rotated image
  const { width: bBoxWidth, height: bBoxHeight } = rotateSize(
    image.width,
    image.height,
    rotation,
  );

  // set canvas size to match the bounding box
  canvas.width = bBoxWidth;
  canvas.height = bBoxHeight;

  // translate canvas context to a central location to allow rotating and flipping around the center
  ctx.translate(bBoxWidth / 2, bBoxHeight / 2);
  ctx.rotate(rotRad);
  ctx.scale(flip.horizontal ? -1 : 1, flip.vertical ? -1 : 1);
  ctx.translate(-image.width / 2, -image.height / 2);

  // draw rotated image
  ctx.drawImage(image, 0, 0);

  const croppedCanvas = document.createElement("canvas");

  const croppedCtx = croppedCanvas.getContext("2d");

  if (!croppedCtx) {
    return null;
  }

  // Set the size of the cropped canvas
  croppedCanvas.width = pixelCrop.width;
  croppedCanvas.height = pixelCrop.height;

  // Draw the cropped image onto the new canvas
  croppedCtx.drawImage(
    canvas,
    pixelCrop.x,
    pixelCrop.y,
    pixelCrop.width,
    pixelCrop.height,
    0,
    0,
    pixelCrop.width,
    pixelCrop.height,
  );

  // As Base64 string
  // return croppedCanvas.toDataURL('image/jpeg');

  // As a blob
  return new Promise((resolve, reject) => {
    croppedCanvas.toBlob((file) => {
      if (file == null) {
        reject(new Error("Failed to crop image"));
      } else {
        resolve({
          url: URL.createObjectURL(file),
          blob: file,
        });
      }
    }, PNG_MIME);
  });
}
