"use client";

import { Ellipsis, LogOut } from "lucide-react";

import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { Link, usePathname } from "@/navigation/navigation";
import {
  mappingFunctions,
  MappingListFunctionKeys,
  SidebarMenuTexts,
} from "@/components/sidebar/menu-list";
import { CollapseMenuButton } from "@/components/sidebar/collpase-menu-button";
import { WithUser } from "@/lib/user";

interface MenuProps extends WithUser {
  isOpen: boolean;
  texts: SidebarMenuTexts;
  mappingKey: MappingListFunctionKeys;
}

export function Menu({ isOpen, texts, mappingKey, authUser }: MenuProps) {
  const pathname = usePathname();
  const menuList = mappingFunctions[mappingKey](authUser, pathname, texts);

  return (
    <ScrollArea className="[&>div>div[style]]:!block ">
      <nav className="pt-8 h-full w-full">
        <ul className="flex flex-col min-h-[calc(100vh-48px-36px-16px-32px-32px)] lg:min-h-[calc(100vh-32px-40px-32px-32px)] items-start space-y-1 px-2">
          {menuList.map(({ groupLabel, menus }, index) => (
            <li className={cn("w-full", groupLabel ? "pt-5" : "")} key={index}>
              {(isOpen && groupLabel) || isOpen === undefined ? (
                <p className="text-sm font-medium text-muted-foreground px-4 pb-2 max-w-[248px] truncate">
                  {groupLabel}
                </p>
              ) : !isOpen && groupLabel ? (
                <TooltipProvider>
                  <Tooltip delayDuration={100}>
                    <TooltipTrigger className="w-full">
                      <div className="w-full flex justify-center items-center">
                        <Ellipsis className="h-5 w-5" />
                      </div>
                    </TooltipTrigger>
                    <TooltipContent side="right">
                      <p>{groupLabel}</p>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              ) : (
                <p className="pb-2"></p>
              )}
              {menus.map(
                ({ href, label, icon: Icon, active, submenus }, index) =>
                  submenus.length === 0 ? (
                    <div className="w-full" key={index}>
                      <TooltipProvider disableHoverableContent>
                        <Tooltip delayDuration={100}>
                          <TooltipTrigger asChild>
                            <Button
                              variant={active ? "secondary" : "ghost"}
                              className="w-full justify-start h-10 mb-1"
                              asChild
                            >
                              <Link href={href}>
                                <span className={cn(!isOpen ? "" : "mr-4")}>
                                  <Icon size={18} />
                                </span>
                                <p
                                  className={cn(
                                    "max-w-[200px] truncate",
                                    !isOpen
                                      ? "-translate-x-96 opacity-0"
                                      : "translate-x-0 opacity-100",
                                  )}
                                >
                                  {label}
                                </p>
                              </Link>
                            </Button>
                          </TooltipTrigger>
                          {!isOpen && (
                            <TooltipContent side="right">
                              {label}
                            </TooltipContent>
                          )}
                        </Tooltip>
                      </TooltipProvider>
                    </div>
                  ) : (
                    <div className="w-full" key={index}>
                      <CollapseMenuButton
                        icon={Icon}
                        label={label}
                        active={active}
                        submenus={submenus}
                      />
                    </div>
                  ),
              )}
            </li>
          ))}
          <li className="w-full grow flex items-start pb-1">
            <TooltipProvider disableHoverableContent>
              <Tooltip delayDuration={100}>
                <TooltipTrigger asChild>
                  <Button
                    variant="outline"
                    className="w-5/6 justify-center h-10 mt-5"
                    asChild
                  >
                    <Link href={`/${mappingKey}/account/signout`}>
                      <span className={cn(!isOpen ? "" : "mr-4")}>
                        <LogOut size={18} />
                      </span>
                      {/*<p*/}
                      {/*  className={cn(*/}
                      {/*    "whitespace-nowrap",*/}
                      {/*    !isOpen ? "opacity-0 hidden" : "opacity-100",*/}
                      {/*  )}*/}
                      {/*>*/}
                      {/*  Sign out*/}
                      {/*</p>*/}
                    </Link>
                  </Button>
                </TooltipTrigger>
                {!isOpen && (
                  <TooltipContent side="right">{"Sign out"}</TooltipContent>
                )}
              </Tooltip>
            </TooltipProvider>
          </li>
        </ul>
      </nav>
    </ScrollArea>
  );
}
