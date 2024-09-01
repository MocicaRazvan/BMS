import { Role } from "@/types/fetch-utils";
import { Session } from "next-auth";
import { isDeepEqual } from "@/lib/utils";

export interface LinkNav {
  href: string;
  role: Role;
  id: string;
}

type createLinks = (authUser: NonNullable<Session["user"]>) => LinkNav[];
export const createPostsLinks: createLinks = ({ id }): LinkNav[] => [
  {
    href: "/posts/approved",
    role: "ROLE_USER",
    id: "approvedPosts",
  },
  // {
  //   href: "/sidebar/posts",
  //   role: "ROLE_ADMIN",
  //   id: "allPosts",
  // },
  {
    href: `/trainer/user/${id}/posts`,
    role: "ROLE_TRAINER",
    id: "yourPosts",
  },
  {
    href: `/trainer/posts/create`,
    role: "ROLE_TRAINER",
    id: "createPost",
  },
];
export const createRecipesLinks: createLinks = ({ id }): LinkNav[] => [
  // {
  //   href: "/sidebar/recipes",
  //   role: "ROLE_ADMIN",
  //   id: "allRecipes",
  // },
  {
    href: `/trainer/user/${id}/recipes`,
    role: "ROLE_TRAINER",
    id: "yourRecipes",
  },
  {
    href: `/trainer/recipes/create`,
    role: "ROLE_TRAINER",
    id: "createRecipe",
  },
];
export const createPlansLinks: createLinks = ({ id }): LinkNav[] => [
  {
    href: "/plans/approved",
    role: "ROLE_USER",
    id: "approvedPlans",
  },
  // {
  //   href: "/sidebar/plans",
  //   role: "ROLE_ADMIN",
  //   id: "allPlans",
  // },
  {
    href: `/trainer/user/${id}/plans`,
    role: "ROLE_TRAINER",
    id: "yourPlans",
  },
  {
    href: `/trainer/plans/create`,
    role: "ROLE_TRAINER",
    id: "createPlan",
  },
  {
    href: `/trainer/user/${id}/plans/monthlySales`,
    role: "ROLE_TRAINER",
    id: "monthlyPlan",
  },
  {
    href: `/trainer/user/${id}/plans/dailySales`,
    role: "ROLE_TRAINER",
    id: "dailyPlan",
  },
];

export type ComponentMenuLink = Omit<LinkNav, "id"> & { text: string };

export const fromLinkNavsToComponentMenuLinks = (
  linksNavs: LinkNav[],
  texts: Record<string, string>,
): ComponentMenuLink[] => linksNavs.map((e) => ({ ...e, text: texts[e.id] }));
export const linkFactory = (
  authUser: Session["user"],
  f: createLinks,
  texts: Record<string, string>,
) => (authUser ? fromLinkNavsToComponentMenuLinks(f(authUser), texts) : []);

export const linksEqual = (a: LinkNav[], b: LinkNav[]) =>
  a.length === b.length && a.every((link, i) => isDeepEqual(link, b[i]));

export const shouldRenderLink = (
  authUser: NonNullable<Session["user"]>,
  linkRole: Role,
) => {
  switch (authUser.role) {
    case "ROLE_ADMIN":
      return true;
    case "ROLE_TRAINER":
      return linkRole !== "ROLE_ADMIN";
    case "ROLE_USER":
      return linkRole === "ROLE_USER";
    default:
      return false;
  }
};
