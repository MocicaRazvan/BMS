import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import SingleDayTrainerPageContent from "@/app/[locale]/trainer/days/single/[id]/page-content";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SingleDayTexts } from "@/components/days/single-day";
import { getSingleDayTrainerPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: { locale: Locale; id: string };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "trainer.SingleDay",
    "/trainer/days/single/" + id,
    locale,
  );
}

export interface SingleDayTrainerPageTexts {
  singleDayTexts: SingleDayTexts;
  title: string;
  menuTexts: SidebarMenuTexts;
  themeSwitchTexts: ThemeSwitchTexts;
}

export default async function SingleDayTrainerPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);

  const [authUser, texts] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getSingleDayTrainerPageTexts(),
  ]);

  return (
    <SidebarContentLayout
      navbarProps={{
        ...texts,
        authUser,
        mappingKey: "trainer",
      }}
    >
      <div className="w-full bg-background ">
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-5">
            <SingleDayTrainerPageContent
              id={id}
              authUser={authUser}
              {...texts}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
