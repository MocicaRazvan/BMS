import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import { getPostFormTexts } from "@/texts/components/forms";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import UpdatePostPageContent from "@/app/[locale]/(main)/trainer/posts/update/[id]/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

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

export default async function UpdatePostPage({
  params: { locale, id },
}: Props) {
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
    getPostFormTexts("update"),
    getUserWithMinRole("ROLE_TRAINER"),
  ]);
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <UpdatePostPageContent
        authUser={user}
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
  );
}
