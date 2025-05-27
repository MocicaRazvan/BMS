"use client";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import { useLocale } from "next-intl";
import { useClientLRUStore } from "@/lib/client-lru-store";
import FindInSite, { FindInSiteTexts } from "@/components/nav/find-in-site";
import { Locale } from "@/navigation";
import { getMetadataAction } from "@/actions/texts/get-metadata-action";

export default function SidebarFindInSite({
  texts,
}: {
  texts: FindInSiteTexts;
}) {
  const { authUser } = useAuthUserMinRole();
  const locale = useLocale();
  const metadataValues = useClientLRUStore({
    setter: () => getMetadataAction(authUser, locale as Locale),
    args: [`user.SidebarFindInSite-${locale}-${authUser.role}`, authUser.role],
  });
  if (!metadataValues) {
    return null;
  }
  return <FindInSite texts={texts} metadataValues={metadataValues} />;
}
