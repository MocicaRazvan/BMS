"use client";

import { AdminUserPageTexts } from "@/app/[locale]/admin/users/[id]/page";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import UserPageContent from "@/app/[locale]/(main)/(user)/users/single/[id]/page-content";
import useGetUser from "@/hoooks/useGetUser";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

interface Props extends AdminUserPageTexts {
  id: string;
}
export default function AdminUserPageContent({ id, ...texts }: Props) {
  const { authUser } = useAuthUserMinRole();

  const { user, messages, error, isFinished } = useGetUser(id);
  const { navigateToNotFound } = useClientNotFound();
  if (isFinished && error?.status) {
    return navigateToNotFound();
  }
  return (
    <SidebarContentLayout
      navbarProps={{
        ...texts,
        title: `${texts.title} ${user?.email || ""}`,
        mappingKey: "admin",
      }}
    >
      <div className="w-full bg-background ">
        <Heading {...texts} title={`${texts.title} ${user?.email || ""}`} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-12">
            <UserPageContent id={id} {...texts.userPageTexts} />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
