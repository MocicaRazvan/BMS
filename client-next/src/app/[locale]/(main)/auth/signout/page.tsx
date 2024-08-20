import { LocaleProps } from "@/navigation";
import { getTranslations, unstable_setRequestLocale } from "next-intl/server";
import SignOut from "@/app/[locale]/(main)/auth/signout/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "auth.SignOutPassword",
      "/auth/signout-password",
      locale,
    )),
  };
}
export default async function SignOutWrapper({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const t = await getTranslations("auth.SignOutPageText");
  return (
    <SignOut
      buttonSignIn={t("buttonSignIn")}
      buttonSignOut={t("buttonSignOut")}
      questionText={t("questionText")}
    />
  );
}
