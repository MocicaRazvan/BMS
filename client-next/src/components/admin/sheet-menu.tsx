"use client";
import { MenuIcon } from "lucide-react";

import { Button } from "@/components/ui/button";

import {
  Sheet,
  SheetHeader,
  SheetContent,
  SheetTrigger,
} from "@/components/ui/sheet";
import { Link } from "@/navigation";
import { Menu } from "@/components/admin/menu";
import { AdminMenuTexts } from "@/components/admin/menu-list";
import Logo from "@/components/logo/logo";

interface Props {
  texts: AdminMenuTexts;
}

export function SheetMenu({ texts }: Props) {
  return (
    <Sheet>
      <SheetTrigger className="lg:hidden" asChild>
        <Button className="h-8" variant="outline" size="icon">
          <MenuIcon size={20} />
        </Button>
      </SheetTrigger>
      <SheetContent className="sm:w-72 px-3 h-full flex flex-col" side="left">
        <SheetHeader>
          <Button
            className="flex justify-center items-center pb-2 pt-1"
            variant="link"
            asChild
          >
            <Link href="/" className="flex items-center gap-2">
              <div className="me-1">
                <Logo />
              </div>
              <h1 className="font-bold text-lg">{texts.mainSite}</h1>
            </Link>
          </Button>
        </SheetHeader>
        <Menu isOpen texts={texts} />
      </SheetContent>
    </Sheet>
  );
}
