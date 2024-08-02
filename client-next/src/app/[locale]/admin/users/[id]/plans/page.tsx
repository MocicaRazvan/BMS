import { PlanTableTexts } from "@/components/table/plans-table";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserPlansAdminPageTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import { sortingPlansSortingOptionsKeys } from "@/texts/components/list";
import { notFound } from "next/navigation";
import UserPlansAdminPageContent from "@/app/[locale]/admin/users/[id]/plans/page-content";
import { AdminMenuTexts } from "@/components/admin/menu-list";

export interface UserPlansAdminPageTexts {
  plansTableTexts: PlanTableTexts;
  sortingPlansSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: AdminMenuTexts;
}
interface Props {
  params: { locale: Locale; id: string };
}

export default async function UserPlansAdminPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts, authUser] = await Promise.all([
    getUserPlansAdminPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);

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
      authUser={authUser}
      sortingOptions={plansOptions}
      {...texts}
      path={`/plans/trainer/filteredWithCount/${id}`}
    />
  );
}
