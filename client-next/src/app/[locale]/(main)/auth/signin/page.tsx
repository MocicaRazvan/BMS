import { getTranslations, unstable_setRequestLocale } from "next-intl/server";
import { getSignInSchemaTexts } from "@/types/forms";
import SingIn from "./page-content";
import { LocaleProps } from "@/navigation";
import { getIntlMetadata } from "@/texts/metadata";
import { Metadata } from "next";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("auth.SignIn", "/auth/signin", locale)),
  };
}

export default async function SignInPageWrapper({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const [t, signInSchemaTexts] = await Promise.all([
    getTranslations("auth.SignInPageText"),
    getSignInSchemaTexts(),
  ]);
  return (
    <SingIn
      cardTitle={t("cardTitle")}
      emailLabel={t("emailLabel")}
      passwordLabel={t("passwordLabel")}
      submitButton={t("submitButton")}
      loadingButton={t("loadingButton")}
      errorMessages={t("errorMessages")}
      linkSignUp={t("linkSignUp")}
      linkForgotPassword={t("linkForgotPassword")}
      signInSchemaTexts={signInSchemaTexts}
      locale={locale}
    />
  );
}
