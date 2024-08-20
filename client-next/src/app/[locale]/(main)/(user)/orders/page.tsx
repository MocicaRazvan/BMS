import OrdersTable, { OrderTableTexts } from "@/components/table/orders-table";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserOrdersPageTexts } from "@/texts/pages";
import { getUser } from "@/lib/user";
import { sortingOrdersSortingOptionsKeys } from "@/texts/components/list";
import Heading from "@/components/common/heading";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

export interface UserOrdersPageTexts {
  orderTableTexts: OrderTableTexts;
  sortingOrdersSortingOptions: SortingOptionsTexts;
  title: string;
  header: string;
}

interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.Orders", "/orders", locale)),
  };
}

export default async function UserOrdersPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [texts, user] = await Promise.all([
    getUserOrdersPageTexts(),
    getUser(),
  ]);
  const ordersOptions = getSortingOptions(
    sortingOrdersSortingOptionsKeys,
    texts.sortingOrdersSortingOptions,
  );
  return (
    <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 max-w-[1350px] mx-auto ">
      <Heading {...texts} />
      <div>
        <OrdersTable
          path={`/orders/filtered/${user.id}`}
          {...texts.orderTableTexts}
          sortingOptions={ordersOptions}
          authUser={user}
          sizeOptions={[10, 20, 30, 40]}
          forWhom={"user"}
        />
      </div>
    </div>
  );
}
