import TestPageContent from "@/app/[locale]/(main)/test/page-content";
import { Locale } from "@/navigation";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import { getMetadataValues } from "@/texts/metadata";
import { getFindInSiteTexts } from "@/texts/components/nav";
import { getAdminDashboardPageTexts } from "@/texts/pages";
import { getUser } from "@/lib/user";
import { getEditorTexts } from "@/texts/components/editor";

interface Props {
  params: {
    locale: Locale;
  };
}

export default async function TestPage({ params }: Props) {
  const session = await getServerSession(authOptions);
  const metadataValues = await getMetadataValues(session?.user, params.locale);
  const texts = await getAdminDashboardPageTexts();
  const authUser = await getUser();
  const editorTexts = await getEditorTexts();
  return (
    <TestPageContent
      metadataValues={metadataValues}
      texts={texts}
      authUser={authUser}
      editorTexts={editorTexts}
    />
  );
}
