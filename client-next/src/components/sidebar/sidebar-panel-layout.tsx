"use client";

import { cn } from "@/lib/utils";
import { useSidebarToggle } from "@/context/sidebar-toggle";
import SideBar from "@/components/sidebar/sidebar";
import {
  MappingListFunctionKeys,
  SidebarMenuTexts,
} from "@/components/sidebar/menu-list";
import { ReactNode } from "react";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

export default function SidebarPanelLayout({
  children,
  menuTexts,
  mainSite,
  mappingKey,
}: {
  children: ReactNode;
  menuTexts: SidebarMenuTexts;
  mainSite: string;
  mappingKey: MappingListFunctionKeys;
}) {
  const { authUser } = useAuthUserMinRole();
  const { toggleIsOpen, isOpen } = useSidebarToggle();

  return (
    <>
      <SideBar
        menuTexts={menuTexts}
        mainSite={mainSite}
        mappingKey={mappingKey}
        authUser={authUser}
      />
      <main
        className={cn(
          "min-h-[calc(100vh_-_56px)] transition-[margin-left] ease-in-out duration-300 overflow-x-visible",
          !isOpen ? "lg:ml-[90px]" : "lg:ml-72",
        )}
      >
        {children}
      </main>
      {/*<footer*/}
      {/*  className={cn(*/}
      {/*    "transition-[margin-left] ease-in-out duration-300",*/}
      {/*    !isOpen ? "lg:ml-[90px]" : "lg:ml-72",*/}
      {/*  )}*/}
      {/*>*/}
      {/*  <SidebarFooter />*/}
      {/*</footer>*/}
    </>
  );
}
