import { TermsOfServiceTexts } from "@/app/[locale]/(main)/termsOfService/page";

export default function TermsOfServiceContent({
  terms,
  title,
}: TermsOfServiceTexts) {
  return (
    <section className="w-full flex flex-col items-center justify-center transition-all px-1 md:px-6 py-10 gap-5 mx-auto max-w-7xl mt-1.5">
      <h1 className="text-center text-xl md:text-3xl lg:text-5xl font-bold tracking-tighter text-balance mb-2 md:mb-3 lg:mb-4">
        {title}
      </h1>
      {Object.entries(terms).map(([key, { title, body }]) => (
        <div key={key} className=" mx-auto">
          <p className="text-lg md:text-xl font-bold tracking-tighter text-balance text-start ps-2 md:ps-4">
            {title}
          </p>
          <p className="prose max-w-none px-4 md:px-12 py-6 text-foreground md:text-lg">
            {body}
          </p>
        </div>
      ))}
    </section>
  );
}
