import { getTranslations, unstable_setRequestLocale } from "next-intl/server";

import { getEmailSchemaTexts } from "@/types/forms";
import { LocaleProps } from "@/navigation";
import ForgotPasswordPage from "@/app/[locale]/(main)/auth/forgot-password/page-content";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "auth.ForgotPassword",
      "/auth/forgot-password",
      locale,
    )),
  };
}
export default async function ForgotPasswordWrapper({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const [t, emailSchemaTexts, session] = await Promise.all([
    getTranslations("auth.ForgotPasswordPageText"),
    getEmailSchemaTexts(),
    getServerSession(authOptions),
  ]);

  return (
    <ForgotPasswordPage
      cardTitle={t("cardTitle")}
      emailLabel={t("emailLabel")}
      submitButton={t("submitButton")}
      loadingButton={t("loadingButton")}
      successMessage={t("successMessage")}
      mxError={t("mxError")}
      emailSchemaTexts={emailSchemaTexts}
      user={session?.user}
    />
  );
}
