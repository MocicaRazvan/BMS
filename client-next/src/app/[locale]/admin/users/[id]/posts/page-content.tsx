"use client";
import { UserPostsAdminPageTexts } from "@/app/[locale]/admin/users/[id]/posts/page";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import PostsTable from "@/components/table/posts-table";
import { UseListProps } from "@/hoooks/useList";
import useGetUser from "@/hoooks/useGetUser";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import { Locale } from "@/navigation/navigation";

interface Props extends UserPostsAdminPageTexts, UseListProps {
  id: string;
  locale: Locale;
}

export default function UserPostsAdminPageContent({
  id,
  title,
  themeSwitchTexts,
  postTableTexts,
  sortingPostsSortingOptions,
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
  //
  // if (isFinished && error) {
  //   notFound();
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
                <PostsTable
                  path={path}
                  forWhom="admin"
                  {...postTableTexts}
                  sortingOptions={sortingOptions}
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
