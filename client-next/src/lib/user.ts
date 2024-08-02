"use server";
import { getServerSession, Session } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import { notFound } from "next/navigation";
import { Role } from "@/types/fetch-utils";

export async function getUser(): Promise<NonNullable<Session["user"]>> {
  const session = await getServerSession(authOptions);

  if (!session || !session.user) {
    return notFound();
  }

  return session.user;
}

// todo poate nu merge ca e pe server shit, daca ai probleme faci hook custom la fel cu useSession
export async function getUserWithMinRole(
  minRole: Role,
): Promise<NonNullable<Session["user"]>> {
  const user = await getUser();
  const authRole = user.role;

  if (authRole === "ROLE_ADMIN") return user;
  if (minRole === "ROLE_ADMIN") return notFound();
  if (minRole === "ROLE_TRAINER" && authRole === "ROLE_USER") return notFound();
  return user;
}

export async function getTheSameUser(
  userId: string,
): Promise<NonNullable<Session["user"]>> {
  const user = await getUser();
  if (user.id?.toString() !== userId?.toString()) return notFound();
  return user;
}

export async function getTheSameUserOrAdmin(
  userId: string,
): Promise<NonNullable<Session["user"]>> {
  const user = await getUser();
  console.log("IDS", user.id === userId);
  if (user.role === "ROLE_ADMIN") return user;
  if (user.id?.toString() === userId?.toString()) return user;
  return notFound();
}

export interface WithUser {
  authUser: NonNullable<Session["user"]>;
}
