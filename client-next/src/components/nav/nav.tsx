"use client";

import { useSession } from "next-auth/react";
import { Link, Locale, usePathname } from "@/navigation/navigation";
import { ModeToggle } from "@/components/nav/theme-switch";
import { LocaleSwitcher } from "@/components/i18n/LocaleSwitcher";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { BurgerNav } from "@/components/nav/burger-nav";
import NavProfile from "@/components/nav/nav-profile";
import { DashboardIcon } from "@radix-ui/react-icons";
import NotificationPop from "@/components/nav/notification-pop";
import CartPop, { CartPopsTexts } from "@/components/nav/cart-pop";
import Logo from "@/components/logo/logo";
import { LockKeyhole } from "lucide-react";
import { memo, useMemo } from "react";
import { appendCreatedAtDesc } from "@/lib/utils";
import FindInSite from "@/components/nav/find-in-site";
import { DaysCalendarCTATexts } from "@/components/days-calendar/days-calendar-cta";
import { NavButtonGroup, NavItem } from "@/components/nav/nav-button";
import { SheetClose } from "@/components/ui/sheet";
import { Avatar, AvatarImage } from "@/components/ui/avatar";
import ActiveLink from "@/components/nav/active-link";
import {
  FindInSiteTexts,
  MetadataValue,
} from "@/components/nav/find-in-site-content";
import { useMedia } from "react-use";
import { WithUser } from "@/lib/user";

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
  findInSiteTexts: FindInSiteTexts;
  dayCalendarCTATexts: DaysCalendarCTATexts;
}

interface ItemsTexts {
  dayCalendarCTATexts: DaysCalendarCTATexts;
  cartPopTexts: CartPopsTexts;
}

interface NavProps extends NavTexts, ItemsTexts {
  metadataValues: MetadataValue[];
  locale: Locale;
}

