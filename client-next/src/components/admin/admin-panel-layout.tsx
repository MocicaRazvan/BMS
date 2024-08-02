"use client";

import { cn } from "@/lib/utils";
import { useSidebarToggle } from "@/context/sidebar-toggle";
import SideBar from "@/components/admin/sidebar";
import { AdminFooter } from "@/components/admin/admin-footer";
import { AdminMenuTexts } from "@/components/admin/menu-list";

export default function AdminPanelLayout({
  children,
  menuTexts,
  mainSite,
}: {
  children: React.ReactNode;
  menuTexts: AdminMenuTexts;
  mainSite: string;
}) {
  const { toggleIsOpen, isOpen } = useSidebarToggle();

  return (
    <>
      <SideBar menuTexts={menuTexts} mainSite={mainSite} />
      <main
        className={cn(
          "min-h-[calc(100vh_-_56px)]  transition-[margin-left] ease-in-out duration-300 overflow-x-hidden",
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
      {/*  <AdminFooter />*/}
      {/*</footer>*/}
    </>
  );
}
