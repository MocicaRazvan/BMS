import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getPostFormTexts } from "@/texts/components/forms";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import PostForm from "@/components/forms/post-form";
import { getUserWithMinRole } from "@/lib/user";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

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

export default async function CreatePostPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [
    {
      titleBodyTexts,
      postSchemaTexts,
      fieldTexts,
      inputMultipleSelectorTexts,
      buttonSubmitTexts,
      baseFormTexts,
    },
    user,
  ] = await Promise.all([
    getPostFormTexts("create"),
    getUserWithMinRole("ROLE_TRAINER"),
  ]);

  return (
    <main className="flex items-center justify-center px-6 py-10">
      <Suspense fallback={<LoadingSpinner />}>
        <PostForm
          postSchemaTexts={postSchemaTexts}
          fieldTexts={fieldTexts}
          titleBodyTexts={titleBodyTexts}
          inputMultipleSelectorTexts={inputMultipleSelectorTexts}
          buttonSubmitTexts={buttonSubmitTexts}
          {...baseFormTexts}
          authUser={user}
          path={"/posts/createWithImages"}
        />
      </Suspense>
    </main>
  );
}
