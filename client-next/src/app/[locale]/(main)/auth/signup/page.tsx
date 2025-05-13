import { getTranslations, unstable_setRequestLocale } from "next-intl/server";
import { getRegistrationSchemaTexts } from "@/types/forms";
import { LocaleProps } from "@/navigation";
import SignUp from "@/app/[locale]/(main)/auth/signup/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import {
  getEmailFromFieldTexts,
  getPasswordStrengthIndicatorTexts,
} from "@/texts/components/forms";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("auth.SignUp", "/auth/signup", locale)),
  };
}
export default async function SignUpPageWrapper({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const [
    t,
    registrationSchemaTexts,
    passwordStrengthTexts,
    emailFromFieldTexts,
  ] = await Promise.all([
    getTranslations("auth.SignUpPageText"),
    getRegistrationSchemaTexts(),
    getPasswordStrengthIndicatorTexts(),
    getEmailFromFieldTexts(),
  ]);
  return (
    <SignUp
      cardTitle={t("cardTitle")}
      passwordLabel={t("passwordLabel")}
      confirmPasswordLabel={t("confirmPasswordLabel")}
      submitButton={t("submitButton")}
      loadingButton={t("loadingButton")}
      emailExistsError={t("emailExistsError")}
      linkSignIn={t("linkSignIn")}
      firstNameLabel={t("firstNameLabel")}
      lastNameLabel={t("lastNameLabel")}
      mxError={t("mxError")}
      registrationSchemaTexts={registrationSchemaTexts}
      passwordStrengthTexts={passwordStrengthTexts}
      emailFromFieldTexts={emailFromFieldTexts}
    />
  );
}
