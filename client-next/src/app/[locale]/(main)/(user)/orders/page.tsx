import { OrderTableTexts } from "@/components/table/orders-table";
import { getSortingOptions, SortingOptionsTexts } from "@/types/constants";
import { LocaleProps } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserOrdersPageTexts } from "@/texts/pages";
import { sortingOrdersSortingOptionsKeys } from "@/texts/components/list";
import Heading from "@/components/common/heading";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import UserOrdersPageContent from "@/app/[locale]/(main)/(user)/orders/page-content";

export interface UserOrdersPageTexts {
  orderTableTexts: OrderTableTexts;
  sortingOrdersSortingOptions: SortingOptionsTexts;
  title: string;
  header: string;
}

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.Orders", "/orders", locale)),
  };
}

export default async function UserOrdersPage({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getUserOrdersPageTexts()]);
  const ordersOptions = getSortingOptions(
    sortingOrdersSortingOptionsKeys,
    texts.sortingOrdersSortingOptions,
  );
  return (
    <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 max-w-[1350px] mx-auto ">
      <Heading {...texts} />
      <div>
        <UserOrdersPageContent
          {...texts.orderTableTexts}
          sortingOptions={ordersOptions}
          sizeOptions={[10, 20, 30, 40]}
          forWhom={"user"}
        />
      </div>
    </div>
  );
}
