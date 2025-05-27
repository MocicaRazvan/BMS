import { Locale } from "@/navigation";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SingleOrderPageContent, {
  SingleOrderPageContentTexts,
} from "@/app/[locale]/(main)/(user)/orders/single/[id]/page-content";
import { unstable_setRequestLocale } from "next-intl/server";
import { getAdminOrderPageTexts } from "@/texts/pages";
import LoadingSpinner from "@/components/common/loading-spinner";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

interface Props {
  params: { locale: Locale; id: string };
}

export interface AdminOrderPageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  header: string;
  title: string;
  menuTexts: SidebarMenuTexts;
  singleOrderPageContentTexts: SingleOrderPageContentTexts;
  findInSiteTexts: FindInSiteTexts;
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.SingleOrder",
    "/admin/orders/single/" + id,
    locale,
  );
}

export default async function AdminOrderPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getAdminOrderPageTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        ...texts,
        mappingKey: "admin",
      }}
    >
      <div className="w-full bg-background ">
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-5">
            <SingleOrderPageContent
              id={id}
              {...texts.singleOrderPageContentTexts}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
