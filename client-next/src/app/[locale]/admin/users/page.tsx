import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import UsersTable, { UserTableTexts } from "@/components/table/users-table";
import { getAdminUsersPageTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { sortingUsersSortingOptionsKeys } from "@/texts/components/list";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import ArchiveQueueCards, {
  ArchiveQueueCardsTexts,
} from "@/components/common/archive-queue-card";
import TopUsers, { TopUsersTexts } from "@/components/charts/top-users";
import { Separator } from "@/components/ui/separator";

interface Props {
  params: { locale: Locale };
}

export interface AdminUsersPageTexts {
  userTableTexts: UserTableTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  userSortingOptionsTexts: SortingOptionsTexts;
  header: string;
  title: string;
  menuTexts: SidebarMenuTexts;
  archiveUsersTexts: ArchiveQueueCardsTexts;
  topUsersTexts: TopUsersTexts;
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("admin.Users", "/admin/users", locale);
}

export default async function AdminUsersPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [
    {
      userTableTexts,
      themeSwitchTexts,
      userSortingOptionsTexts,
      title,
      header,
      menuTexts,
      archiveUsersTexts,
      topUsersTexts,
    },
    authUser,
  ] = await Promise.all([
    getAdminUsersPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);

  const userOptions = getSortingOptions(
    sortingUsersSortingOptionsKeys,
    userSortingOptionsTexts,
  );

  return (
    <SidebarContentLayout
      navbarProps={{
        title,
        themeSwitchTexts,
        authUser,
        menuTexts,
        mappingKey: "admin",
      }}
    >
      <div className="w-full bg-background">
        <Heading title={title} header={header} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full space-y-10">
            <UsersTable
              path={"/users"}
              forWhom={"admin"}
              sortingOptions={userOptions}
              authUser={authUser}
              {...userTableTexts}
              sizeOptions={[10, 20, 30, 40]}
              extraQueryParams={{
                admin: "true",
              }}
            />

            <Separator />
            <div className="my-5 h-full w-full">
              <TopUsers texts={topUsersTexts} locale={locale} />
            </div>
            <Separator />
            <ArchiveQueueCards
              prefix={"user"}
              locale={locale}
              showHeader={true}
              {...archiveUsersTexts}
              authUser={authUser}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
