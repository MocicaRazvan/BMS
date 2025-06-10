import { getTranslations, unstable_setRequestLocale } from "next-intl/server";
import { LocaleProps } from "@/navigation/navigation";
import ConfirmEmailPage from "@/app/[locale]/(main)/auth/confirm-email/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "auth.ConfirmEmail",
      "/auth/confirm-email",
      locale,
    )),
  };
}
export default async function ConfirmEmailPageWrapper({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const t = await getTranslations("auth.ConfirmEmailPageText");
  return (
    <ConfirmEmailPage
      isLoadingHeader={t("isLoadingHeader")}
      isFinishedErrorHeader={t("isFinishedErrorHeader")}
    />
  );
}
