"use client";

import { memo, useState } from "react";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetTrigger,
} from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { Home, LogOut, Menu } from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Session } from "next-auth";
import { isDeepEqual } from "@/lib/utils";
import { Link } from "@/navigation";
import { NavTexts } from "@/components/nav/nav";
import { NavButtonGroup, NavItem } from "@/components/nav/nav-button";

interface Props {
  authUser: Session["user"];

  texts: NavTexts;
  linkItems: NavItem[];
}

const BurgerNav = memo<Props>(({ authUser, texts, linkItems }: Props) => {
  const [sheetOpen, setSheetOpen] = useState(false);
  return (
    <Sheet open={sheetOpen} onOpenChange={setSheetOpen} modal={true}>
      <SheetTrigger asChild>
        <Button className="sticky top-4 left-6 z-50" variant="outline">
          <Menu />
        </Button>
      </SheetTrigger>
      <SheetContent
        side="left"
        className="w-[75%] z-[100] min-h-[100vh]"
        closeClassNames="h-5 w-5"
      >
        <ScrollArea className="pb-8 px-4 my-8  h-full  flex flex-col gap-6 ">
          {!authUser && (
            <div className="mb-8">
              <Link
                href="/"
                className="font-bold hover:underline flex items-center justify-start gap-2 text-xl transition-all hover:scale-[1.02]"
              >
                <SheetClose className="flex items-center justify-start gap-2 h-full w-full">
                  <Home className="h-8 w-8" /> {texts.links.home}
                </SheetClose>
              </Link>
              <hr className="border my-5" />
            </div>
          )}

          {authUser && (
            <div className="pt-2.5">
              <NavButtonGroup
                items={linkItems}
                className="flex-col items-start gap-0.5"
              />
            </div>
          )}

          {!authUser && (
            <>
              <div className="mt-5 ps-2 transition-all hover:scale-[1.02] text-lg">
                <Link
                  href="/auth/signin"
                  className="font-bold hover:underline text-lg transition-all hover:scale-[1.02]"
                >
                  {"Sign In"}
                </Link>
              </div>
            </>
          )}

          {authUser && (
            <div className="mt-5 mb-1 ps-2 flex items-center justify-start transition-all hover:scale-[1.03] ">
              <Button
                asChild
                variant="outline"
                className="w-5/6 justify-center"
              >
                <Link
                  href={`/auth/signout`}
                  className="flex items-center gap-2"
                >
                  <LogOut size={18} />
                  <p>{"Sign out"}</p>
                </Link>
              </Button>
            </div>
          )}
        </ScrollArea>
      </SheetContent>
    </Sheet>
  );
}, isDeepEqual);

BurgerNav.displayName = "BurgerNav";

export { BurgerNav };
