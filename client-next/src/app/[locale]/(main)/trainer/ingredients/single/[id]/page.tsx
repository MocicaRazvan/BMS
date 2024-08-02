import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import SingleIngredientPageContent from "@/app/[locale]/(main)/trainer/ingredients/single/[id]/page-content";
import { getSingleIngredientPageTexts } from "@/texts/pages";

interface Props {
  params: { id: string; locale: Locale };
}

export default async function SingleIngredientPage({
  params: { id, locale },
}: Props) {
  unstable_setRequestLocale(locale);
  const [authUser, texts] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getSingleIngredientPageTexts(),
  ]);

  return (
    <>
      <SingleIngredientPageContent authUser={authUser} id={id} {...texts} />
    </>
  );
}
