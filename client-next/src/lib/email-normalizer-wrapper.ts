import { normalizeEmail } from "email-normalizer";

export const normalizeEmailWrapper = (email: string) => {
  try {
    return normalizeEmail({ email });
  } catch (e) {
    return email;
  }
};
