"use client";

import { memo, useEffect, useRef, useState } from "react";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetTrigger,
} from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { Home, Menu } from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Session } from "next-auth";
import { ComponentMenuLink, LinkNav, linksEqual } from "@/components/nav/links";
import { MenuBarMenuNav } from "@/components/nav/menu-bar-menu-nav";
import { isDeepEqual } from "@/lib/utils";
import { AvatarImage } from "@radix-ui/react-avatar";
import { Avatar } from "@/components/ui/avatar";
import { Link } from "@/navigation";
import { AccordionBarMenuNav } from "@/components/nav/accordion-bar-menu-nav";
import { NavTexts } from "@/components/nav/nav";
import { DashboardIcon } from "@radix-ui/react-icons";
import NavProfile from "@/components/nav/nav-profile";

interface Props {
  authUser: Session["user"];
  postsLinks: ComponentMenuLink[];
  recipesLinks: ComponentMenuLink[];
  plansLinks: ComponentMenuLink[];
  isAdminOrTrainer: boolean;
  isUser: boolean;
  isTrainer: boolean;
  isAdmin: boolean;
  texts: NavTexts;
}

const BurgerNav = memo<Props>(
  ({
    authUser,
    postsLinks,
    isAdminOrTrainer,
    isUser,
    isTrainer,
    isAdmin,
    texts,
    recipesLinks,
    plansLinks,
  }: Props) => {
    const [sheetOpen, setSheetOpen] = useState(false);

    return (
      <Sheet open={sheetOpen} onOpenChange={setSheetOpen} modal={true}>
        <SheetTrigger asChild>
          <Button className="sticky top-4 left-6 z-50">
            <Menu />
          </Button>
        </SheetTrigger>
        <SheetContent
          side={"left"}
          className="w-[75%] z-[100] min-h-[100vh] "
          closeClassNames="h-8 w-8"
        >
          <ScrollArea className="py-4 pb-10 px-4 my-8  h-full  flex flex-col gap-6 ">
            <div className="mb-8">
              <Link
                href="/"
                className="font-bold hover:underline flex items-center justify-start gap-2 text-xl transition-all hover:scale-[1.02]"
              >
                <SheetClose className="flex items-center justify-start gap-2 h-full w-full">
                  <Home className="h-8 w-8" /> {texts.links.home}
                </SheetClose>
              </Link>
            </div>
            {isUser && (
              <div>
                <Link
                  href="/posts/approved"
                  className="font-bold hover:underline text-lg transition-all hover:scale-[1.02]"
                >
                  {texts.links.posts}
                </Link>
              </div>
            )}
            <AccordionBarMenuNav
              title={texts.links.posts}
              render={!isUser}
              links={postsLinks}
              authUser={authUser}
              setSheetOpen={setSheetOpen}
            />
            <AccordionBarMenuNav
              title={texts.links.recipes}
              render={!isUser}
              links={recipesLinks}
              authUser={authUser}
              setSheetOpen={setSheetOpen}
            />{" "}
            <AccordionBarMenuNav
              title={texts.links.plans}
              render={!isUser}
              links={plansLinks}
              authUser={authUser}
              setSheetOpen={setSheetOpen}
            />
            {authUser && (
              <>
                <div className="mt-5 transition-all ps-2 hover:scale-[1.02] text-lg">
                  <Link
                    href="/subscriptions"
                    className="font-bold hover:underline hover:scale-[1.02] transition-all"
                  >
                    {texts.links.subscriptions}
                  </Link>
                </div>{" "}
                <div className="mt-5 transition-all ps-2 hover:scale-[1.02] text-lg">
                  <Link
                    href="/subscriptions"
                    className="font-bold hover:underline hover:scale-[1.02] transition-all"
                  >
                    {texts.links.orders}
                  </Link>
                </div>
              </>
            )}
            {isAdminOrTrainer && (
              <div className="mt-5 transition-all ps-2 hover:scale-[1.02] text-lg">
                <Link
                  href="/trainer/ingredients"
                  className="font-bold hover:underline transition-all hover:scale-[1.02]"
                >
                  {texts.links.ingredients}
                </Link>{" "}
              </div>
            )}
            {authUser && (
              <div className="mt-5 ps-2 transition-all hover:scale-[1.02] text-lg">
                <Link
                  href="/chat"
                  className="font-bold hover:underline transition-all hover:scale-[1.02]"
                >
                  {texts.links.chat}
                </Link>
              </div>
            )}{" "}
            {authUser && (
              <div className="mt-5 ps-2 transition-all hover:scale-[1.02] text-lg">
                <Link
                  href="/kanban"
                  className="font-bold hover:underline transition-all hover:scale-[1.02]"
                >
                  {texts.links.kanban}
                </Link>
              </div>
            )}
            {isAdmin && (
              <div className="mt-5 ps-2 ">
                <Link
                  href="/admin/dashboard"
                  className="font-bold hover:underline flex items-center justify-start gap-2 transition-all hover:scale-[1.02]"
                >
                  <DashboardIcon /> {texts.links.adminDashboard}
                </Link>
              </div>
            )}
            {!authUser && (
              <div className="mt-5 ps-2 transition-all hover:scale-[1.02] text-lg">
                <Link
                  href="/auth/signin"
                  className="font-bold hover:underline text-lg transition-all hover:scale-[1.02]"
                >
                  Sign In
                </Link>
              </div>
            )}
            {authUser && (
              <SheetClose className="flex items-center justify-start gap-2 h-full w-full ps-2 hover:scale-[1.03] transition-all text-lg mt-12">
                <Link
                  href={`/users/single/${authUser?.id}`}
                  className="text-lg hover:underline font-bold block "
                >
                  {authUser.image ? (
                    <Avatar className="cursor-pointer">
                      <AvatarImage
                        src={authUser?.image}
                        alt={authUser?.email}
                      />
                    </Avatar>
                  ) : (
                    <p className="cursor-pointer text-sm hover:underline">
                      {authUser.email}
                    </p>
                  )}
                </Link>
              </SheetClose>
            )}
          </ScrollArea>
        </SheetContent>
      </Sheet>
    );
  },
  (prevProps, nextProps) =>
    isDeepEqual(prevProps.authUser, nextProps.authUser) &&
    // linksEqual(prevProps.postsLinks, nextProps.postsLinks) &&
    // linksEqual(prevProps.exercisesLinks, nextProps.exercisesLinks) &&
    // linksEqual(prevProps.trainingsLinks, nextProps.trainingsLinks) &&
    // linksEqual(prevProps.ordersLinks, nextProps.ordersLinks) &&
    prevProps.isAdminOrTrainer === nextProps.isAdminOrTrainer &&
    prevProps.isUser === nextProps.isUser &&
    prevProps.isTrainer === nextProps.isTrainer &&
    prevProps.isAdmin === nextProps.isAdmin,
);

BurgerNav.displayName = "BurgerNav";

export { BurgerNav };
