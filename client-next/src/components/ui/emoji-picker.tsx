"use client";

import {
  type EmojiPickerListCategoryHeaderProps,
  type EmojiPickerListEmojiProps,
  type EmojiPickerListRowProps,
  EmojiPicker as EmojiPickerPrimitive,
} from "frimousse";
import { LoaderIcon, SearchIcon } from "lucide-react";

import { cn } from "@/lib/utils";

function EmojiPicker({
  className,
  ...props
}: React.ComponentProps<typeof EmojiPickerPrimitive.Root>) {
  return (
    <EmojiPickerPrimitive.Root
      className={cn(
        "bg-popover text-popover-foreground isolate flex h-full w-fit flex-col overflow-hidden rounded-md",
        className,
      )}
      data-slot="emoji-picker"
      {...props}
    />
  );
}

function EmojiPickerSearch({
  className,
  ...props
}: React.ComponentProps<typeof EmojiPickerPrimitive.Search>) {
  return (
    <div
      className={cn("flex h-9 items-center gap-2 border-b px-3", className)}
      data-slot="emoji-picker-search-wrapper"
    >
      <SearchIcon className="size-4 shrink-0 opacity-50" />
      <EmojiPickerPrimitive.Search
        className="outline-hidden placeholder:text-muted-foreground flex h-10 w-full rounded-md bg-transparent py-3 text-sm disabled:cursor-not-allowed disabled:opacity-50 ring-none px-0.5"
        data-slot="emoji-picker-search"
        {...props}
      />
    </div>
  );
}

function EmojiPickerRow({ children, ...props }: EmojiPickerListRowProps) {
  return (
    <div {...props} className="scroll-my-1 px-1" data-slot="emoji-picker-row">
      {children}
    </div>
  );
}

function EmojiPickerEmoji({
  emoji,
  className,
  ...props
}: EmojiPickerListEmojiProps) {
  return (
    <button
      {...props}
      className={cn(
        "data-[active]:bg-accent flex size-7 items-center justify-center rounded-sm text-base",
        className,
      )}
      data-slot="emoji-picker-emoji"
    >
      {emoji.emoji}
    </button>
  );
}

function EmojiPickerCategoryHeader({
  category,
  ...props
}: EmojiPickerListCategoryHeaderProps) {
  return (
    <div
      {...props}
      className="bg-popover text-muted-foreground px-3 pb-2 pt-3.5 text-xs leading-none"
      data-slot="emoji-picker-category-header"
    >
      {category.label}
    </div>
  );
}

function EmojiPickerContent({
  className,
  noResultsText = "No emoji found.",
  ...props
}: React.ComponentProps<typeof EmojiPickerPrimitive.Viewport> & {
  noResultsText?: string;
}) {
  return (
    <EmojiPickerPrimitive.Viewport
      className={cn("outline-hidden relative flex-1", className)}
      data-slot="emoji-picker-viewport"
      {...props}
    >
      <EmojiPickerPrimitive.Loading
        className="absolute inset-0 flex items-center justify-center text-muted-foreground"
        data-slot="emoji-picker-loading"
      >
        <LoaderIcon className="size-4 animate-spin" />
      </EmojiPickerPrimitive.Loading>
      <EmojiPickerPrimitive.Empty
        className="absolute inset-0 flex items-center justify-center text-muted-foreground text-sm"
        data-slot="emoji-picker-empty"
      >
        {noResultsText}
      </EmojiPickerPrimitive.Empty>
      <EmojiPickerPrimitive.List
        className="select-none pb-1"
        components={{
          Row: EmojiPickerRow,
          Emoji: EmojiPickerEmoji,
          CategoryHeader: EmojiPickerCategoryHeader,
        }}
        data-slot="emoji-picker-list"
      />
    </EmojiPickerPrimitive.Viewport>
  );
}

function EmojiPickerFooter({
  className,
  selectText = "Select an emoji…",
  ...props
}: React.ComponentProps<"div"> & {
  selectText?: string;
}) {
  return (
    <div
      className={cn(
        "max-w-(--frimousse-viewport-width) flex w-full min-w-0 items-center gap-1 border-t p-2",
        className,
      )}
      data-slot="emoji-picker-footer"
      {...props}
    >
      <EmojiPickerPrimitive.ActiveEmoji>
        {({ emoji }) =>
          emoji ? (
            <>
              <div className="flex size-7 flex-none items-center justify-center text-lg">
                {emoji.emoji}
              </div>
              <span className="text-secondary-foreground truncate text-xs">
                {emoji.label}
              </span>
            </>
          ) : (
            <span className="text-muted-foreground ml-1.5 flex h-7 items-center truncate text-xs">
              {selectText}
            </span>
          )
        }
      </EmojiPickerPrimitive.ActiveEmoji>
    </div>
  );
}

interface ComposedEmojiPickerTexts {
  searchPlaceholder: string;
  noResultsText: string;
  selectText: string;
}

interface CustomEmojiPickerTextsProps {
  texts: ComposedEmojiPickerTexts;
  pickerProps?: React.ComponentProps<typeof EmojiPickerPrimitive.Root>;
  pickerSearchProps?: React.ComponentProps<typeof EmojiPickerPrimitive.Search>;
  pickerContentProps?: React.ComponentProps<
    typeof EmojiPickerPrimitive.Viewport
  >;
  pickerFooterProps?: React.ComponentProps<"div">;
}

const ComposedPicker = ({
  texts: { searchPlaceholder, noResultsText, selectText },
  pickerProps,
  pickerSearchProps,
  pickerContentProps,
  pickerFooterProps,
}: CustomEmojiPickerTextsProps) => {
  return (
    <EmojiPicker
      {...pickerProps}
      className={cn("h-[342px] w-[274px]", pickerProps?.className)}
    >
      <EmojiPickerSearch
        placeholder={searchPlaceholder}
        {...pickerSearchProps}
      />
      <EmojiPickerContent
        noResultsText={noResultsText}
        {...pickerContentProps}
      />
      <EmojiPickerFooter selectText={selectText} {...pickerFooterProps} />
    </EmojiPicker>
  );
};
export {
  EmojiPicker,
  EmojiPickerSearch,
  EmojiPickerContent,
  EmojiPickerFooter,
  ComposedPicker,
  type ComposedEmojiPickerTexts,
};
