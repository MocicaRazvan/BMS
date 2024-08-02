"use client";
import CustomMediaCarousel from "@/components/common/custom-media-carousel";

interface Props {
  images: string[];
}

export default function CustomImageCarousel({ images }: Props) {
  // const [isFullScreen, setIsFullScreen] = useState(false);
  // const [currentImage, setCurrentImage] = useState<string | null>(null);

  return (
    <CustomMediaCarousel
      media={images.map((src) => ({ type: "image", src }))}
    />
  );
  // return (
  //   <div className="w-full flex justify-center items-center overflow-hidden">
  //     <Carousel className="w-full max-w-4xl">
  //       <CarouselContent>
  //         {images.map((img, i) => (
  //           <CarouselItem key={img + i}>
  //             <div
  //               className="rounded-lg overflow-hidden h-[450px] max-h-[450px] w-full  cursor-grab relative"
  //               onClick={() => {
  //                 setIsFullScreen(true);
  //                 setCurrentImage(img);
  //               }}
  //             >
  //               <Image
  //                 src={img}
  //                 alt="post image"
  //                 fill={true}
  //                 className="w-full max-w-[1000px]  object-cover h-full"
  //                 priority={i === 0}
  //               />
  //             </div>
  //           </CarouselItem>
  //         ))}
  //       </CarouselContent>
  //       <CarouselPrevious />
  //       <CarouselNext />
  //     </Carousel>
  //     {currentImage && (
  //       <Dialog open={isFullScreen} onOpenChange={setIsFullScreen}>
  //         <DialogContent
  //           className="h-[80vh] min-w-[90%] p-0 overflow-hidden"
  //           closeButtonClassName={
  //             "bg-background w-6 h-6 flex items-center justify-center font-bold"
  //           }
  //         >
  //           <Image
  //             src={currentImage}
  //             alt="current image"
  //             fill={true}
  //             className="w-full object-cover h-full "
  //           />
  //         </DialogContent>
  //       </Dialog>
  //     )}
  //   </div>
  // );
}
