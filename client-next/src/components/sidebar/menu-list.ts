import {
  Calendar,
  CalendarCog,
  CalendarDays,
  CookingPot,
  HandPlatter,
  Kanban,
  LayoutGrid,
  MailPlus,
  MapIcon,
  NotebookPen,
  Settings,
  ShoppingBag,
  SquarePen,
  Users,
} from "lucide-react";
import { Session } from "next-auth";

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

export const adminGroupLabels = [
  "contents",
  "sales",
  "settings",
  "company",
] as const;

export const trainerGroupLabels = ["contents", "sales"] as const;

export const adminLabels = [
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

export const trainerLabels = [
  "dashboard",
  "posts",
  "recipes",
  "plans",
  "days",
  "ingredients",
  "orders",
  "monthlySales",
  "dailySales",
  "kanban",
] as const;

export const adminSubLabels = ["allIngredients", "createIngredient"] as const;

export const trainerSubLabels = [
  "yourPosts",
  "createPost",
  "yourRecipes",
  "createRecipe",
  "yourPlans",
  "createPlan",
  "yourDays",
  "createDay",
] as const;

export type SidebarMenuTexts = {
  groupLabels: Record<string, string>;
  labels: Record<string, string>;
  subLabels: Record<string, string>;
  mainSite: string;
};
export type AdminSidebarMenuTexts = {
  groupLabels: Record<(typeof adminGroupLabels)[number], string>;
  labels: Record<(typeof adminLabels)[number], string>;
  subLabels: Record<(typeof adminSubLabels)[number], string>;
  mainSite: string;
};

export type TrainerSidebarMenuTexts = {
  groupLabels: Record<(typeof trainerGroupLabels)[number], string>;
  labels: Record<(typeof trainerLabels)[number], string>;
  subLabels: Record<(typeof trainerSubLabels)[number], string>;
  mainSite: string;
};

export type getMenuListType<T extends SidebarMenuTexts = SidebarMenuTexts> = (
  pathname: string,
  texts: T,
) => Group[];
export const getAdminMenuList: getMenuListType<AdminSidebarMenuTexts> = (
  pathname,
  { groupLabels, labels, subLabels },
) => {
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
          active: pathname.includes("/posts") && !pathname.includes("/users"),
          icon: SquarePen,
          submenus: [],
        },
        {
          href: "/admin/recipes",
          label: labels["recipes"],
          active: pathname.includes("/recipes") && !pathname.includes("/users"),
          icon: CookingPot,
          submenus: [],
        },
        {
          href: "/admin/plans",
          label: labels["plans"],
          active: pathname.includes("/plans") && !pathname.includes("/users"),
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
          active: pathname.includes("/orders") && !pathname.includes("/users"),
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
};

export const getTrainerMenuList: (
  authUser: NonNullable<Session["user"]>,
) => getMenuListType<TrainerSidebarMenuTexts> =
  (authUser) =>
  (pathname, { groupLabels, labels, subLabels }) => [
    // {
    //   groupLabel: "",
    //   menus: [
    //     {
    //       href: "/trainer/dashboard",
    //       label: labels["dashboard"],
    //       active: pathname.includes("/dashboard"),
    //       icon: LayoutGrid,
    //       submenus: [],
    //     },
    //   ],
    // },
    {
      groupLabel: groupLabels["contents"],
      menus: [
        {
          href: "",
          label: labels["posts"],
          active: pathname.includes("/posts"),
          icon: SquarePen,
          submenus: [
            {
              href: `/trainer/user/${authUser.id}/posts`,
              label: subLabels["yourPosts"],
              active: pathname === `/trainer/user/${authUser.id}/posts`,
            },
            {
              href: "/trainer/posts/create",
              label: subLabels["createPost"],
              active: pathname === "/trainer/posts/create",
            },
          ],
        },
        {
          href: "/trainer/ingredients",
          label: labels["ingredients"],
          active: pathname.includes("/ingredients"),
          icon: HandPlatter,
          submenus: [],
        },
        {
          href: "",
          label: labels["recipes"],
          active: pathname.includes("/recipes"),
          icon: CookingPot,
          submenus: [
            {
              href: `/trainer/user/${authUser.id}/recipes`,
              label: subLabels["yourRecipes"],
              active: pathname === `/trainer/user/${authUser.id}/recipes`,
            },
            {
              href: "/trainer/recipes/create",
              label: subLabels["createRecipe"],
              active: pathname === "/trainer/recipes/create",
            },
          ],
        },
        {
          href: "",
          label: labels["days"],
          active: pathname.includes("/days"),
          icon: CalendarCog,
          submenus: [
            {
              href: `/trainer/user/${authUser.id}/days`,
              label: subLabels["yourDays"],
              active: pathname === `/trainer/user/${authUser.id}/days`,
            },
            {
              href: "/trainer/days/create",
              label: subLabels["createDay"],
              active: pathname === "/trainer/days/create",
            },
          ],
        },
        // {
        //   href: "/trainer/orders",
        //   label: labels["orders"],
        //   active: pathname.includes("/orders"),
        //   icon: ShoppingBag,
        //   submenus: [],
        // },
        {
          href: "",
          label: labels["plans"],
          active:
            pathname.includes("/plans") &&
            !pathname.includes("/dailySales") &&
            !pathname.includes("/monthlySales"),
          icon: NotebookPen,
          submenus: [
            {
              href: `/trainer/user/${authUser.id}/plans`,
              label: subLabels["yourPlans"],
              active: pathname === `/trainer/user/${authUser.id}/plans`,
            },
            {
              href: "/trainer/plans/create",
              label: subLabels["createPlan"],
              active: pathname === "/trainer/plans/create",
            },
          ],
        },
        {
          href: "/trainer/kanban",
          label: labels["kanban"],
          active: pathname.includes("/kanban"),
          icon: Kanban,
          submenus: [],
        },
      ],
    },
    {
      groupLabel: groupLabels["sales"],
      menus: [
        {
          href: `/trainer/user/${authUser.id}/plans/monthlySales`,
          label: labels["monthlySales"],
          active: pathname.includes("/monthlySales"),
          icon: Calendar,
          submenus: [],
        },
        {
          href: `/trainer/user/${authUser.id}/plans/dailySales`,
          label: labels["dailySales"],
          active: pathname.includes("/dailySales"),
          icon: CalendarDays,
          submenus: [],
        },
      ],
    },
  ];

export type MappingListFunctionKeys = "admin" | "trainer";

export const mappingFunctions: Record<
  MappingListFunctionKeys,
  (
    authUser: NonNullable<Session["user"]>,
    pathname: string,
    texts: SidebarMenuTexts,
  ) => ReturnType<getMenuListType>
> = {
  admin: (
    _: NonNullable<Session["user"]>,
    pathname: string,
    texts: SidebarMenuTexts,
  ) => getAdminMenuList(pathname, texts),
  // todo change
  trainer: (
    authUser: NonNullable<Session["user"]>,
    pathname: string,
    texts: SidebarMenuTexts,
  ) => getTrainerMenuList(authUser)(pathname, texts),
};
