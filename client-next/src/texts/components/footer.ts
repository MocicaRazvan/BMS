import { FooterTexts } from "@/components/footer/footer";
import { getTranslations } from "next-intl/server";

export async function getFooterTexts(): Promise<FooterTexts> {
  const t = await getTranslations("components.footer.FooterTexts");

  return {
    disclaimer: t("disclaimer"),
    home: t("home"),
    privacyPolicy: t("privacyPolicy"),
    title: t("title"),
    plans: t("plans"),
    posts: t("posts"),
    signIn: t("signIn"),
    signUp: t("signUp"),
    rightsReserved: t("rightsReserved"),
    termsOfService: t("termsOfService"),
  };
}
