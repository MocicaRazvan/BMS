"use client";

import { Role } from "@/types/fetch-utils";
import { Session } from "next-auth";
import { MenubarItem, MenubarSeparator } from "@/components/ui/menubar";
import { Fragment, memo, useCallback, useMemo } from "react";
import { cn, isDeepEqual } from "@/lib/utils";
import {
  ComponentMenuLink,
  LinkNav,
  shouldRenderLink,
} from "@/components/nav/links";
import {
  DropdownMenuItem,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu";
import { Link, usePathname } from "@/navigation";

interface MenuBarItemsNavProps {
  links: ComponentMenuLink[];
  authUser: NonNullable<Session["user"]>;
  isDropdown?: boolean;
}

const MenuBarItemsNav = memo<MenuBarItemsNavProps>(
  ({ links, authUser, isDropdown = false }) => {
    const path = usePathname();

    const shouldRenderNavLink = useCallback(
      (linkRole: Role) => shouldRenderLink(authUser, linkRole),
      [authUser],
    );

    const Item = useMemo(
      () => (isDropdown ? DropdownMenuItem : MenubarItem),
      [isDropdown],
    );
    const Separator = useMemo(
      () => (isDropdown ? DropdownMenuSeparator : MenubarSeparator),
      [isDropdown],
    );

    return (
      <>
        {links.map(({ text, href, role }, i) => {
          const active = path.toLowerCase() === href.toLowerCase();
          return (
            shouldRenderNavLink(role) && (
              <Fragment key={href}>
                <Item
                  className={cn(
                    "flex items-center justify-center cursor-pointer",
                    active && "bg-accent",
                  )}
                >
                  <Link href={href} className={"text-start w-full"}>
                    {text}
                  </Link>
                </Item>
                {i < links.length - 1 && <Separator />}
              </Fragment>
            )
          );
        })}
      </>
    );
  },
  (prevProps, nextProps) =>
    isDeepEqual(prevProps.authUser, nextProps.authUser) &&
    prevProps.links.every((link, i) => isDeepEqual(link, nextProps.links[i])),
);

MenuBarItemsNav.displayName = "MenuBarItemsNav";
export { MenuBarItemsNav };
