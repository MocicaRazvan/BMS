"use client";

import { UserRecipesAdminPageTexts } from "@/app/[locale]/admin/users/[id]/recipes/page";
import { WithUser } from "@/lib/user";
import { UseListProps } from "@/hoooks/useList";
import useFetchStream from "@/hoooks/useFetchStream";
import { CustomEntityModel, UserDto } from "@/types/dto";
import { BaseError } from "@/types/responses";
import { notFound } from "next/navigation";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import LoadingSpinner from "@/components/common/loading-spinner";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import RecipeTable from "@/components/table/recipes-table";
import useGetUser from "@/hoooks/useGetUser";
import useClientNotFound from "@/hoooks/useClientNotFound";

interface Props extends UserRecipesAdminPageTexts, WithUser, UseListProps {
  id: string;
}

export default function UserRecipesAdminPageContent({
  id,
  title,
  themeSwitchTexts,
  recipesTableTexts,
  sortingRecipesSortingOptions,
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
  // if (isFinished && error) {
  //   notFound();
  //
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
                <RecipeTable
                  path={path}
                  forWhom="admin"
                  sortingOptions={sortingOptions}
                  {...recipesTableTexts}
                  authUser={authUser}
                  sizeOptions={[10, 20, 30, 40]}
                />
              </div>
            </Suspense>
          </>
        )}
      </div>
    </AdminContentLayout>
  );
}
