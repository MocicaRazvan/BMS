import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import UserPageContent from "@/app/[locale]/(main)/(user)/users/single/[id]/page-content";
import { getUserPageTexts } from "@/texts/pages";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: { locale: Locale; id: string };
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "user.UserProfile",
      "/users/single/" + id,
      locale,
    )),
  };
}

export default async function UserPage({ params: { locale, id } }: Props) {
  unstable_setRequestLocale(locale);

  const [userPageTexts] = await Promise.all([getUserPageTexts()]);

  return (
    <UserPageContent id={id} {...userPageTexts} showDayCalendarCTA={true} />
  );
}
