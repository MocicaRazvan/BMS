"use client";
import { UserPostsAdminPageTexts } from "@/app/[locale]/admin/users/[id]/posts/page";
import { WithUser } from "@/lib/user";
import useFetchStream from "@/hoooks/useFetchStream";
import { CustomEntityModel, UserDto } from "@/types/dto";
import { BaseError } from "@/types/responses";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import PostsTable from "@/components/table/posts-table";
import { UseListProps } from "@/hoooks/useList";
import { notFound } from "next/navigation";
import useGetUser from "@/hoooks/useGetUser";

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
  //
  // if (isFinished && error) {
  //   notFound();
  // }
  // const user = messages[0]?.content;

  return (
    <AdminContentLayout
      navbarProps={{
        title: `${title} ${user?.email || ""}`,
        authUser,
        themeSwitchTexts,
        menuTexts,
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
                />
              </div>
            </Suspense>
          </>
        )}
      </div>
    </AdminContentLayout>
  );
}
