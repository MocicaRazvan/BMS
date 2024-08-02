import { getTranslations, unstable_setRequestLocale } from "next-intl/server";
import { getResetPasswordSchemaTexts } from "@/types/forms";
import { LocaleProps } from "@/navigation";
import ResetPasswordPage from "@/app/[locale]/(main)/auth/reset-password/page-content";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";

export default async function ResetPasswordWrapper({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const [t, resetPasswordSchemaTexts, session] = await Promise.all([
    getTranslations("auth.ResetPasswordPageText"),
    getResetPasswordSchemaTexts(),
    getServerSession(authOptions),
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
      resetPasswordSchemaTexts={resetPasswordSchemaTexts}
      user={session?.user}
    />
  );
}
