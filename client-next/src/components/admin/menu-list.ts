import {
  Tag,
  Users,
  Settings,
  Bookmark,
  SquarePen,
  LayoutGrid,
  HandPlatter,
  CookingPot,
  NotebookPen,
  ShoppingBag,
  Calendar,
  MapIcon,
  CalendarDays,
  MailPlus,
  Kanban,
} from "lucide-react";

type Submenu = {
  href: string;
  label: string;
  active: boolean;
};

type Menu = {
  href: string;
  label: string;
  active: boolean;
  icon: any;
  submenus: Submenu[];
};

type Group = {
  groupLabel: string;
  menus: Menu[];
};

export const groupLabels = [
  "contents",
  "sales",
  "settings",
  "company",
] as const;
export const labels = [
  "dashboard",
  "posts",
  "recipes",
  "plans",
  "ingredients",
  "users",
  "orders",
  "monthlySales",
  "dailySales",
  "countries",
  "account",
  "email",
  "kanban",
] as const;

export const subLabels = ["allIngredients", "createIngredient"] as const;

export type AdminMenuTexts = {
  groupLabels: Record<(typeof groupLabels)[number], string>;
  labels: Record<(typeof labels)[number], string>;
  subLabels: Record<(typeof subLabels)[number], string>;
  mainSite: string;
};

export function getMenuList(
  pathname: string,
  { groupLabels, labels, subLabels }: AdminMenuTexts,
): Group[] {
  return [
    {
      groupLabel: "",
      menus: [
        {
          href: "/admin/dashboard",
          label: labels["dashboard"],
          active: pathname.includes("/dashboard"),
          icon: LayoutGrid,
          submenus: [],
        },
      ],
    },
    {
      groupLabel: groupLabels["contents"],
      menus: [
        {
          href: "/admin/posts",
          label: labels["posts"],
          active: pathname.includes("/posts"),
          icon: SquarePen,
          submenus: [],
        },
        {
          href: "/admin/recipes",
          label: labels["recipes"],
          active: pathname.includes("/recipes"),
          icon: CookingPot,
          submenus: [],
        },
        {
          href: "/admin/plans",
          label: labels["plans"],
          active: pathname.includes("/plans"),
          icon: NotebookPen,
          submenus: [],
        },
        {
          href: "",
          label: labels["ingredients"],
          active: pathname.includes("/ingredients"),
          icon: HandPlatter,
          submenus: [
            {
              href: "/admin/ingredients",
              label: subLabels["allIngredients"],
              active: pathname === "/admin/ingredients",
            },
            {
              href: "/admin/ingredients/create",
              label: subLabels["createIngredient"],
              active: pathname === "/admin/ingredients/create",
            },
          ],
        },
        {
          href: "/admin/users",
          label: labels["users"],
          active: pathname.includes("/users"),
          icon: Users,
          submenus: [],
        },
        {
          href: "/admin/orders",
          label: labels["orders"],
          active: pathname.includes("/orders"),
          icon: ShoppingBag,
          submenus: [],
        },
      ],
    },
    {
      groupLabel: groupLabels["sales"],
      menus: [
        {
          href: "/admin/monthlySales",
          label: labels["monthlySales"],
          active: pathname.includes("/monthlySales"),
          icon: Calendar,
          submenus: [],
        },
        {
          href: "/admin/dailySales",
          label: labels["dailySales"],
          active: pathname.includes("/dailySales"),
          icon: CalendarDays,
          submenus: [],
        },
        {
          href: "/admin/countries",
          label: labels["countries"],
          active: pathname.includes("/countries"),
          icon: MapIcon,
          submenus: [],
        },
      ],
    },

    {
      groupLabel: groupLabels["company"],
      menus: [
        {
          href: "/admin/email",
          label: labels["email"],
          active: pathname.includes("/email"),
          icon: MailPlus,
          submenus: [],
        },
        {
          href: "/admin/kanban",
          label: labels["kanban"],
          active: pathname.includes("/kanban"),
          icon: Kanban,
          submenus: [],
        },
      ],
    },
    {
      groupLabel: groupLabels["settings"],
      menus: [
        {
          href: "/account",
          label: labels["account"],
          active: pathname.includes("/account"),
          icon: Settings,
          submenus: [],
        },
      ],
    },
  ];
}
