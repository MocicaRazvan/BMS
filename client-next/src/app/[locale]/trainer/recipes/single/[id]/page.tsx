import { getUserWithMinRole } from "@/lib/user";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import SingeRecipePageContent, {
  SingleRecipePageTexts,
} from "@/app/[locale]/trainer/recipes/single/[id]/page-content";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getTrainerSingleRecipePageTexts } from "@/texts/pages";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";

interface Props {
  params: { locale: Locale; id: string };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.SingleRecipe",
      "/trainer/recipes/single/" + id,
      locale,
    )),
  };
}

export interface TrainerSingleRecipePageTexts {
  singleRecipePageTexts: SingleRecipePageTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  title: string;
}

export default async function SingleRecipePage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);

  const [authUser, texts] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getTrainerSingleRecipePageTexts(),
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
          <SingeRecipePageContent
            authUser={authUser}
            id={id}
            {...texts.singleRecipePageTexts}
          />
        </Suspense>
      </div>{" "}
    </SidebarContentLayout>
  );
}
