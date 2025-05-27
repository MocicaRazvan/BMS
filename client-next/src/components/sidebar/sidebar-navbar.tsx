import { SheetMenu } from "@/components/sidebar/sheet-menu";
import { ModeToggle } from "@/components/nav/theme-switch";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { LocaleSwitcher } from "@/components/i18n/LocaleSwitcher";
import {
  MappingListFunctionKeys,
  SidebarMenuTexts,
} from "@/components/sidebar/menu-list";
import { FindInSiteTexts } from "@/components/nav/find-in-site";
import SidebarNotificationPop from "@/components/sidebar/sidebar-notification-pop";
import SidebarFindInSite from "@/components/sidebar/sidebar-find-in-site";

export interface SidebarNavbarProps {
  title: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  mappingKey: MappingListFunctionKeys;
  findInSiteTexts: FindInSiteTexts;
}

export function SidebarNavbar({
  title,
  themeSwitchTexts,
  menuTexts,
  mappingKey,
  findInSiteTexts,
}: SidebarNavbarProps) {
  return (
    <header
      className="sticky top-0 z-[49] w-full bg-background/95 shadow backdrop-blur supports-[backdrop-filter]:bg-background/60 dark:shadow-secondary "
      id="top-item"
    >
      <div className="mx-4 sm:mx-8 flex h-14 items-center">
        <div className="flex items-center space-x-4 lg:space-x-0">
          <SheetMenu texts={menuTexts} mappingKey={mappingKey} />
          <h1 className="font-bold">{title}</h1>
        </div>
        <div className="flex flex-1 items-center space-x-4 justify-end">
          <SidebarFindInSite texts={findInSiteTexts} />
          <SidebarNotificationPop />
          <LocaleSwitcher />
          <ModeToggle {...themeSwitchTexts} />
        </div>
      </div>
    </header>
  );
}
