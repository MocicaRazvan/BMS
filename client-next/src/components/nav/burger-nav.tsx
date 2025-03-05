"use client";

import { memo, useState } from "react";
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetTrigger,
} from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { Home, LockKeyhole, LogOut, Menu } from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Session } from "next-auth";
import { ComponentMenuLink } from "@/components/nav/links";
import { appendCreatedAtDesc, isDeepEqual } from "@/lib/utils";
import { AvatarImage } from "@radix-ui/react-avatar";
import { Avatar } from "@/components/ui/avatar";
import { Link, usePathname } from "@/navigation";
import { NavTexts } from "@/components/nav/nav";
import { DashboardIcon } from "@radix-ui/react-icons";
import ActiveLink from "@/components/nav/active-link";
import FindInSite, { MetadataValue } from "@/components/nav/find-in-site";

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
  metadataValues: MetadataValue[];
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
    metadataValues,
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
            <div>
              <FindInSite
                texts={texts.findInSiteTexts}
                metadataValues={metadataValues}
              />
            </div>
            {authUser && (
              <>
                <div className="mt-5 transition-all ps-2 hover:scale-[1.02] text-lg">
                  <ActiveLink
                    isActive={pathName === "/subscriptions"}
                    href={appendCreatedAtDesc("/subscriptions")}
                  >
                    {texts.links.subscriptions}
                  </ActiveLink>
                </div>
                <div className="mt-5 transition-all ps-2 hover:scale-[1.02] text-lg">
                  <ActiveLink
                    href={appendCreatedAtDesc("/orders")}
                    isActive={pathName === "/orders"}
                  >
                    {texts.links.orders}
                  </ActiveLink>
                </div>
                <div className="mt-5 transition-all ps-2 hover:scale-[1.02] text-lg">
                  <ActiveLink
                    href={appendCreatedAtDesc("/posts/approved")}
                    isActive={pathName === "/posts/approved"}
                  >
                    {texts.links.posts}
                  </ActiveLink>
                </div>
                <div className="mt-5 transition-all ps-2 hover:scale-[1.02] text-lg">
                  <ActiveLink
                    href={appendCreatedAtDesc("/plans/approved")}
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
                <div className="mt-5 ps-2 flex items-center justify-start transition-all hover:scale-[1.03] ">
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
                      <p>Sign out</p>
                    </Link>
                  </Button>
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
                  href={appendCreatedAtDesc(
                    `/trainer/user/${authUser?.id}/posts`,
                  )}
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
    prevProps.isAdminOrTrainer === nextProps.isAdminOrTrainer &&
    prevProps.isUser === nextProps.isUser &&
    prevProps.isTrainer === nextProps.isTrainer &&
    prevProps.isAdmin === nextProps.isAdmin,
);

BurgerNav.displayName = "BurgerNav";

export { BurgerNav };
