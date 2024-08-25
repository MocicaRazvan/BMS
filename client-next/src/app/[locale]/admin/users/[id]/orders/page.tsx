import { Locale } from "@/navigation";
import { OrderTableTexts } from "@/components/table/orders-table";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserOrdersAdminPageTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import { sortingOrdersSortingOptionsKeys } from "@/texts/components/list";
import UserOrdersAdminPageContent from "@/app/[locale]/admin/users/[id]/orders/page-content";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

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
  const [texts, authUser] = await Promise.all([
    getUserOrdersAdminPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);

  const ordersOptions = getSortingOptions(
    sortingOrdersSortingOptionsKeys,
    texts.sortingOrdersSortingOptions,
  );

  return (
    <UserOrdersAdminPageContent
      id={id}
      authUser={authUser}
      sortingOptions={ordersOptions}
      {...texts}
      path={`/orders/filtered/${id}`}
    />
  );
}
