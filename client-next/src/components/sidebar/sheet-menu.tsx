"use client";
import { MenuIcon } from "lucide-react";

import { Button } from "@/components/ui/button";

import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTrigger,
} from "@/components/ui/sheet";
import { Link } from "@/navigation/navigation";
import { Menu } from "@/components/sidebar/menu";
import {
  MappingListFunctionKeys,
  SidebarMenuTexts,
} from "@/components/sidebar/menu-list";
import Logo from "@/components/logo/logo";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

interface Props {
  texts: SidebarMenuTexts;
  mappingKey: MappingListFunctionKeys;
}

export function SheetMenu({ texts, mappingKey }: Props) {
  const { authUser } = useAuthUserMinRole();

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
        <Menu
          isOpen={true}
          texts={texts}
          mappingKey={mappingKey}
          authUser={authUser}
        />
      </SheetContent>
    </Sheet>
  );
}
