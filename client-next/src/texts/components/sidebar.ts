import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getTranslations } from "next-intl/server";

export async function getSidebarMenuTexts(
  key: "admin" | "trainer",
  groupLabels: readonly string[],
  labels: readonly string[],
  subLabels: readonly string[],
): Promise<SidebarMenuTexts> {
  const t = await getTranslations(`components.sidebar.${key}.SidebarMenuTexts`);

  return {
    mainSite: t("mainSite"),
    groupLabels: groupLabels.reduce<SidebarMenuTexts["groupLabels"]>(
      (acc, key) => ({ ...acc, [key]: t("groupLabels." + key) }),
      {} as SidebarMenuTexts["groupLabels"],
    ),
    labels: labels.reduce<SidebarMenuTexts["labels"]>(
      (acc, key) => ({ ...acc, [key]: t("labels." + key) }),
      {} as SidebarMenuTexts["labels"],
    ),
    subLabels: subLabels.reduce<SidebarMenuTexts["subLabels"]>(
      (acc, key) => ({ ...acc, [key]: t("subLabels." + key) }),
      {} as SidebarMenuTexts["subLabels"],
    ),
  };
}

export async function getSidebarLayoutTexts(
  key: "admin" | "trainer",
  groupLabels: readonly string[],
  labels: readonly string[],
  subLabels: readonly string[],
) {
  const [menuTexts, t] = await Promise.all([
    getSidebarMenuTexts(key, groupLabels, labels, subLabels),
    getTranslations(`components.sidebar.${key}.SidebarLayoutTexts`),
  ]);
  return {
    menuTexts,
    mainSite: t("mainSite"),
  };
}
