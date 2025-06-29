import { unstable_setRequestLocale } from "next-intl/server";
import { getDisclaimerTexts } from "@/texts/pages";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { LocaleProps } from "@/navigation/navigation";
import TermsOfServiceContent from "@/app/[locale]/(main)/termsOfService/TermsOfServiceContent";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return await getIntlMetadata("disclaimer", "/disclaimer", locale);
}

export default async function TermsOfService({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const texts = await getDisclaimerTexts();
  return <TermsOfServiceContent {...texts} />;
}
