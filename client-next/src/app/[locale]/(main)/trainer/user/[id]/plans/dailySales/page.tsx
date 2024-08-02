import DailySales, { DailySalesTexts } from "@/components/charts/daily-sales";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserDailySalesPageTexts } from "@/texts/pages";
import { getTheSameUserOrAdmin } from "@/lib/user";
import Heading from "@/components/common/heading";

export interface UserDailySalesPageTexts {
  dailySalesTexts: DailySalesTexts;
  title: string;
  header: string;
}
interface Props {
  params: { locale: Locale; id: string };
}

export default async function UsersDailySalesPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts, authUser] = await Promise.all([
    getUserDailySalesPageTexts(),
    getTheSameUserOrAdmin(id),
  ]);
  return (
    <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 max-w-[1500px] mx-auto ">
      <Heading {...texts} />
      <div>
        <DailySales
          path={`/orders/trainer/countAndAmount/daily/${id}`}
          {...texts.dailySalesTexts}
          authUser={authUser}
        />
      </div>
    </div>
  );
}
