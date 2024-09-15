import {
  SidebarNavbar,
  SidebarNavbarProps,
} from "@/components/sidebar/sidebar-navbar";

interface ContentLayoutProps {
  children: React.ReactNode;
  navbarProps: SidebarNavbarProps;
}

export default function SidebarContentLayout({
  navbarProps,
  children,
}: ContentLayoutProps) {
  return (
    <div>
      <SidebarNavbar {...navbarProps} />
      <div className="container max-w-[1550px] pt-3 md:pt-8 pb-3 md:pb-8 px-1 sm:px-4 md:px-8 w-full">
        {children}
      </div>
    </div>
  );
}
