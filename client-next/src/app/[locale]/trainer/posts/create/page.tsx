import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getPostFormTexts } from "@/texts/components/forms";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import PostForm from "@/components/forms/post-form";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getCreatePostPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

interface Props {
  params: { locale: Locale };
}

export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.CreatePost",
      "/trainer/posts/create",
      locale,
    )),
  };
}

export interface CreatePostPageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  postFormTexts: Awaited<ReturnType<typeof getPostFormTexts>>;
  findInSiteTexts: FindInSiteTexts;
}

export default async function CreatePostPage({ params: { locale } }: Props) {
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
        loadedImages,
        bodyAIGeneratedPopTexts,
        titleAIGeneratedPopTexts,
        aiCheckBoxes,
        diffusionImagesFormTexts,
      },
      ...rest
    },
  ] = await Promise.all([getCreatePostPageTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: baseFormTexts.header,
        ...rest,
        mappingKey: "trainer",
      }}
    >
      <main className="flex items-center justify-center px-6 py-10">
        <Suspense fallback={<LoadingSpinner />}>
          <PostForm
            postSchemaTexts={postSchemaTexts}
            fieldTexts={fieldTexts}
            titleBodyTexts={titleBodyTexts}
            inputMultipleSelectorTexts={inputMultipleSelectorTexts}
            buttonSubmitTexts={buttonSubmitTexts}
            loadedImages={loadedImages}
            {...baseFormTexts}
            path={"/posts/createWithImages"}
            titleAIGeneratedPopTexts={titleAIGeneratedPopTexts}
            bodyAIGeneratedPopTexts={bodyAIGeneratedPopTexts}
            aiCheckBoxes={aiCheckBoxes}
            diffusionImagesFormTexts={diffusionImagesFormTexts}
          />
        </Suspense>
      </main>
    </SidebarContentLayout>
  );
}
