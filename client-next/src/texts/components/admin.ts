import {
  AdminMenuTexts,
  groupLabels,
  labels,
  subLabels,
} from "@/components/admin/menu-list";
import { getTranslations } from "next-intl/server";

export async function getAdminMenuTexts(): Promise<AdminMenuTexts> {
  const t = await getTranslations("components.admin.AdminMenuTexts");

  return {
    mainSite: t("mainSite"),
    groupLabels: groupLabels.reduce<AdminMenuTexts["groupLabels"]>(
      (acc, key) => ({ ...acc, [key]: t("groupLabels." + key) }),
      {} as AdminMenuTexts["groupLabels"],
    ),
    labels: labels.reduce<AdminMenuTexts["labels"]>(
      (acc, key) => ({ ...acc, [key]: t("labels." + key) }),
      {} as AdminMenuTexts["labels"],
    ),
    subLabels: subLabels.reduce<AdminMenuTexts["subLabels"]>(
      (acc, key) => ({ ...acc, [key]: t("subLabels." + key) }),
      {} as AdminMenuTexts["subLabels"],
    ),
  };
}

export async function getAdminLayoutTexts() {
  const [menuTexts, t] = await Promise.all([
    getAdminMenuTexts(),
    getTranslations("components.admin.AdminLayout"),
  ]);
  return {
    menuTexts,
    mainSite: t("mainSite"),
  };
}
