"use client";

import { AdminUserPageTexts } from "@/app/[locale]/admin/users/[id]/page";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import UserPageContent from "@/app/[locale]/(main)/(user)/users/single/[id]/page-content";
import useGetUser from "@/hoooks/useGetUser";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import { Locale } from "@/navigation/navigation";

interface Props extends AdminUserPageTexts {
  id: string;
  locale: Locale;
}
export default function AdminUserPageContent({ id, locale, ...texts }: Props) {
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
        locale,
      }}
    >
      <div className="w-full bg-background ">
        <Heading {...texts} title={`${texts.title} ${user?.email || ""}`} />
        <div className="mt-12">
          <UserPageContent id={id} {...texts.userPageTexts} />
        </div>
      </div>
    </SidebarContentLayout>
  );
}
