"use client";

import { memo, useEffect, useRef, useState } from "react";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetTrigger,
} from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { Home, LockKeyhole, Menu } from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Session } from "next-auth";
import { ComponentMenuLink, LinkNav, linksEqual } from "@/components/nav/links";
import { MenuBarMenuNav } from "@/components/nav/menu-bar-menu-nav";
import { isDeepEqual } from "@/lib/utils";
import { AvatarImage } from "@radix-ui/react-avatar";
import { Avatar } from "@/components/ui/avatar";
import { Link, usePathname } from "@/navigation";
import { AccordionBarMenuNav } from "@/components/nav/accordion-bar-menu-nav";
import { NavTexts } from "@/components/nav/nav";
import { DashboardIcon } from "@radix-ui/react-icons";
import NavProfile from "@/components/nav/nav-profile";
import ActiveLink from "@/components/nav/active-link";

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
    const pathName = usePathname();
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

            {/*/>*/}
            {authUser && (
              <>
                <div className="mt-5 transition-all ps-2 hover:scale-[1.02] text-lg">
                  <ActiveLink
                    isActive={pathName === "/subscriptions"}
                    href={"/subscriptions"}
                  >
                    {texts.links.subscriptions}
                  </ActiveLink>
                </div>
                <div className="mt-5 transition-all ps-2 hover:scale-[1.02] text-lg">
                  <ActiveLink href="/orders" isActive={pathName === "/orders"}>
                    {texts.links.orders}
                  </ActiveLink>
                </div>
                <div className="mt-5 transition-all ps-2 hover:scale-[1.02] text-lg">
                  <ActiveLink
                    href="/posts/approved"
                    isActive={pathName === "/posts/approved"}
                  >
                    {texts.links.posts}
                  </ActiveLink>
                </div>
                <div className="mt-5 transition-all ps-2 hover:scale-[1.02] text-lg">
                  <ActiveLink
                    href="/plans/approved"
                    isActive={pathName === "/plans/approved"}
                  >
                    {texts.links.plans}
                  </ActiveLink>
                </div>
                <div className="mt-5 ps-2 transition-all hover:scale-[1.02] text-lg">
                  <ActiveLink href="/chat" isActive={pathName === "/chat"}>
                    {texts.links.chat}
                  </ActiveLink>
                </div>
                <div className="mt-5 ps-2 transition-all hover:scale-[1.02] text-lg">
                  <ActiveLink href="/kanban" isActive={pathName === "/kanban"}>
                    {texts.links.kanban}
                  </ActiveLink>
                </div>
                <div className="mt-5 ps-2 transition-all hover:scale-[1.02] text-lg">
                  <ActiveLink
                    href="/calculator"
                    isActive={pathName === "/calculator"}
                  >
                    {texts.links.calculator}
                  </ActiveLink>
                </div>
              </>
            )}
            <hr className="border my-5" />
            {isAdmin && (
              <div className="mt-5 ps-2 ">
                <Link
                  href="/admin/dashboard"
                  className="font-bold hover:underline flex items-center justify-start gap-2 transition-all hover:scale-[1.02]"
                >
                  <LockKeyhole /> {texts.links.adminDashboard}
                </Link>
              </div>
            )}
            {isAdminOrTrainer && (
              <div className="mt-5 ps-2 ">
                <Link
                  href={`/trainer/user/${authUser?.id}/posts`}
                  className="font-bold hover:underline flex items-center justify-start gap-2 transition-all hover:scale-[1.02]"
                >
                  <DashboardIcon /> {texts.links.trainerDashboard}
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
