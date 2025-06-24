"use client";
import { cn, truncate } from "@/lib/utils";
import noImg from "@/assets/noImage.jpg";
import { ComponentType, useMemo } from "react";
import { ResponseWithUserDtoEntity, TitleBodyImagesUserDto } from "@/types/dto";
import { format, parseISO } from "date-fns";
import { Avatar, AvatarImage } from "@/components/ui/avatar";
import { Link, Locale } from "@/navigation/navigation";
import CustomImage from "@/components/common/custom-image";
import { OverflowLengthTextTooltip } from "@/components/common/overflow-text-tooltip";

export interface ItemCardTexts {
  author: string;
}

export interface ExtraProps<T> {
  item: ResponseWithUserDtoEntity<T>;
  locale: Locale;
}
interface Props<T extends TitleBodyImagesUserDto> {
  item: ResponseWithUserDtoEntity<T>;
  ExtraContent?: ComponentType<ExtraProps<T>>;
  ExtraHeader?: ComponentType<ExtraProps<T>>;
  ImageOverlay?: ComponentType<ExtraProps<T>>;
  texts: ItemCardTexts;
  eagerImage?: boolean;
  maxTitleLength?: number;
  itemHref?: string;
  onClick?: () => void;
  locale: Locale;
}
export default function ItemCard<T extends TitleBodyImagesUserDto>({
  item,
  ExtraContent,
  ExtraHeader,
  ImageOverlay,
  eagerImage = true,
  texts: { author },
  maxTitleLength = 75,
  itemHref,
  onClick,
  locale,
}: Props<T>) {
  const body = useMemo(
    () =>
      new DOMParser().parseFromString(item.model.content.body, "text/html")
        .documentElement.textContent,
    [item.model.content.body],
  );
  const ImageComp = (
    <CustomImage
      thumblinator
      alt="Header"
      className={cn(
        "rounded-lg object-cover w-full",
        itemHref && "cursor-pointer",
      )}
      height="250"
      // loader={imageLoader}
      src={item.model.content.images?.[0] || noImg}
      style={{
        aspectRatio: "400/250",
        objectFit: "cover",
      }}
      width="400"
      loading={eagerImage ? "eager" : undefined}
    />
  );
  return (
    <div
      className={cn(
        "flex flex-col items-start gap-2 border rounded-xl p-4 w-full  hover:shadow-lg transition-all duration-300 shadow-foreground hover:shadow-foreground/40 hover:scale-[1.025]",
      )}
    >
      <div
        className="relative w-full h-[250px] bg-background"
        onClick={onClick}
      >
        {itemHref ? (
          <Link href={itemHref} passHref>
            {ImageComp}
          </Link>
        ) : (
          ImageComp
        )}
        {ImageOverlay && <ImageOverlay item={item} locale={locale} />}
      </div>
      <div className="flex flex-col gap-3 mt-1 w-full">
        <div className="flex flex-col gap-1 w-full h-[285px] overflow-hidden py-2.5">
          <div className="flex items-center w-full justify-between">
            <OverflowLengthTextTooltip
              maxLength={maxTitleLength}
              text={item.model.content.title}
            >
              <h2 className="text-lg font-semibold tracking-tight text-center">
                {truncate(item.model.content.title, maxTitleLength)}
              </h2>
            </OverflowLengthTextTooltip>
          </div>
          <div className="w-full h-full flex items-center justify-between gap-2">
            {ExtraHeader && <ExtraHeader item={item} locale={locale} />}
            <p className="ml-auto">
              {format(parseISO(item.model.content.createdAt), "dd/MM/yyyy")}
            </p>
          </div>
          <p className="text-sm text-gray-500 dark:text-gray-400 h-36">
            {body && body.length > 285 ? body.slice(0, 285) + "..." : body}
          </p>
        </div>
        <hr className="border" />
        <Link href={`/users/single/${item.user?.id}`}>
          <div className="flex items-center justify-around cursor-pointer hover:underline mt-5">
            <p className="text-sm font-bold">{author}</p>
            <div className="flex items-center justify-center flex-1 gap-2">
              <Avatar>
                <AvatarImage
                  alt="User profile image"
                  className="rounded-full object-cover aspect-square"
                  src={item.user?.image || noImg.src}
                />
              </Avatar>
              <p className="text-sm">{item.user.email}</p>
            </div>
          </div>
        </Link>
        {ExtraContent && (
          <div className=" w-full">
            <ExtraContent item={item} locale={locale} />
          </div>
        )}
      </div>
    </div>
  );
}
