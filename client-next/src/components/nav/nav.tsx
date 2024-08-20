"use client";

import { useSession } from "next-auth/react";
import { useMemo, useState } from "react";
// import CartPop from "./cart-pop";
import { Link } from "@/navigation";
import { ModeToggle } from "@/components/nav/theme-switch";
import { LocaleSwitcher } from "@/components/i18n/LocaleSwitcher";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import {
  createPlansLinks,
  createPostsLinks,
  createRecipesLinks,
  linkFactory,
} from "@/components/nav/links";
import { MenuBarMenuNav } from "@/components/nav/menu-bar-menu-nav";
import { BurgerNav } from "@/components/nav/burger-nav";
import NavProfile from "@/components/nav/nav-profile";
import { DashboardIcon } from "@radix-ui/react-icons";
import NotificationPop from "@/components/nav/notification-pop";
import CartPop from "@/components/nav/cart-pop";
import Logo from "@/components/logo/logo";

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
  };
}

export default function Nav({
  themeSwitchTexts,
  postsTexts,
  recipesTexts,
  links,
  plansTexts,
}: NavTexts) {
  const session = useSession();
  const [showProfile, setShowProfile] = useState(false);

  const authUser = session?.data?.user;

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
      className="min-h-10 md:flex items-center justify-between px-4 py-2 border-b sticky top-0 bg-opacity-60 z-[49]
    w-full border-border/40 bg-background/95 backdrop-blur
     supports-[backdrop-filter]:bg-background/60 flex-wrap 2xl:border-l 2xl:border-r"
    >
      <div className="hidden xl:flex items-center justify-between w-full">
        <div className="flex items-center justify-center gap-4 me-3">
          <div className="mr-8 flex items-center justify-start gap-3">
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
                className="font-bold hover:underline flex items-center justify-center gap-2 hover:scale-[1.03] transition-all
              px-3"
              >
                <DashboardIcon className="w-6 h-6" />
                <p className="text-center">{links.adminDashboard}</p>
              </Link>
            )}
          </div>{" "}
          <div className="flex items-center  justify-center gap-3 flex-wrap">
            {authUser && (
              <>
                <Link
                  href="/subscriptions"
                  className="font-bold hover:underline hover:scale-[1.03] transition-all"
                >
                  {links.subscriptions}
                </Link>
                <Link
                  href="/orders"
                  className="font-bold hover:underline hover:scale-[1.03] transition-all"
                >
                  {links.orders}
                </Link>
              </>
            )}
            {isUser && (
              <>
                <Link
                  href="/posts/approved"
                  className="font-bold hover:underline hover:scale-[1.03] transition-all"
                >
                  {links.posts}
                </Link>
                <Link
                  href="/plans/approved"
                  className="font-bold hover:underline hover:scale-[1.03] transition-all"
                >
                  {links.plans}
                </Link>{" "}
              </>
            )}

            <MenuBarMenuNav
              title={links.posts}
              render={!isUser}
              links={postsLinks}
              authUser={authUser}
            />
            <MenuBarMenuNav
              title={links.recipes}
              render={!isUser}
              links={recipesLinks}
              authUser={authUser}
            />
            <MenuBarMenuNav
              title={links.plans}
              render={!isUser}
              links={plansLinks}
              authUser={authUser}
            />
            {isAdminOrTrainer && (
              <Link
                href="/trainer/ingredients"
                className="font-bold hover:underline hover:scale-[1.03] transition-all"
              >
                {links.ingredients}
              </Link>
            )}
            {authUser && (
              <>
                <Link
                  href="/chat"
                  className="font-bold hover:underline hover:scale-[1.03] transition-all"
                >
                  {links.chat}
                </Link>
                <Link
                  href="/kanban"
                  className="font-bold hover:underline hover:scale-[1.03] transition-all"
                >
                  {links.kanban}
                </Link>
              </>
            )}
            {/*<Link href={"/auth/confirm-email"}>Confirm email</Link>*/}
            {/*<Link href={"/auth/forgot-password"}>Forgot passowrd</Link>*/}
            {/*<Link href={"/auth/reset-password"}>Reset passowrd</Link>*/}
            {/*<Link href={"/auth/signin"}>Signin</Link>*/}
            {/*<Link href={"/auth/signout"}>Signout</Link>*/}
            {/*<Link href={"/auth/signup"}>Signup</Link>*/}
          </div>
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
              <CartPop authUser={authUser} />
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
              <CartPop authUser={authUser} />
            </>
          )}
          <LocaleSwitcher />
          <ModeToggle {...themeSwitchTexts} />
        </div>
      </div>
    </nav>
  );
}
