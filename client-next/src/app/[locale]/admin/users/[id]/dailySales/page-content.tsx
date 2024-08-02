"use client";

import { UserAdminDailySalesPageTexts } from "@/app/[locale]/admin/users/[id]/dailySales/page";
import { WithUser } from "@/lib/user";
import useGetUser from "@/hoooks/useGetUser";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import LoadingSpinner from "@/components/common/loading-spinner";
import Heading from "@/components/common/heading";
import DailySales from "@/components/charts/daily-sales";

interface Props extends UserAdminDailySalesPageTexts, WithUser {
  id: string;
}

export default function UserAdminDailySalesPageContent({
  id,
  dailySalesTexts,
  menuTexts,
  themeSwitchTexts,
  authUser,
  header,
  title,
}: Props) {
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
            <div className="mt-10 h-full">
              <DailySales
                path={`/orders/trainer/countAndAmount/daily/${id}`}
                {...dailySalesTexts}
                authUser={authUser}
              />
            </div>
          </>
        )}
      </div>
    </AdminContentLayout>
  );
}
