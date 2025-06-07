import { PlanTableTexts } from "@/components/table/plans-table";
import { getSortingOptions, SortingOptionsTexts } from "@/types/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserPlansAdminPageTexts } from "@/texts/pages";
import { sortingPlansSortingOptionsKeys } from "@/texts/components/list";
import { notFound } from "next/navigation";
import UserPlansAdminPageContent from "@/app/[locale]/admin/users/[id]/plans/page-content";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { TopPlansTexts } from "@/components/charts/top-plans";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

export interface UserPlansAdminPageTexts {
  plansTableTexts: PlanTableTexts;
  sortingPlansSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
  topPlansTexts: TopPlansTexts;
  findInSiteTexts: FindInSiteTexts;
}
interface Props {
  params: { locale: Locale; id: string };
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.UserPlans",
    "/admin/users/" + id + "/plans",
    locale,
  );
}

export default async function UserPlansAdminPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getUserPlansAdminPageTexts()]);

  const plansOptions = getSortingOptions(
    sortingPlansSortingOptionsKeys,
    texts.sortingPlansSortingOptions,
  );

  if (!id) {
    notFound();
  }

  return (
    <UserPlansAdminPageContent
      id={id}
      sortingOptions={plansOptions}
      locale={locale}
      {...texts}
      path={`/plans/trainer/filteredWithCount/${id}`}
    />
  );
}
