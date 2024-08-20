import MonthlySales, {
  MonthlySalesTexts,
} from "@/components/charts/monthly-sales";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserMonthlySalesPageTexts } from "@/texts/pages";
import { getTheSameUserOrAdmin } from "@/lib/user";
import Heading from "@/components/common/heading";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

export interface UserMonthlySalesPageTexts {
  monthlySalesTexts: MonthlySalesTexts;
  title: string;
  header: string;
}
interface Props {
  params: { locale: Locale; id: string };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.MonthlySales",
      "/trainer/user/" + id + "/plans/monthlySales",
      locale,
    )),
  };
}

export default async function UsersMonthlySalesPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts, authUser] = await Promise.all([
    getUserMonthlySalesPageTexts(),
    getTheSameUserOrAdmin(id),
  ]);
  return (
    <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 max-w-[1500px] mx-auto ">
      <Heading {...texts} />
      <div>
        <MonthlySales
          path={`/orders/trainer/countAndAmount/${id}`}
          {...texts.monthlySalesTexts}
          authUser={authUser}
        />
      </div>
    </div>
  );
}
