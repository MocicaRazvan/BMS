import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import UsersTable, { UserTableTexts } from "@/components/table/users-table";
import { getAdminUsersPageTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { sortingUsersSortingOptionsKeys } from "@/texts/components/list";
import { AdminMenuTexts } from "@/components/admin/menu-list";

interface Props {
  params: { locale: Locale };
}

export interface AdminUsersPageTexts {
  userTableTexts: UserTableTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  userSortingOptionsTexts: SortingOptionsTexts;
  header: string;
  title: string;
  menuTexts: AdminMenuTexts;
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
    <AdminContentLayout
      navbarProps={{
        title,
        themeSwitchTexts,
        authUser,
        menuTexts,
      }}
    >
      <div className="w-full bg-background">
        <Heading title={title} header={header} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10">
            <UsersTable
              path={"/users"}
              forWhom={"admin"}
              sortingOptions={userOptions}
              authUser={authUser}
              {...userTableTexts}
              sizeOptions={[10, 20, 30, 40]}
            />
          </div>
        </Suspense>
      </div>
    </AdminContentLayout>
  );
}
