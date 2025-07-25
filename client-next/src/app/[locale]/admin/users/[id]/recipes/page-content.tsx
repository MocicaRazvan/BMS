"use client";

import { UserRecipesAdminPageTexts } from "@/app/[locale]/admin/users/[id]/recipes/page";
import { UseListProps } from "@/hoooks/useList";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import LoadingSpinner from "@/components/common/loading-spinner";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import RecipeTable from "@/components/table/recipes-table";
import useGetUser from "@/hoooks/useGetUser";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import { Locale } from "@/navigation/navigation";

interface Props extends UserRecipesAdminPageTexts, UseListProps {
  id: string;
  locale: Locale;
}

export default function UserRecipesAdminPageContent({
  id,
  title,
  themeSwitchTexts,
  recipesTableTexts,
  sortingRecipesSortingOptions,
  header,
  path,
  sortingOptions,
  menuTexts,
  findInSiteTexts,
  locale,
}: Props) {
  const { authUser } = useAuthUserMinRole();

  const { user, messages, error, isFinished } = useGetUser(id);
  const { navigateToNotFound } = useClientNotFound();
  if (isFinished && error?.status) {
    return navigateToNotFound();
  }
  // if (isFinished && error) {
  //   notFound();
  //
  // }
  // const user = messages[0]?.content;

  return (
    <SidebarContentLayout
      navbarProps={{
        title: `${title} ${user?.email || ""}`,
        themeSwitchTexts,
        menuTexts,
        mappingKey: "admin",
        findInSiteTexts,
        locale,
      }}
    >
      <div className="w-full h-full bg-background">
        {!isFinished ? (
          <LoadingSpinner />
        ) : (
          <>
            <Heading
              title={`${title} ${user?.email}`}
              header={`${header} ${user?.email}`}
            />
            <Suspense fallback={<LoadingSpinner />}>
              <div className="mt-10">
                <RecipeTable
                  path={path}
                  forWhom="admin"
                  sortingOptions={sortingOptions}
                  {...recipesTableTexts}
                  sizeOptions={[10, 20, 30, 40]}
                  mainDashboard={true}
                />
              </div>
            </Suspense>
          </>
        )}
      </div>
    </SidebarContentLayout>
  );
}
