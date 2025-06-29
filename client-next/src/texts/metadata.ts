import { getTranslations } from "next-intl/server";
import { Locale, locales } from "@/navigation/navigation";
import { Role } from "@/types/fetch-utils";
import { Session } from "next-auth";
import { appendCreatedAtDesc } from "@/lib/utils";

const languages = locales.reduce<Record<Locale, string>>(
  (acc, l) => ({ ...acc, [l]: `/${l}` }),
  {} as Record<Locale, string>,
);

export interface IntlMetadata {
  title: string;
  description: string;
  keywords?: string[];
  alternates?: {
    canonical: string;
    languages: Record<Locale, string>;
  };
}

function getKeywords(initial: string) {
  return initial.split(", ").map((k) => k.trim());
}

export async function getIntlMetadata(
  key: string,
  path?: string,
  locale?: Locale,
): Promise<IntlMetadata> {
  const t = await getTranslations(`metadata.${key}`);
  let canonical = path;
  if (locale) {
    canonical = `/${locale}${path}`;
  }

  return {
    title: t("title"),
    description: t("description"),
    keywords: getKeywords(t("keywords")),
    alternates: canonical
      ? {
          canonical,
          languages,
        }
      : undefined,
  };
}
interface MetadataValues {
  key: string;
  path: string;
  role: Role | "ROLE_PUBLIC";
}
const metadataValues = (id: string): MetadataValues[] => [
  {
    key: "user.Calculator",
    path: "/calculator",
    role: "ROLE_USER",
  },
  {
    key: "user.DaysCalendar",
    path: "/daysCalendar",
    role: "ROLE_USER",
  },
  {
    key: "user.Cart",
    path: "/cart",
    role: "ROLE_USER",
  },
  {
    key: "user.Chat",
    path: "/chat",
    role: "ROLE_USER",
  },
  {
    key: "user.Kanban",
    path: "/kanban",
    role: "ROLE_USER",
  },
  {
    key: "user.Orders",
    path: appendCreatedAtDesc("/orders"),
    role: "ROLE_USER",
  },
  {
    key: "user.ApprovedPlans",
    path: appendCreatedAtDesc("/plans/approved"),
    role: "ROLE_USER",
  },
  {
    key: "user.ApprovedPosts",
    path: appendCreatedAtDesc("/posts/approved"),
    role: "ROLE_USER",
  },
  {
    key: "user.Subscriptions",
    path: appendCreatedAtDesc("/subscriptions"),
    role: "ROLE_USER",
  },
  {
    key: "user.UserProfile",
    path: "/users/single/" + id,
    role: "ROLE_USER",
  },
  {
    key: "terms-of-service",
    path: "/termsOfService",
    role: "ROLE_PUBLIC",
  },
  {
    key: "privacy-policy",
    path: "/privacyPolicy",
    role: "ROLE_PUBLIC",
  },
  {
    key: "disclaimer",
    path: "/disclaimer",
    role: "ROLE_PUBLIC",
  },
  {
    key: "auth.SignIn",
    path: "/auth/signout",
    role: "ROLE_PUBLIC",
  },
  {
    key: "auth.SignOut",
    path: "/auth/signin",
    role: "ROLE_PUBLIC",
  },
  {
    key: "auth.SignUp",
    path: "/auth/signup",
    role: "ROLE_PUBLIC",
  },

  {
    key: "admin.Countries",
    path: "/admin/countries",
    role: "ROLE_ADMIN",
  },
  {
    key: "admin.DailySales",
    path: "/admin/dailySales",
    role: "ROLE_ADMIN",
  },
  {
    key: "admin.Dashboard",
    path: "/admin/dashboard",
    role: "ROLE_ADMIN",
  },
  {
    key: "admin.Email",
    path: "/admin/email",
    role: "ROLE_ADMIN",
  },
  {
    key: "admin.CreateIngredient",
    path: "/admin/ingredients/create",
    role: "ROLE_ADMIN",
  },
  {
    key: "admin.Ingredients",
    path: appendCreatedAtDesc("/admin/ingredients"),
    role: "ROLE_ADMIN",
  },
  // {
  //   key: "admin.Kanban",
  //   path: "/admin/kanban",
  //   role: "ROLE_ADMIN",
  // },
  {
    key: "admin.MonthlySales",
    path: "/admin/monthlySales",
    role: "ROLE_ADMIN",
  },
  {
    key: "admin.Orders",
    path: appendCreatedAtDesc("/admin/orders"),
    role: "ROLE_ADMIN",
  },
  {
    key: "admin.Plans",
    path: appendCreatedAtDesc("/admin/plans"),
    role: "ROLE_ADMIN",
  },
  {
    key: "admin.CreatePostAI",
    path: "/admin/posts/aiCreate",
    role: "ROLE_ADMIN",
  },
  {
    key: "admin.Posts",
    path: appendCreatedAtDesc("/admin/posts"),
    role: "ROLE_ADMIN",
  },
  {
    key: "admin.Recipes",
    path: appendCreatedAtDesc("/admin/recipes"),
    role: "ROLE_ADMIN",
  },
  {
    key: "admin.Users",
    path: appendCreatedAtDesc("/admin/users"),
    role: "ROLE_ADMIN",
  },
  {
    key: "trainer.CreateDay",
    path: "/trainer/days/create",
    role: "ROLE_TRAINER",
  },
  {
    key: "trainer.Ingredients",
    path: appendCreatedAtDesc("/trainer/ingredients"),
    role: "ROLE_TRAINER",
  },
  // {
  //   key: "trainer.Kanban",
  //   path: "/trainer/kanban",
  //   role: "ROLE_TRAINER",
  // },
  {
    key: "trainer.CreatePlan",
    path: "/trainer/plans/create",
    role: "ROLE_TRAINER",
  },
  {
    key: "trainer.CreatePost",
    path: "/trainer/posts/create",
    role: "ROLE_TRAINER",
  },
  {
    key: "trainer.CreateRecipe",
    path: "/trainer/recipes/create",
    role: "ROLE_TRAINER",
  },
  {
    key: "trainer.TrainerDays",
    path: appendCreatedAtDesc("/trainer/user/" + id + "/days"),
    role: "ROLE_TRAINER",
  },
  {
    key: "trainer.DailySales",
    path: "/trainer/user/" + id + "/plans/dailySales",
    role: "ROLE_TRAINER",
  },
  {
    key: "trainer.MonthlySales",
    path: "/trainer/user/" + id + "/plans/monthlySales",
    role: "ROLE_TRAINER",
  },
  {
    key: "trainer.TrainerPlans",
    path: appendCreatedAtDesc("/trainer/user/" + id + "/plans"),
    role: "ROLE_TRAINER",
  },
  {
    key: "trainer.TrainerPosts",
    path: appendCreatedAtDesc("/trainer/user/" + id + "/posts"),
    role: "ROLE_TRAINER",
  },
  {
    key: "trainer.TrainerRecipes",
    path: appendCreatedAtDesc("/trainer/user/" + id + "/recipes"),
    role: "ROLE_TRAINER",
  },
  {
    key: "admin.ArchiveQueues",
    path: "/admin/archiveQueues",
    role: "ROLE_ADMIN",
  },
];

export async function getMetadataValues(
  authUser: Session["user"],
  locale: Locale,
) {
  const role: MetadataValues["role"] = !authUser
    ? "ROLE_PUBLIC"
    : authUser.role;
  const id = authUser ? authUser.id : "";
  let values;
  if (role === "ROLE_ADMIN") {
    values = metadataValues(id);
  } else if (role === "ROLE_TRAINER") {
    values = metadataValues(id).filter((r) => r.role !== "ROLE_ADMIN");
  } else if (role === "ROLE_USER") {
    values = metadataValues(id).filter(
      (r) => r.role !== "ROLE_ADMIN" && r.role !== "ROLE_TRAINER",
    );
  } else {
    values = metadataValues(id).filter((r) => r.role === "ROLE_PUBLIC");
  }

  return await Promise.all(
    values.map(async (r) => ({
      ...r,
      metadata: await getIntlMetadata(r.key, r.path, locale),
    })),
  );
}
