import { Locale } from "@/navigation";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SingleIngredientPageContent, {
  SingleIngredientPageTexts,
} from "@/app/[locale]/trainer/ingredients/single/[id]/page-content";
import { getUser } from "@/lib/user";
import { getAdminIngredientPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: { locale: Locale; id: string };
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.SingleIngredient",
    "/admin/ingredients/single/" + id,
    locale,
  );
}
export interface AdminIngredientPageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  header: string;
  title: string;
  menuTexts: SidebarMenuTexts;
  singleIngredientPageTexts: SingleIngredientPageTexts;
}

export default async function AdminIngredientPage({
  params: { locale, id },
}: Props) {
  const [authUser, texts] = await Promise.all([
    getUser(),
    getAdminIngredientPageTexts(),
  ]);
  return (
    <SidebarContentLayout
      navbarProps={{
        ...texts,
        authUser,
        mappingKey: "admin",
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
