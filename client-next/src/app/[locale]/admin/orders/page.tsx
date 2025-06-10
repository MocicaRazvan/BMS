import { Locale } from "@/navigation/navigation";
import OrdersTable, { OrderTableTexts } from "@/components/table/orders-table";
import { getSortingOptions, SortingOptionsTexts } from "@/types/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { unstable_setRequestLocale } from "next-intl/server";
import { getAdminOrdersPageTexts } from "@/texts/pages";
import { sortingOrdersSortingOptionsKeys } from "@/texts/components/list";
import Heading from "@/components/common/heading";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("admin.Orders", "/admin/orders", locale);
}

export interface AdminOrdersPageTexts {
  orderTableTexts: OrderTableTexts;
  sortingOrdersSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}

export default async function AdminOrdersPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);

  const [
    {
      title,
      themeSwitchTexts,
      orderTableTexts,
      sortingOrdersSortingOptions,
      header,
      menuTexts,
      findInSiteTexts,
    },
  ] = await Promise.all([getAdminOrdersPageTexts()]);

  const orderOptions = getSortingOptions(
    sortingOrdersSortingOptionsKeys,
    sortingOrdersSortingOptions,
  );

  return (
    <SidebarContentLayout
      navbarProps={{
        title,
        themeSwitchTexts,
        menuTexts,
        mappingKey: "admin",
        findInSiteTexts,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading title={title} header={header} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full">
            <OrdersTable
              path={`/orders/admin/filtered`}
              forWhom="admin"
              sortingOptions={orderOptions}
              mainDashboard={true}
              {...orderTableTexts}
              sizeOptions={[10, 20, 30, 40]}
              extraQueryParams={{
                admin: "true",
              }}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
