"use client";
import { Input } from "@/components/ui/input";
import { Search, X } from "lucide-react";
import { Button } from "@/components/ui/button";

export interface SearchInputTexts {
  placeholder: string;
}

export interface SearchInputProps {
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onClear: () => void;
  searchInputTexts: SearchInputTexts;
}

export default function SearchInput({
  value,
  onChange,
  onClear,
  searchInputTexts: { placeholder },
}: SearchInputProps) {
  return (
    <div className="relative w-[350px] group">
      <Input
        className="px-10  w-full"
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        autoFocus
      />
      <Search className="absolute left-0 top-0 m-2.5 h-4 w-4 text-muted-foreground group-focus:text-ring" />
      <Button
        variant={"outline"}
        size={"icon"}
        className="absolute right-0 top-0 m-2.5 h-5 w-5 text-muted-foreground group-focus:text-ring cursor-pointer"
        onClick={() => onClear()}
      >
        <X className="w-4 h-4" />
      </Button>
    </div>
  );
}
