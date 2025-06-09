import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getPostFormTexts } from "@/texts/components/forms";
import UpdatePostPageContent from "@/app/[locale]/trainer/posts/update/[id]/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getUpdatePostPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

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
  findInSiteTexts: FindInSiteTexts;
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
        loadedImages,
        aiCheckBoxes,
        bodyAIGeneratedPopTexts,
        titleAIGeneratedPopTexts,
        diffusionImagesFormTexts,
      },
      ...rest
    },
  ] = await Promise.all([getUpdatePostPageTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: baseFormTexts.header,
        ...rest,
        mappingKey: "trainer",
      }}
    >
      <main className="flex items-center justify-center px-6 py-10">
        <UpdatePostPageContent
          postId={id}
          postSchemaTexts={postSchemaTexts}
          fieldTexts={fieldTexts}
          titleBodyTexts={titleBodyTexts}
          inputMultipleSelectorTexts={inputMultipleSelectorTexts}
          buttonSubmitTexts={buttonSubmitTexts}
          {...baseFormTexts}
          path={`/posts/updateWithImages/${id}`}
          loadedImages={loadedImages}
          aiCheckBoxes={aiCheckBoxes}
          bodyAIGeneratedPopTexts={bodyAIGeneratedPopTexts}
          titleAIGeneratedPopTexts={titleAIGeneratedPopTexts}
          diffusionImagesFormTexts={diffusionImagesFormTexts}
        />
      </main>
    </SidebarContentLayout>
  );
}
