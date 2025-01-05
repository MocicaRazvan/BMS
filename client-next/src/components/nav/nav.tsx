"use client";

import { useSession } from "next-auth/react";

import { Link, usePathname } from "@/navigation";
import { ModeToggle } from "@/components/nav/theme-switch";
import { LocaleSwitcher } from "@/components/i18n/LocaleSwitcher";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import {
  createPlansLinks,
  createPostsLinks,
  createRecipesLinks,
  linkFactory,
} from "@/components/nav/links";
import { BurgerNav } from "@/components/nav/burger-nav";
import NavProfile from "@/components/nav/nav-profile";
import { DashboardIcon } from "@radix-ui/react-icons";
import NotificationPop from "@/components/nav/notification-pop";
import CartPop, { CartPopsTexts } from "@/components/nav/cart-pop";
import Logo from "@/components/logo/logo";
import { LockKeyhole } from "lucide-react";
import ActiveLink from "@/components/nav/active-link";
import { useMemo } from "react";
import { appendCreatedAtDesc } from "@/lib/utils";

export interface NavTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  postsTexts: Record<string, string>;
  recipesTexts: Record<string, string>;
  plansTexts: Record<string, string>;
  links: {
    posts: string;
    recipes: string;
    home: string;
    adminDashboard: string;
    chat: string;
    ingredients: string;
    plans: string;
    subscriptions: string;
    orders: string;
    kanban: string;
    trainerDashboard: string;
    calculator: string;
  };
}

interface NavProps extends NavTexts {
  cartPopTexts: CartPopsTexts;
}

export default function Nav({
  themeSwitchTexts,
  postsTexts,
  recipesTexts,
  links,
  plansTexts,
  cartPopTexts,
}: NavProps) {
  const session = useSession();

  const authUser = session?.data?.user;
  const pathName = usePathname();

  const isUser = authUser?.role === "ROLE_USER";
  const isTrainer = authUser?.role === "ROLE_TRAINER";
  const isAdmin = authUser?.role === "ROLE_ADMIN";
  const isAdminOrTrainer = isAdmin || isTrainer;

  const postsLinks = useMemo(
    () => linkFactory(authUser, createPostsLinks, postsTexts),
    [authUser, postsTexts],
  );
  const recipesLinks = useMemo(
    () => linkFactory(authUser, createRecipesLinks, recipesTexts),
    [authUser, recipesTexts],
  );
  const plansLinks = useMemo(
    () => linkFactory(authUser, createPlansLinks, plansTexts),
    [authUser, plansTexts],
  );

  return (
    <nav
      className="min-h-10 md:flex items-center justify-between px-2.5 py-2 border-b sticky top-0 bg-opacity-60 z-[49]
    w-full border-border/40 bg-background/95 backdrop-blur
     supports-[backdrop-filter]:bg-background/60 flex-wrap 2xl:border-l 2xl:border-r"
      id="top-item"
    >
      <div className="hidden xl:flex items-center justify-between w-full">
        <div className="flex items-center justify-center gap-1.5 me-1.5">
          <div className="mr-8 flex items-center justify-start gap-1.5">
            <Link
              href="/"
              className="font-bold hover:underline flex items-center justify-center gap-2 hover:scale-[1.03] transition-all
           px-3
              "
            >
              <Logo />
              {links.home}
            </Link>
            {isAdmin && (
              <Link
                href="/admin/dashboard"
                className="text-balance gap-1 font-bold hover:underline flex items-center justify-center hover:scale-[1.03] transition-all px-1"
              >
                <LockKeyhole className="w-6 h-6" />
                <p>{links.adminDashboard}</p>
              </Link>
            )}
            {isAdminOrTrainer && (
              <Link
                href={appendCreatedAtDesc(
                  `/trainer/user/${authUser?.id}/posts`,
                )}
                className="text-balance gap-1 font-bold hover:underline flex items-center justify-center hover:scale-[1.03] transition-all px-1"
              >
                <DashboardIcon className="w-6 h-6" />
                <p>{links.trainerDashboard}</p>
              </Link>
            )}
          </div>
          {authUser && (
            <div className="flex items-center text-lg  justify-center gap-4 flex-wrap">
              <ActiveLink
                isActive={pathName === "/subscriptions"}
                href={appendCreatedAtDesc("/subscriptions")}
              >
                {links.subscriptions}
              </ActiveLink>
              <ActiveLink
                href={appendCreatedAtDesc("/orders")}
                isActive={pathName === "/orders"}
              >
                {links.orders}
              </ActiveLink>
              <ActiveLink
                href={appendCreatedAtDesc("/posts/approved")}
                isActive={pathName === "/posts/approved"}
              >
                {links.posts}
              </ActiveLink>
              <ActiveLink
                href={appendCreatedAtDesc("/plans/approved")}
                isActive={pathName === "/plans/approved"}
              >
                {links.plans}
              </ActiveLink>
              <ActiveLink href="/chat" isActive={pathName === "/chat"}>
                {links.chat}
              </ActiveLink>
              <ActiveLink href="/kanban" isActive={pathName === "/kanban"}>
                {links.kanban}
              </ActiveLink>
              <ActiveLink
                href="/calculator"
                isActive={pathName === "/calculator"}
              >
                {links.calculator}
              </ActiveLink>
            </div>
          )}
        </div>
        <div
          className="mx-auto md:ml-auto md:mr-1 flex items-center justify-center gap-6
      mt-2 sm:mt-0
      "
        >
          {authUser && (
            <>
              <NavProfile authUser={authUser} />
              <NotificationPop authUser={authUser} />
              <CartPop authUser={authUser} cartPopTexts={cartPopTexts} />
            </>
          )}
          {!authUser && (
            <Link
              href={"/auth/signin"}
              className="font-bold hover:underline hover:scale-110 transition-all"
            >
              Sign In
            </Link>
          )}
          <LocaleSwitcher />
          <ModeToggle {...themeSwitchTexts} />
        </div>
      </div>
      <div className="xl:hidden w-full flex items-center justify-between">
        <BurgerNav
          authUser={authUser}
          postsLinks={postsLinks}
          recipesLinks={recipesLinks}
          plansLinks={plansLinks}
          isAdminOrTrainer={isAdminOrTrainer}
          isUser={isUser}
          isTrainer={isTrainer}
          isAdmin={isAdmin}
          texts={{
            themeSwitchTexts,
            postsTexts,
            links,
            recipesTexts,
            plansTexts,
          }}
        />
        <div className="flex items-center justify-center gap-5">
          {authUser && (
            <>
              <NotificationPop authUser={authUser} />
              <CartPop authUser={authUser} cartPopTexts={cartPopTexts} />
            </>
          )}
          <LocaleSwitcher />
          <ModeToggle {...themeSwitchTexts} />
        </div>
      </div>
    </nav>
  );
}
