"use client";
import { Input } from "@/components/ui/input";
import { Search, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { ChangeEvent } from "react";

export interface SearchInputTexts {
  placeholder: string;
}

export interface SearchInputProps {
  value: string;
  onChange: (e: ChangeEvent<HTMLInputElement>) => void;
  onClear?: () => void;
  searchInputTexts: SearchInputTexts;
  className?: string;
  autoFocus?: boolean;
}

export default function SearchInput({
  value,
  onChange,
  onClear,
  searchInputTexts: { placeholder },
  className,
  autoFocus = true,
}: SearchInputProps) {
  return (
    <div className={cn("relative w-[350px] group", className)}>
      <Input
        className="px-10  w-full"
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        autoFocus={autoFocus}
      />
      <Search className="absolute left-0 top-0 m-2.5 h-4 w-4 text-muted-foreground group-focus:text-ring" />
      {value !== "" && onClear && (
        <Button
          variant={"outline"}
          size={"icon"}
          className="absolute right-0 top-0 m-2.5 h-5 w-5 text-muted-foreground group-focus:text-ring cursor-pointer"
          onClick={() => onClear()}
        >
          <X className="w-4 h-4" />
        </Button>
      )}
    </div>
  );
}
