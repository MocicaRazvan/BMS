export type Role = "ROLE_USER" | "ROLE_TRAINER" | "ROLE_ADMIN";
export type SortDirection = "asc" | "desc" | "none";
export type AuthProvider = "LOCAL" | "GITHUB" | "GOOGLE";
export type AcceptHeader = "application/x-ndjson" | "application/json";
export const sortDirections: SortDirection[] = ["asc", "desc", "none"] as const;
