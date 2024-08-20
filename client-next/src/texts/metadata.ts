import { getTranslations } from "next-intl/server";
import { Locale, locales } from "@/navigation";
const languages = locales.reduce<Record<Locale, string>>(
  (acc, l) => ({ ...acc, [l]: `/${l}` }),
  {} as Record<Locale, string>,
);

export interface IntlMetadata {
  title: string;
  description: string;
  keywords?: string[];
  alternates?: {
    canonical: string;
    languages: Record<Locale, string>;
  };
}

function getKeywords(initial: string) {
  return initial.split(", ").map((k) => k.trim());
}

export async function getIntlMetadata(
  key: string,
  path?: string,
  locale?: Locale,
): Promise<IntlMetadata> {
  const t = await getTranslations(`metadata.${key}`);
  let canonical = path;
  if (locale) {
    canonical = `/${locale}${path}`;
  }

  return {
    title: t("title"),
    description: t("description"),
    keywords: getKeywords(t("keywords")),
    alternates: canonical
      ? {
          canonical,
          languages,
        }
      : undefined,
  };
}
