import { getTranslations, unstable_setRequestLocale } from "next-intl/server";
import { getRegistrationSchemaTexts } from "@/types/forms";
import { LocaleProps } from "@/navigation";
import SignUp from "@/app/[locale]/(main)/auth/signup/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
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
  const [t, registrationSchemaTexts] = await Promise.all([
    getTranslations("auth.SignUpPageText"),
    getRegistrationSchemaTexts(),
  ]);
  return (
    <SignUp
      cardTitle={t("cardTitle")}
      emailLabel={t("emailLabel")}
      passwordLabel={t("passwordLabel")}
      confirmPasswordLabel={t("confirmPasswordLabel")}
      submitButton={t("submitButton")}
      loadingButton={t("loadingButton")}
      emailExistsError={t("emailExistsError")}
      linkSignIn={t("linkSignIn")}
      firstNameLabel={t("firstNameLabel")}
      lastNameLabel={t("lastNameLabel")}
      registrationSchemaTexts={registrationSchemaTexts}
    />
  );
}
