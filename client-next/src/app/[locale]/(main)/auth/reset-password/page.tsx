import { getTranslations, unstable_setRequestLocale } from "next-intl/server";
import { getResetPasswordSchemaTexts } from "@/types/forms";
import { LocaleProps } from "@/navigation";
import ResetPasswordPage from "@/app/[locale]/(main)/auth/reset-password/page-content";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { getPasswordStrengthIndicatorTexts } from "@/texts/components/forms";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "auth.ResetPassword",
      "/auth/reset-password",
      locale,
    )),
  };
}
export default async function ResetPasswordWrapper({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const [t, resetPasswordSchemaTexts, session, passwordStrengthTexts] =
    await Promise.all([
      getTranslations("auth.ResetPasswordPageText"),
      getResetPasswordSchemaTexts(),
      getServerSession(authOptions),
      getPasswordStrengthIndicatorTexts(),
    ]);
  return (
    <ResetPasswordPage
      cardTitle={t("cardTitle")}
      passwordLabel={t("passwordLabel")}
      confirmPasswordLabel={t("confirmPasswordLabel")}
      submitButton={t("submitButton")}
      loadingButton={t("loadingButton")}
      errorMessages={t("errorMessages")}
      emailLabel={t("emailLabel")}
      passwordStrengthTexts={passwordStrengthTexts}
      resetPasswordSchemaTexts={resetPasswordSchemaTexts}
      user={session?.user}
    />
  );
}
