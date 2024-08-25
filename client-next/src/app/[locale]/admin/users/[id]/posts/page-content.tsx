"use client";
import { UserPostsAdminPageTexts } from "@/app/[locale]/admin/users/[id]/posts/page";
import { WithUser } from "@/lib/user";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import PostsTable from "@/components/table/posts-table";
import { UseListProps } from "@/hoooks/useList";
import useGetUser from "@/hoooks/useGetUser";
import useClientNotFound from "@/hoooks/useClientNotFound";

interface Props extends UserPostsAdminPageTexts, WithUser, UseListProps {
  id: string;
}

export default function UserPostsAdminPageContent({
  id,
  title,
  themeSwitchTexts,
  postTableTexts,
  sortingPostsSortingOptions,
  header,
  authUser,
  path,
  sortingOptions,
  menuTexts,
}: Props) {
  // const { messages, error, refetch, isFinished } = useFetchStream<
  //   CustomEntityModel<UserDto>,
  //   BaseError
  // >({
  //   path: `/users/${id}`,
  //   method: "GET",
  //   authToken: true,
  //   useAbortController: false,
  // });

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
        authUser,
        themeSwitchTexts,
        menuTexts,
        mappingKey: "admin",
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
                  authUser={authUser}
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
