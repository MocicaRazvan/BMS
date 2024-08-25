import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import { getPostFormTexts } from "@/texts/components/forms";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import UpdatePostPageContent from "@/app/[locale]/trainer/posts/update/[id]/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getUpdatePostPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";

interface Props {
  params: {
    locale: Locale;
    id: string;
  };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.UpdatePost",
      "/trainer/posts/update/" + id,
      locale,
    )),
  };
}
export interface UpdatePostPageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  postFormTexts: Awaited<ReturnType<typeof getPostFormTexts>>;
}
export default async function UpdatePostPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [
    {
      postFormTexts: {
        titleBodyTexts,
        postSchemaTexts,
        fieldTexts,
        inputMultipleSelectorTexts,
        buttonSubmitTexts,
        baseFormTexts,
      },
      ...rest
    },
    authUser,
  ] = await Promise.all([
    getUpdatePostPageTexts(),
    getUserWithMinRole("ROLE_TRAINER"),
  ]);
  return (
    <SidebarContentLayout
      navbarProps={{
        title: baseFormTexts.header,
        ...rest,
        authUser,
        mappingKey: "trainer",
      }}
    >
      <main className="flex items-center justify-center px-6 py-10">
        <Suspense fallback={<LoadingSpinner />}>
          <UpdatePostPageContent
            authUser={authUser}
            postId={id}
            postSchemaTexts={postSchemaTexts}
            fieldTexts={fieldTexts}
            titleBodyTexts={titleBodyTexts}
            inputMultipleSelectorTexts={inputMultipleSelectorTexts}
            buttonSubmitTexts={buttonSubmitTexts}
            {...baseFormTexts}
            path={`/posts/updateWithImages/${id}`}
          />
        </Suspense>
      </main>
    </SidebarContentLayout>
  );
}
