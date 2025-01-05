import { Locale } from "@/navigation";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SingleTrainerPlanPageContent, {
  SingleTrainerPlanPageTexts,
} from "@/app/[locale]/trainer/plans/single/[id]/page-content";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUser } from "@/lib/user";
import { getAdminPlanPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import ScrollProgress from "@/components/common/scroll-progress";

interface Props {
  params: { locale: Locale; id: string };
}

export interface AdminPlanPageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  header: string;
  title: string;
  menuTexts: SidebarMenuTexts;
  singleTrainerPlanPageTexts: SingleTrainerPlanPageTexts;
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.SinglePlan",
    "/admin/plans/single/" + id,
    locale,
  );
}

export default async function AdminPlanPage({ params: { locale, id } }: Props) {
  unstable_setRequestLocale(locale);

  const [authUser, texts] = await Promise.all([
    getUser(),
    getAdminPlanPageTexts(),
  ]);

  return (
    <SidebarContentLayout
      navbarProps={{
        ...texts,
        authUser,
        mappingKey: "admin",
      }}
    >
      <ScrollProgress />
      <div className="w-full bg-background ">
        <Suspense fallback={<LoadingSpinner />}>
          <div>
            <SingleTrainerPlanPageContent
              authUser={authUser}
              {...texts.singleTrainerPlanPageTexts}
              id={id}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
