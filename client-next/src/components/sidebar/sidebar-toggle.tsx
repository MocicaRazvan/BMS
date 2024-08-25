"use client";
import { useSidebarToggle } from "@/context/sidebar-toggle";
import { Button } from "@/components/ui/button";
import { ChevronLeft } from "lucide-react";
import { cn } from "@/lib/utils";

export default function SidebarToggle() {
  const { isOpen, toggleIsOpen } = useSidebarToggle();
  return (
    <div className="invisible lg:visible absolute top-[12px] -right-[16px] z-20">
      <Button
        onClick={() => toggleIsOpen()}
        className="rounded-md w-8 h-8"
        variant="outline"
        size="icon"
      >
        <ChevronLeft
          className={cn(
            "h-4 w-4 transition-transform ease-in-out duration-700",
            !isOpen ? "rotate-180" : "rotate-0",
          )}
        />
      </Button>
    </div>
  );
}
