import OrderCompletePageContent from "@/app/[locale]/(main)/(user)/orderComplete/page-content";
import { Link, Locale } from "@/navigation";
import { Button } from "@/components/ui/button";
import { unstable_setRequestLocale } from "next-intl/server";
import { getOrderCompletePageTexts } from "@/texts/pages";

export interface OrderCompletePageTexts {
  title: string;
  ordersBtn: string;
  plansBtn: string;
}
interface Props {
  params: { locale: Locale };
}

export default async function OrderCompletePage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const { ordersBtn, plansBtn, title } = await getOrderCompletePageTexts();
  return (
    <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 max-w-[1350px] mx-auto relative">
      <h1 className="text-2xl md:text-4xl tracking-tighter font-bold text-success mt-10 text-center">
        {title}
      </h1>
      <div className="flex items-center justify-center flex-wrap w-full gap-10">
        <Button asChild>
          <Link href={"/orders"}>{ordersBtn}</Link>
        </Button>
        <Button asChild>
          <Link href={"/plans/approved"}>{plansBtn}</Link>
        </Button>
      </div>
      <OrderCompletePageContent />
    </div>
  );
}
