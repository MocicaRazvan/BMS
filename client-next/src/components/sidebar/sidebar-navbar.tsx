import { SheetMenu } from "@/components/sidebar/sheet-menu";
import { ModeToggle } from "@/components/nav/theme-switch";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { LocaleSwitcher } from "@/components/i18n/LocaleSwitcher";
import NotificationPop from "@/components/nav/notification-pop";
import { WithUser } from "@/lib/user";
import {
  MappingListFunctionKeys,
  SidebarMenuTexts,
} from "@/components/sidebar/menu-list";
import FindInSite, {
  FindInSiteTexts,
  MetadataValue,
} from "@/components/nav/find-in-site";

export interface SidebarNavbarProps extends WithUser {
  title: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  mappingKey: MappingListFunctionKeys;
  findInSiteTexts: FindInSiteTexts;
  metadataValues: MetadataValue[];
}

export function SidebarNavbar({
  title,
  themeSwitchTexts,
  authUser,
  menuTexts,
  mappingKey,
  findInSiteTexts,
  metadataValues,
}: SidebarNavbarProps) {
  return (
    <header
      className="sticky top-0 z-[49] w-full bg-background/95 shadow backdrop-blur supports-[backdrop-filter]:bg-background/60 dark:shadow-secondary "
      id="top-item"
    >
      <div className="mx-4 sm:mx-8 flex h-14 items-center">
        <div className="flex items-center space-x-4 lg:space-x-0">
          <SheetMenu
            texts={menuTexts}
            mappingKey={mappingKey}
            authUser={authUser}
          />
          <h1 className="font-bold">{title}</h1>
        </div>
        <div className="flex flex-1 items-center space-x-4 justify-end">
          <FindInSite texts={findInSiteTexts} metadataValues={metadataValues} />
          <NotificationPop authUser={authUser} />
          <LocaleSwitcher />
          <ModeToggle {...themeSwitchTexts} />
        </div>
      </div>
    </header>
  );
}
