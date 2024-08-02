import { getTranslations, unstable_setRequestLocale } from "next-intl/server";
import { LocaleProps } from "@/navigation";
import ConfirmEmailPage from "@/app/[locale]/(main)/auth/confirm-email/page-content";

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
