import { AdminNavbar, AdminNavbarProps } from "@/components/admin/admin-navbar";

interface ContentLayoutProps {
  children: React.ReactNode;
  navbarProps: AdminNavbarProps;
}

export default function AdminContentLayout({
  navbarProps,
  children,
}: ContentLayoutProps) {
  return (
    <div>
      <AdminNavbar {...navbarProps} />
      <div className="container max-w-[1550px] pt-8 pb-8 px-4 sm:px-8 w-full">
        {children}
      </div>
    </div>
  );
}
