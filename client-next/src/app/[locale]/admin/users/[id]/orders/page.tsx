import { Locale } from "@/navigation/navigation";
import { OrderTableTexts } from "@/components/table/orders-table";
import { getSortingOptions, SortingOptionsTexts } from "@/types/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserOrdersAdminPageTexts } from "@/texts/pages";
import { sortingOrdersSortingOptionsKeys } from "@/texts/components/list";
import UserOrdersAdminPageContent from "@/app/[locale]/admin/users/[id]/orders/page-content";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: { locale: Locale; id: string };
}
export interface UserOrdersAdminPageTexts {
  orderTableTexts: OrderTableTexts;
  sortingOrdersSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.UserOrders",
    "/admin/users/" + id + "/orders",
    locale,
  );
}

export default async function UserOrdersAdminPage({
  params: { id, locale },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getUserOrdersAdminPageTexts()]);

  const ordersOptions = getSortingOptions(
    sortingOrdersSortingOptionsKeys,
    texts.sortingOrdersSortingOptions,
  );

  return (
    <UserOrdersAdminPageContent
      id={id}
      sortingOptions={ordersOptions}
      {...texts}
      path={`/orders/filtered/${id}`}
    />
  );
}
