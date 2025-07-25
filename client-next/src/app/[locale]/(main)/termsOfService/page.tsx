import { unstable_setRequestLocale } from "next-intl/server";
import { getTermsOfServiceTexts, terms } from "@/texts/pages";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { LocaleProps } from "@/navigation/navigation";
import TermsOfServiceContent from "@/app/[locale]/(main)/termsOfService/TermsOfServiceContent";

export interface TermsOfServiceTexts {
  title: string;
  terms: Record<
    (typeof terms)[number],
    {
      title: string;
      body: string;
    }
  >;
}

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return await getIntlMetadata("terms-of-service", "/termsOfService", locale);
}

export default async function TermsOfService({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const texts = await getTermsOfServiceTexts();
  return <TermsOfServiceContent {...texts} />;
}