export default function Nav({
  themeSwitchTexts,
  postsTexts,
  recipesTexts,
  links,
  plansTexts,
  cartPopTexts,
  findInSiteTexts,
  metadataValues,
  dayCalendarCTATexts,
  locale,
}: NavProps) {
  const session = useSession();
  const authUser = session?.data?.user;

  const isUser = authUser?.role === "ROLE_USER";
  const isTrainer = authUser?.role === "ROLE_TRAINER";
  const isAdmin = authUser?.role === "ROLE_ADMIN";
  const isAdminOrTrainer = isAdmin || isTrainer;

  const baseLinks: NavItem[] = useMemo(
    () => [
      {
        link: appendCreatedAtDesc("/subscriptions"),
        name: links.subscriptions,
        isActive: (pathName) => pathName === "/subscriptions",
      },
      {
        link: appendCreatedAtDesc("/orders"),
        name: links.orders,
        isActive: (pathName) => pathName === "/orders",
      },
      {
        link: appendCreatedAtDesc("/posts/approved"),
        name: links.posts,
        isActive: (pathName) => pathName === "/posts/approved",
      },
      {
        link: appendCreatedAtDesc("/plans/approved"),
        name: links.plans,
        isActive: (pathName) => pathName === "/plans/approved",
      },
      {
        link: "/chat",
        name: links.chat,
        isActive: (pathName) => pathName === "/chat",
      },
      {
        link: "/kanban",
        name: links.kanban,
        isActive: (pathName) => pathName === "/kanban",
      },
      {
        link: "/calculator",
        name: links.calculator,
        isActive: (pathName) => pathName === "/calculator",
      },
    ],
    [
      links.calculator,
      links.chat,
      links.kanban,
      links.orders,
      links.plans,
      links.posts,
      links.subscriptions,
    ],
  );

  const privilegedLinks = useMemo(() => {
    const privilegedLinks: NavItem[] = [];
    if (isAdmin) {
      privilegedLinks.push({
        link: "/admin/dashboard",
        name: links.adminDashboard,
        Icon: LockKeyhole,
      });
    }
    if (isAdminOrTrainer) {
      privilegedLinks.push({
        link: appendCreatedAtDesc(`/trainer/user/${authUser?.id}/posts`),
        name: links.trainerDashboard,
        Icon: DashboardIcon,
      });
    }
    return privilegedLinks;
  }, [
    authUser?.id,
    isAdmin,
    isAdminOrTrainer,
    links.adminDashboard,
    links.trainerDashboard,
  ]);

  const linkItems: NavItem[] = useMemo(() => {
    const finalLinks: NavItem[] = [
      {
        link: "/",
        isActive: (pathName) => pathName === "/",
        additional: <Logo width={40} height={40} />,
        linkClassName: "mr-1.5 2xl:mr-3 px-1 md:px-1 py-1 md:py-1 rounded-full",
      },
    ];

    finalLinks.push(...privilegedLinks);

    finalLinks.push(...baseLinks);

    return finalLinks;
  }, [baseLinks, privilegedLinks]);

  const burgerItems: NavItem[] = useMemo(() => {
    const finalLinks: NavItem[] = [
      {
        link: "/",
        isActive: (pathName) => pathName === "/",
        additional: (
          <div className="flex items-center gap-1.5">
            <Logo width={40} height={40} />
            <span className="md:text-lg">{links.home}</span>
          </div>
        ),
        separator: <hr className="border my-2.5 w-full" />,
      },
    ];
    const burgerPrivileged: NavItem[] = [];
    if (isAdmin) {
      burgerPrivileged.push({
        link: "/admin/dashboard",
        name: links.adminDashboard,
        Icon: LockKeyhole,
      });
    }
    if (isAdminOrTrainer) {
      burgerPrivileged.push({
        link: appendCreatedAtDesc(`/trainer/user/${authUser?.id}/posts`),
        name: links.trainerDashboard,
        Icon: DashboardIcon,
        separator: <hr className="border my-2.5 w-full" />,
      });
    }

    finalLinks.push(...burgerPrivileged);

    finalLinks.push(...baseLinks);
    if (authUser) {
      finalLinks.push({
        link: `/users/single/${authUser?.id}`,
        beforeSeparator: <hr className="border my-2.5 w-full" />,
        separator: <hr className="border my-2.5 w-full" />,
        isActive: (pathName) => pathName === `/users/single/${authUser?.id}`,
        additional: (
          <SheetClose className="flex items-center justify-start gap-2 h-full w-full ps-2">
            {authUser.image ? (
              <div className="flex items-center justify-between w-full gap-2 ">
                <Avatar className="cursor-pointer">
                  <AvatarImage src={authUser?.image} alt={authUser?.email} />
                </Avatar>
                <p className="cursor-pointer text-xs hover:underline max-w-[230px] truncate">
                  {authUser.email}
                </p>
              </div>
            ) : (
              <p className="cursor-pointer text-sm">{authUser.email}</p>
            )}
          </SheetClose>
        ),
      });
    }

    return finalLinks;
  }, [
    authUser,
    baseLinks,
    isAdmin,
    isAdminOrTrainer,
    links.adminDashboard,
    links.home,
    links.trainerDashboard,
  ]);

  return (
    <nav
      className="min-h-10 md:flex items-center justify-between px-2.5 py-2 border-b sticky top-0 bg-opacity-60 z-[49]
    w-full border-border/40 bg-background/95 backdrop-blur
     supports-[backdrop-filter]:bg-background/60 flex-wrap 2xl:border-l 2xl:border-r"
      id="top-item"
    >
      <div className="hidden lgxl:flex items-center justify-between w-full">
        <div className="flex items-center justify-center gap-1 ">
          {authUser ? (
            <div className="flex items-start text-lg  justify-center gap-4 flex-wrap">
              <NavButtonGroup items={linkItems} />
            </div>
          ) : (
            <>
              <Link
                href="/"
                className="mr-1.5 2xl:mr-3 px-1 md:px-1 py-1 md:py-1 rounded-full  hover:scale-105 transition-transform duration-200 ease-in-out"
              >
                <Logo width={40} height={40} />
              </Link>
              <div className="ms-6">
                <NoUserNav calculatorTexts={links.calculator} />
              </div>
            </>
          )}
        </div>
        <div className="mx-auto  md:mr-1 flex items-center justify-center gap-6 md:gap-3 mt-2 sm:mt-0">
          <ScreenAwareFind
            texts={findInSiteTexts}
            metadataValues={metadataValues}
            small={false}
          />
          <UserNavItems
            authUser={authUser}
            dayCalendarCTATexts={dayCalendarCTATexts}
            cartPopTexts={cartPopTexts}
            locale={locale}
          />
          {!authUser && <SignInLink />}
          <LocaleSwitcher />
          <ModeToggle {...themeSwitchTexts} />
        </div>
      </div>
      <div className="lgxl:hidden w-full flex items-center justify-between">
        <BurgerNav
          authUser={authUser}
          texts={{
            themeSwitchTexts,
            postsTexts,
            links,
            recipesTexts,
            plansTexts,
            findInSiteTexts,
            dayCalendarCTATexts,
          }}
          linkItems={burgerItems}
        />
        <div className="flex items-center justify-center gap-5 ">
          <div className="mx-auto  md:mr-1 flex items-center justify-center gap-6 md:gap-3 mt-2 sm:mt-0">
            <ScreenAwareFind
              texts={findInSiteTexts}
              metadataValues={metadataValues}
              small={true}
            />
          </div>
          <UserNavItems
            authUser={authUser}
            dayCalendarCTATexts={dayCalendarCTATexts}
            cartPopTexts={cartPopTexts}
            locale={locale}
          />
          <LocaleSwitcher />
          <ModeToggle {...themeSwitchTexts} />
        </div>
      </div>
    </nav>
  );
}

const UserNavItems = memo(
  (
    props: ItemsTexts &
      Partial<WithUser> & {
        locale: Locale;
      },
  ) => {
    if (!props.authUser) return null;
    return (
      <>
        <NavProfile
          authUser={props.authUser}
          dayCalendarCTATexts={props.dayCalendarCTATexts}
        />
        <NotificationPop authUser={props.authUser} locale={props.locale} />
        <CartPop authUser={props.authUser} cartPopTexts={props.cartPopTexts} />
      </>
    );
  },
);

UserNavItems.displayName = "UserNavItems";

function ScreenAwareFind(props: {
  texts: FindInSiteTexts;
  metadataValues: MetadataValue[];
  small: boolean;
}) {
  const lgxl = useMedia("(min-width: 1485px)", false);
  const render = props.small ? !lgxl : lgxl;
  if (!render) return null;
  return (
    <FindInSite texts={props.texts} metadataValues={props.metadataValues} />
  );
}

function NoUserNav({ calculatorTexts }: { calculatorTexts: string }) {
  const session = useSession();
  const pathname = usePathname();
  if (session.status === "loading") return null;
  return (
    <ActiveLink href="/calculator" isActive={pathname === "/calculator"}>
      {calculatorTexts}
    </ActiveLink>
  );
}
function SignInLink() {
  const session = useSession();
  if (session.status === "loading" || session.status === "authenticated")
    return null;
  return (
    <Link
      href="/auth/signin"
      className="font-bold hover:underline hover:scale-110 transition-all"
    >
      {"Sign In"}
    </Link>
  );
}
