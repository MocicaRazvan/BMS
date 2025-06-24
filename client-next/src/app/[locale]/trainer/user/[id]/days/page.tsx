import { Locale } from "@/navigation/navigation";
import DaysTable, { DayTableTexts } from "@/components/table/day-table";
import { getSortingOptions, SortingOptionsTexts } from "@/types/constants";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserDaysPageTexts } from "@/texts/pages";
import { sortingDaysSortingOptionsKeys } from "@/texts/components/list";
import Heading from "@/components/common/heading";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import IsTheSameUserOrAdmin from "@/app/[locale]/trainer/user/is-the-same-user-or-admin";
import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: { locale: Locale; id: string };
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.TrainerDays",
      "/trainer/user/" + id + "/days",
      locale,
    )),
  };
}
export interface UserDaysPageTexts {
  dayTableTexts: DayTableTexts;
  sortingDaysSortingOptions: SortingOptionsTexts;
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}

export default async function UserDaysPage({ params: { locale, id } }: Props) {
  unstable_setRequestLocale(locale);

  const [userDaysPageTexts] = await Promise.all([getUserDaysPageTexts()]);

  const daysOptions = getSortingOptions(
    sortingDaysSortingOptionsKeys,
    userDaysPageTexts.sortingDaysSortingOptions,
  );

  return (
    <IsTheSameUserOrAdmin id={id}>
      <SidebarContentLayout
        navbarProps={{
          title: userDaysPageTexts.title,
          themeSwitchTexts: userDaysPageTexts.themeSwitchTexts,
          menuTexts: userDaysPageTexts.menuTexts,
          mappingKey: "trainer",
          findInSiteTexts: userDaysPageTexts.findInSiteTexts,
          locale,
        }}
      >
        <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 mx-auto ">
          <Heading {...userDaysPageTexts} />
          <div>
            <DaysTable
              path={`/days/trainer/filteredWithCount/${id}`}
              forWhom={"trainer"}
              {...userDaysPageTexts.dayTableTexts}
              sortingOptions={daysOptions}
              sizeOptions={[10, 20, 30, 40]}
            />
          </div>
        </div>
      </SidebarContentLayout>
    </IsTheSameUserOrAdmin>
  );
}
