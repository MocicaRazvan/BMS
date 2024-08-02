"use client";

import { UserPlansPageTexts } from "@/app/[locale]/(main)/trainer/user/[id]/plans/page";
import { WithUser } from "@/lib/user";
import { UseListProps } from "@/hoooks/useList";
import { UserPlansAdminPageTexts } from "@/app/[locale]/admin/users/[id]/plans/page";
import useGetUser from "@/hoooks/useGetUser";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import LoadingSpinner from "@/components/common/loading-spinner";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import PlansTable from "@/components/table/plans-table";
import { useSidebarToggle } from "@/context/sidebar-toggle";

interface Props extends UserPlansAdminPageTexts, WithUser, UseListProps {
  id: string;
}

export default function UserPlansAdminPageContent({
  id,
  title,
  themeSwitchTexts,
  plansTableTexts,
  header,
  authUser,
  path,
  sortingOptions,
  menuTexts,
}: Props) {
  const { isOpen } = useSidebarToggle();
  const { user, messages, error, isFinished } = useGetUser(id);

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
              <div className="mt-10 h-full">
                <PlansTable
                  path={path}
                  forWhom={"admin"}
                  sortingOptions={sortingOptions}
                  {...plansTableTexts}
                  authUser={authUser}
                  sizeOptions={[10, 20, 30, 40]}
                  isSidebarOpen={isOpen}
                />
              </div>
            </Suspense>
          </>
        )}
      </div>
    </AdminContentLayout>
  );
}
