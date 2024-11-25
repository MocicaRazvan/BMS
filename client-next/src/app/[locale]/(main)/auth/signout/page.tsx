import { LocaleProps, redirect } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import SignOut from "@/app/[locale]/(main)/auth/signout/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { getSignOutPageTexts } from "@/texts/pages";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("auth.SignOut", "/auth/signout", locale)),
  };
}
export default async function SignOutWrapper({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const [texts, session] = await Promise.all([
    getSignOutPageTexts(),
    getServerSession(authOptions),
  ]);

  if (!session || !session.user) {
    redirect("/auth/signin");
  }

  return (
    <SignOut
      {...texts}
      locale={locale}
      // buttonSignIn={t("buttonSignIn")}
      // buttonSignOut={t("buttonSignOut")}
      // questionText={t("questionText")}
    />
  );
}
