import { Role } from "@/types/fetch-utils";
import { locales } from "@/navigation";
import { NextRequest } from "next/server";
import { NextMiddlewareWithAuth, withAuth } from "next-auth/middleware";
import { HIERARCHY } from "@/lib/constants";

const stripRE = new RegExp(`^\/(${locales.join("|")})(?=\/|$)`);
const optionalStripLocale = (url: string): string => {
  const stripped = url.replace(stripRE, "");
  return stripped === "" ? "/home" : stripped;
};

const createPathRegex = (paths: string[]) =>
  new RegExp(`^\/${paths.join("|")}.*`);

const PUBLIC_REGEX = createPathRegex([
  "test",
  "home",
  "auth",
  "not-found",
  "termsOfService",
  "calculator",
]);
const USER_REGEX = createPathRegex([
  "cart",
  "chat",
  "daysCalendar",
  "failure",
  "kanban",
  "orderComplete",
  "orders",
  "plans",
  "posts",
  "subscriptions",
  "success",
  "users",
]);

const TRAINER_REGEX = createPathRegex(["trainer"]);
const ADMIN_REGEX = createPathRegex(["admin"]);

const getMinRoleByPath = (origPath: string): Role | "undefined" => {
  const path = optionalStripLocale(origPath);
  if (ADMIN_REGEX.test(path)) {
    return "ROLE_ADMIN";
  }
  if (TRAINER_REGEX.test(path)) {
    return "ROLE_TRAINER";
  }
  if (USER_REGEX.test(path)) {
    return "ROLE_USER";
  }
  if (PUBLIC_REGEX.test(path)) {
    return "undefined";
  }
  return "undefined";
};

export const authMiddleware = (
  req: NextRequest,
  success: NextMiddlewareWithAuth,
) =>
  (
    withAuth(success, {
      callbacks: {
        authorized: ({ token, req }) => {
          const role = token?.user?.role || "undefined";
          const path = req.nextUrl.pathname;
          const minRole = getMinRoleByPath(path);
          const res = HIERARCHY[role] >= HIERARCHY[minRole];
          if (!res) {
            console.warn(
              `Unauthorized access attempt by role ${role} to path ${path}`,
            );
          }
          return res;
        },
      },
    }) as any
  )(req);
