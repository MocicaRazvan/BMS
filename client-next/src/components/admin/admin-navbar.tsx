import { SheetMenu } from "@/components/admin/sheet-menu";
import { ModeToggle } from "@/components/nav/theme-switch";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { LocaleSwitcher } from "@/components/i18n/LocaleSwitcher";
import NotificationPop from "@/components/nav/notification-pop";
import { WithUser } from "@/lib/user";
import { AdminMenuTexts } from "@/components/admin/menu-list";

export interface AdminNavbarProps extends WithUser {
  title: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: AdminMenuTexts;
}

export function AdminNavbar({
  title,
  themeSwitchTexts,
  authUser,
  menuTexts,
}: AdminNavbarProps) {
  return (
    <header className="sticky top-0 z-10 w-full bg-background/95 shadow backdrop-blur supports-[backdrop-filter]:bg-background/60 dark:shadow-secondary">
      <div className="mx-4 sm:mx-8 flex h-14 items-center">
        <div className="flex items-center space-x-4 lg:space-x-0">
          <SheetMenu texts={menuTexts} />
          <h1 className="font-bold">{title}</h1>
        </div>
        <div className="flex flex-1 items-center space-x-2 justify-end">
          <NotificationPop authUser={authUser} />
          <LocaleSwitcher />
          <ModeToggle {...themeSwitchTexts} />
        </div>
      </div>
    </header>
  );
}
