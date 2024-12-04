import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import DuplicatePostPageContent from "@/app/[locale]/trainer/posts/duplicate/[id]/page-content";
import { getUserWithMinRole } from "@/lib/user";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getPostFormTexts } from "@/texts/components/forms";
import { getUserDuplicatePostPage } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: {
    locale: Locale;
    id: string;
  };
}
export interface UserDuplicatePostPage {
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  postFormTexts: Awaited<ReturnType<typeof getPostFormTexts>>;
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.DuplicatePost",
      "/trainer/posts/duplicate/" + id,
      locale,
    )),
  };
}
export default async function UserDuplicatePostPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [
    authUser,
    {
      postFormTexts: {
        titleBodyTexts,
        postSchemaTexts,
        fieldTexts,
        inputMultipleSelectorTexts,
        buttonSubmitTexts,
        baseFormTexts,
        loadedImages,
        bodyAIGeneratedPopTexts,
        titleAIGeneratedPopTexts,
        aiCheckBoxes,
      },
      ...rest
    },
  ] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getUserDuplicatePostPage(),
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
          <DuplicatePostPageContent
            authUser={authUser}
            postId={id}
            postSchemaTexts={postSchemaTexts}
            fieldTexts={fieldTexts}
            titleBodyTexts={titleBodyTexts}
            inputMultipleSelectorTexts={inputMultipleSelectorTexts}
            buttonSubmitTexts={buttonSubmitTexts}
            {...baseFormTexts}
            path={`/posts/createWithImages`}
            loadedImages={loadedImages}
            aiCheckBoxes={aiCheckBoxes}
            bodyAIGeneratedPopTexts={bodyAIGeneratedPopTexts}
            titleAIGeneratedPopTexts={titleAIGeneratedPopTexts}
          />
        </Suspense>
      </main>
    </SidebarContentLayout>
  );
}
