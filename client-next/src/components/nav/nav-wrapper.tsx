import { getCartPopsTexts, getNavTexts } from "@/texts/components/nav";
import Nav from "@/components/nav/nav";
import { Locale } from "@/navigation/navigation";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import { getMetadataValues } from "@/texts/metadata";

export default async function NavWrapper({ locale }: { locale: Locale }) {
  const [navTexts, cartPopTexts, metadataValues] = await Promise.all([
    getNavTexts(),
    getCartPopsTexts(),
    getServerSession(authOptions).then((session) =>
      getMetadataValues(session?.user, locale),
    ),
  ]);
  return (
    <Nav
      {...navTexts}
      cartPopTexts={cartPopTexts}
      metadataValues={metadataValues}
      locale={locale}
    />
  );
}
