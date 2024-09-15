import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getTermsOfServiceTexts, terms } from "@/texts/pages";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

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

interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("terms-of-service", "/terms-of-service", locale);
}

export default async function TermsOfService({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const { title, terms } = await getTermsOfServiceTexts();
  return (
    <section className="w-full flex flex-col items-center justify-center transition-all px-1 md:px-6 py-10 gap-5">
      <h1 className="text-center text-xl md:text-3xl lg:text-4xl font-bold tracking-tighter text-balance mb-2">
        {title}
      </h1>
      {Object.entries(terms).map(([key, { title, body }]) => (
        <div key={key} className=" mx-auto">
          <p className="text-lg md:text-xl font-bold tracking-tighter text-balance text-start ps-2 md:ps-4">
            {title}
          </p>
          <p className="prose max-w-none px-4 md:px-12  py-6 text-foreground md:text-lg">
            {body}
          </p>
        </div>
      ))}
    </section>
  );
}
