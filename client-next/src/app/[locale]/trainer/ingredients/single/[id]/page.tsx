import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import SingleIngredientPageContent, {
  SingleIngredientPageTexts,
} from "@/app/[locale]/trainer/ingredients/single/[id]/page-content";
import {
  getSingleIngredientPageTexts,
  getTrainerSingleIngredientPageTexts,
} from "@/texts/pages";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";

interface Props {
  params: { id: string; locale: Locale };
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.SingleIngredient",
      "/trainer/single/" + id,
      locale,
    )),
  };
}
export interface TrainerSingleIngredientPageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  menuTexts: SidebarMenuTexts;
  singleIngredientPageTexts: SingleIngredientPageTexts;
}
export default async function SingleIngredientPage({
  params: { id, locale },
}: Props) {
  unstable_setRequestLocale(locale);
  const [authUser, texts] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getTrainerSingleIngredientPageTexts(),
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
            <SingleIngredientPageContent
              authUser={authUser}
              {...texts.singleIngredientPageTexts}
              id={id}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
