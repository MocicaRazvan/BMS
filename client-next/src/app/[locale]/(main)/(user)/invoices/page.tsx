import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUser } from "@/lib/user";
import InvoicePageContent from "@/app/[locale]/(main)/(user)/invoices/page-content";

interface Props {
  params: { locale: Locale };
}

export default async function InvoicePage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [user] = await Promise.all([getUser()]);
  return <InvoicePageContent authUser={user} />;
}
