import { z } from "zod";
import { getTranslations } from "next-intl/server";
import { FormType } from "@/texts/components/forms";
import { dayTypes, planObjectives } from "@/types/dto";
import DOMPurify from "dompurify";
import { AIGeneratePopTexts } from "@/components/forms/ai-generate-pop";

export const fileItemSchema = z.object({
  id: z.union([z.string(), z.number()]),
  src: z.string(),
  file: z.instanceof(File),
});

export interface BaseFormProps {
  type?: FormType;
  path: string;
}

export interface AITitleBodyForm {
  titleAIGeneratedPopTexts: AIGeneratePopTexts;
  bodyAIGeneratedPopTexts: AIGeneratePopTexts;
  aiCheckBoxes: Record<string, string>;
}
export interface EmailSchemaTexts {
  email: string;
}

export const getEmailSchema = (texts: EmailSchemaTexts) =>
  z.object({
    email: z.string().email(texts.email),
  });

export const getEmailSchemaTexts = async (): Promise<EmailSchemaTexts> => {
  const t = await getTranslations("zod.EmailSchemaTexts");
  return {
    email: t("email"),
  };
};

// export const getI18nEmailSchema = async () => {
//   const t = await getTranslations("zod.EmailSchemaTexts");
//   return zerialize(
//     getEmailSchema({
//       email: t("email"),
//     }),
//   );
// };

export interface ResetPasswordSchemaTexts {
  minPassword: string;
  confirmPassword: string;
}

export const getResetPasswordSchema = ({
  minPassword,
  confirmPassword,
}: ResetPasswordSchemaTexts) =>
  z
    .object({
      password: z.string().min(4, minPassword),
      confirmPassword: z.string().min(4, minPassword),
    })
    .superRefine((data, ctx) => {
      if (data.password !== data.confirmPassword) {
        ctx.addIssue({
          path: ["confirmPassword"],
          message: confirmPassword,
          code: z.ZodIssueCode.custom,
        });
      }
    });
export const getResetPasswordSchemaTexts =
  async (): Promise<ResetPasswordSchemaTexts> => {
    const t = await getTranslations("zod.ResetPasswordSchemaTexts");
    return {
      minPassword: t("minPassword"),
      confirmPassword: t("confirmPassword"),
    };
  };

// export const getI18nResetPasswordSchema = async () => {
//   const t = await getTranslations("zod.ResetPasswordSchemaTexts");
//   return zerialize(
//     getResetPasswordSchema({
//       minPassword: t("minPassword"),
//       confirmPassword: t("confirmPassword"),
//     }),
//   );
// };

export interface SignInSchemaTexts extends EmailSchemaTexts {
  minPassword: string;
}

export const getSignInSchema = ({ minPassword, email }: SignInSchemaTexts) =>
  z
    .object({
      // email: z.string().email("Invalid email address"),
      password: z.string().min(4, minPassword),
    })
    .and(getEmailSchema({ email }));

export const getSignInSchemaTexts = async (): Promise<SignInSchemaTexts> => {
  const [emailTexts, signInTexts] = await Promise.all([
    getEmailSchemaTexts(),
    getTranslations("zod.SignInSchemaTexts"),
  ]);

  return {
    ...emailTexts,
    minPassword: signInTexts("minPassword"),
  };
};

export interface UpdateProfileSchemaTexts {
  minFirstName: string;
  minLastName: string;
  minImage: string;
}

export const getUpdateProfileSchema = ({
  minFirstName,
  minLastName,
  minImage,
}: UpdateProfileSchemaTexts) =>
  z.object({
    firstName: z.string().min(2, minFirstName),
    lastName: z.string().min(2, minLastName),
    image: z.array(fileItemSchema).optional(),
  });

// export const getI18nUpdateProfileSchema = async () => {
//   const t = await getTranslations("zod.UpdateProfileSchemaTexts");
//   return zerialize(
//     getUpdateProfileSchema({
//       minFirstName: t("minFirstName"),
//       minLastName: t("minLastName"),
//       invalidUrl: t("invalidUrl"),
//     }),
//   );
// };

export interface RegisterSchemaTexts
  extends EmailSchemaTexts,
    UpdateProfileSchemaTexts,
    ResetPasswordSchemaTexts,
    ResetPasswordSchemaTexts {
  minPassword: string;
  mustLetters: string;
  mustNumbers: string;
  mustSpecial: string;
}

export const getRegistrationSchema = ({
  minPassword,
  confirmPassword,
  minLastName,
  minFirstName,
  minImage,
  email,
  mustLetters,
  mustSpecial,
  mustNumbers,
}: RegisterSchemaTexts) =>
  z
    .object({
      confirmPassword: z.string().min(4, minPassword),
      password: z
        .string()
        .regex(/[a-zA-Z]/, {
          message: mustLetters,
        })
        .regex(/\d/, { message: mustNumbers })
        .regex(/[!@#$%^&*(),.?":{}|<>]/, {
          message: mustSpecial,
        })
        .min(8, { message: minPassword }),
    })
    .and(getEmailSchema({ email }))
    .and(getUpdateProfileSchema({ minFirstName, minLastName, minImage }))
    .superRefine((data, ctx) => {
      if (data.password !== data.confirmPassword) {
        ctx.addIssue({
          path: ["confirmPassword"],
          message: confirmPassword,
          code: z.ZodIssueCode.custom,
        });
      }
    });

export const getUpdateProfileSchemaTexts =
  async (): Promise<UpdateProfileSchemaTexts> => {
    const t = await getTranslations("zod.UpdateProfileSchemaTexts");
    return {
      minFirstName: t("minFirstName"),
      minLastName: t("minLastName"),
      minImage: t("minImage"),
    };
  };

export const getRegistrationSchemaTexts =
  async (): Promise<RegisterSchemaTexts> => {
    const [emailTexts, signInTexts, updateProfileTexts, resetPasswordTexts, t] =
      await Promise.all([
        getEmailSchemaTexts(),
        getSignInSchemaTexts(),
        getUpdateProfileSchemaTexts(),
        getResetPasswordSchemaTexts(),
        getTranslations("zod.RegisterSchemaTexts"),
      ]);

    return {
      ...emailTexts,
      ...signInTexts,
      ...updateProfileTexts,
      ...resetPasswordTexts,
      minPassword: t("minPassword"),
      mustLetters: t("mustLetters"),
      mustNumbers: t("mustNumbers"),
      mustSpecial: t("mustSpecial"),
    };
  };

export interface ImageSchemaTexts {
  minImages: string;
}

export const getImageSchema = ({ minImages }: ImageSchemaTexts) =>
  z.object({
    images: z.array(fileItemSchema).min(1, minImages),
  });

export const getImageSchemaTexts: () => Promise<ImageSchemaTexts> =
  async () => {
    const t = await getTranslations("zod.forms.components.InputFile");
    return {
      minImages: t("errors", { type: t("types.image") }),
    };
  };

export interface VideoSchemaTexts {
  minVideos: string;
}

export const getVideoSchemaTexts = async () => {
  const t = await getTranslations("zod.forms.components.InputFile");
  return {
    minVideos: t("errors", { type: t("types.video") }),
  };
};

export const getVideoSchema = ({ minVideos }: VideoSchemaTexts) =>
  z.object({
    videos: z.array(fileItemSchema).min(1, minVideos),
  });

export interface TitleBodySchemaTexts {
  minTitle: string;
  minBody: string;
}

export const getTitleBodySchema = ({
  minTitle,
  minBody,
}: TitleBodySchemaTexts) =>
  z.object({
    title: z.string().min(2, minTitle),
    body: z.string().min(2, minBody),
  });

export const getTitleBodySchemaTexts =
  async (): Promise<TitleBodySchemaTexts> => {
    const t = await getTranslations("zod.TitleBodySchemaTexts");
    return {
      minTitle: t("minTitle"),
      minBody: t("minBody"),
    };
  };

export interface PostSchemaTexts
  extends TitleBodySchemaTexts,
    ImageSchemaTexts {
  minTags: string;
}

export const getPostSchema = (texts: PostSchemaTexts) =>
  z
    .object({
      tags: z
        .array(z.object({ label: z.string(), value: z.string() }))
        .min(1, texts.minTags),
    })
    .and(getTitleBodySchema(texts))
    .and(getImageSchema(texts));

export const getPostSchemaTexts = async (): Promise<PostSchemaTexts> => {
  const [titleBodyTexts, imageTexts, t] = await Promise.all([
    getTitleBodySchemaTexts(),
    getImageSchemaTexts(),
    getTranslations("zod.PostSchemaTexts"),
  ]);

  return {
    ...titleBodyTexts,
    ...imageTexts,
    minTags: t("minTags"),
  };
};

export const macroKeys = [
  "fat",
  "saturatedFat",
  "carbohydrates",
  "sugar",
  "protein",
  "salt",
] as (keyof IngredientNutritionalFactType["nutritionalFact"])[];
export const dietTypes = [
  "VEGAN",
  "VEGETARIAN",
  // "CARNIVORE",
  "OMNIVORE",
] as const;
export const unitTypes = ["GRAM", "MILLILITER"] as const;

export interface IngredientSchemaTexts {
  minName: string;
  invalidDietType: string;
}

export async function getIngredientSchemaTexts(): Promise<IngredientSchemaTexts> {
  const t = await getTranslations("zod.IngredientSchemaTexts");
  return {
    minName: t("minName"),
    invalidDietType: t("invalidDietType"),
  };
}

export const getIngredientSchema = ({
  minName,
  invalidDietType,
}: IngredientSchemaTexts) =>
  z.object({
    name: z.string().min(2, minName),
    type: z.enum(dietTypes, {
      message: invalidDietType,
    }),
  });

interface NutritionalFactSchemaTextItem {
  positive: string;
  nonnegative: string;
  max: string;
}
export interface NutritionalFactSchemaTexts {
  fat: NutritionalFactSchemaTextItem;
  saturatedFat: NutritionalFactSchemaTextItem;
  carbohydrates: NutritionalFactSchemaTextItem;
  sugar: NutritionalFactSchemaTextItem;
  protein: NutritionalFactSchemaTextItem;
  salt: NutritionalFactSchemaTextItem;
  unit: string;
  refineSaturatedFat: string;
  refineSugar: string;
  totalGrams: string;
  atLeastOnePositive: string;
}

export async function getNutritionalFactSchemaTexts(): Promise<NutritionalFactSchemaTexts> {
  const t = await getTranslations("zod.NutritionalFactSchemaTexts");

  const macro = [
    "fat",
    "saturatedFat",
    "carbohydrates",
    "sugar",
    "protein",
    "salt",
  ].reduce(
    (acc, key) => ({
      ...acc,
      [key]: {
        positive: t(`${key}.positive`),
        nonnegative: t(`${key}.nonnegative`),
        max: t(`${key}.max`),
      },
    }),
    {} as Record<string, NutritionalFactSchemaTextItem>,
  );

  return {
    unit: t("unit"),
    refineSaturatedFat: t("refineSaturatedFat"),
    refineSugar: t("refineSugar"),
    totalGrams: t("totalGrams"),
    atLeastOnePositive: t("atLeastOnePositive"),
    ...macro,
  } as NutritionalFactSchemaTexts;
}

const getNutritionalFactSchema = ({
  refineSugar,
  refineSaturatedFat,
  fat,
  sugar,
  saturatedFat,
  salt,
  unit,
  protein,
  carbohydrates,
  totalGrams,
  atLeastOnePositive,
}: NutritionalFactSchemaTexts) =>
  z
    .object({
      fat: z.coerce
        .number()
        // .positive({ message: fat.positive })
        .nonnegative({ message: fat.nonnegative })
        .max(100, { message: fat.max }),
      saturatedFat: z.coerce
        .number()
        // .positive({ message: saturatedFat.positive })
        .nonnegative({ message: saturatedFat.nonnegative })
        .max(100, { message: saturatedFat.max }),
      carbohydrates: z.coerce
        .number()
        // .positive({ message: carbohydrates.positive })
        .nonnegative({ message: carbohydrates.nonnegative })
        .max(100, { message: carbohydrates.max }),
      sugar: z.coerce
        .number()
        // .positive({ message: sugar.positive })
        .nonnegative({ message: sugar.nonnegative })
        .max(100, { message: sugar.max }),
      protein: z.coerce
        .number()
        // .positive({ message: protein.positive })
        .nonnegative({ message: protein.nonnegative })
        .max(100, { message: protein.max }),
      salt: z.coerce
        .number()
        // .positive({ message: salt.positive })
        .nonnegative({ message: salt.nonnegative })
        .max(100, { message: salt.max }),
      unit: z.enum(unitTypes, { message: unit }),
    })
    .superRefine((data, ctx) => {
      if (data.saturatedFat > data.fat) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: refineSaturatedFat,
          path: ["saturatedFat"],
        });
      }
    })
    .superRefine((data, ctx) => {
      if (data.sugar > data.carbohydrates) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: refineSugar,
          path: ["sugar"],
        });
      }
    });

// .superRefine((data, ctx) => {
//   const keysToCheck: (keyof typeof data)[] = [
//     "carbohydrates",
//     "protein",
//     "fat",
//     "salt",
//   ];
//   const allFieldsTouched = keysToCheck.every(
//     (key) => data[key] !== undefined && data[key] !== 0,
//   );
//
//   if (allFieldsTouched) {
//     const sum = data.carbohydrates + data.protein + data.fat + data.salt;
//     if (sum !== 100) {
//       keysToCheck.forEach((key) => {
//         ctx.addIssue({
//           code: z.ZodIssueCode.custom,
//           message: totalGrams + ` ${sum}`,
//           path: [key],
//         });
//       });
//     }
//   }
// });

export interface IngredientNutritionalFactSchemaTexts {
  ingredient: IngredientSchemaTexts;
  nutritionalFact: NutritionalFactSchemaTexts;
}

export async function getIngredientNutritionalFactSchemaTexts(): Promise<IngredientNutritionalFactSchemaTexts> {
  const [ingredientTexts, nutritionalFactTexts] = await Promise.all([
    getIngredientSchemaTexts(),
    getNutritionalFactSchemaTexts(),
  ]);

  return {
    ingredient: ingredientTexts,
    nutritionalFact: nutritionalFactTexts,
  };
}

export const getIngredientNutritionalFactSchema = ({
  ingredient,
  nutritionalFact,
}: IngredientNutritionalFactSchemaTexts) =>
  z.object({
    ingredient: getIngredientSchema(ingredient),
    nutritionalFact: getNutritionalFactSchema(nutritionalFact),
  });

export interface IngredientQuantitySchemaTexts {
  minQuantity: string;
  requiredIngredient: string;
}
export async function getIngredientQuantitySchemaTexts(): Promise<IngredientQuantitySchemaTexts> {
  const t = await getTranslations("zod.IngredientQuantitySchemaTexts");
  return {
    minQuantity: t("minQuantity"),
    requiredIngredient: t("requiredIngredient"),
  };
}

export const getIngredientQuantitySchema = ({
  minQuantity,
  requiredIngredient,
}: IngredientQuantitySchemaTexts) =>
  z.object({
    id: z.coerce
      .number({ invalid_type_error: requiredIngredient })
      .refine((val) => !isNaN(val), {
        message: requiredIngredient,
      }),
    quantity: z.coerce
      .number({
        invalid_type_error: minQuantity,
      })
      .min(1, minQuantity)
      .refine((val) => !isNaN(val), {
        message: minQuantity,
      }),
  });
//    const t = await getTranslations("zod.forms.components.InputFile");
export interface RecipeSchemaTexts
  extends TitleBodySchemaTexts,
    ImageSchemaTexts {
  ingredient: IngredientQuantitySchemaTexts;
  minVideos: string;
  minIngredients: string;
}

export async function getRecipeSchemaTexts(): Promise<RecipeSchemaTexts> {
  const [titleBodyTexts, imageSchemaTexts, tv, t, ingredient] =
    await Promise.all([
      getTitleBodySchemaTexts(),
      getImageSchemaTexts(),
      getTranslations("zod.forms.components.InputFile"),
      getTranslations("zod.RecipeSchemaTexts"),
      getIngredientQuantitySchemaTexts(),
    ]);

  return {
    ...titleBodyTexts,
    ...imageSchemaTexts,
    minVideos: tv("errors", { type: tv("types.video") }),
    minIngredients: t("minIngredients"),
    ingredient,
  };
}

export const getRecipeSchema = (texts: RecipeSchemaTexts) =>
  z
    .object({
      ingredients: z
        .array(getIngredientQuantitySchema(texts.ingredient))
        .min(1, texts.minIngredients),
      videos: z.array(fileItemSchema).min(1, texts.minVideos),
    })
    .and(getTitleBodySchema(texts))
    .and(getImageSchema(texts));

export interface PlanSchemaTexts
  extends TitleBodySchemaTexts,
    ImageSchemaTexts {
  minPrice: string;
  minDays: string;
  objective: string;
}

export async function getPlanSchemaTexts(): Promise<PlanSchemaTexts> {
  const [titleBodyTexts, imageSchemaTexts, t] = await Promise.all([
    getTitleBodySchemaTexts(),
    getImageSchemaTexts(),
    getTranslations("zod.PlanSchemaTexts"),
  ]);

  return {
    ...titleBodyTexts,
    ...imageSchemaTexts,
    minPrice: t("minPrice"),
    minDays: t("minDays"),
    objective: t("objective"),
  };
}

export const getPlanSchema = (texts: PlanSchemaTexts) =>
  z
    .object({
      days: z
        .array(z.coerce.number({ invalid_type_error: texts.minDays }))
        .min(1, texts.minDays),
      price: z.coerce.number().min(1, texts.minPrice),
      objective: z.enum(planObjectives as [string, ...string[]], {
        invalid_type_error: texts.objective,
      }),
    })
    .and(getTitleBodySchema(texts))
    .and(getImageSchema(texts));

export interface MealSchemaTexts {
  hourPeriod: string;
  minutePeriod: string;
  minRecipes: string;
  periodUsed: string;
}

export async function getMealSchemaTexts(): Promise<MealSchemaTexts> {
  const t = await getTranslations("zod.MealSchemaTexts");
  return {
    hourPeriod: t("hourPeriod"),
    minutePeriod: t("minutePeriod"),
    minRecipes: t("minRecipes"),
    periodUsed: t("periodUsed"),
  };
}

export const getMealSchema = (texts: MealSchemaTexts) =>
  z.object({
    period: z.object({
      hour: z.coerce
        .number({ invalid_type_error: texts.hourPeriod })
        .min(0, texts.hourPeriod)
        .max(23, texts.hourPeriod),
      minute: z.coerce
        .number({ invalid_type_error: texts.minutePeriod })
        .min(0, texts.minutePeriod)
        .max(59, texts.minutePeriod),
    }),
    recipes: z
      .array(z.coerce.number({ invalid_type_error: texts.minRecipes }))
      .min(1, texts.minRecipes),
  });

export interface DaySchemaTexts extends TitleBodySchemaTexts {
  type: string;
  minMeals: string;
  period: string;
  minRecipes: string;
  meals: MealSchemaTexts;
}

export async function getDaySchemaTexts(): Promise<DaySchemaTexts> {
  const [titleBodyTexts, meals, t] = await Promise.all([
    getTitleBodySchemaTexts(),
    getMealSchemaTexts(),
    getTranslations("zod.DaySchemaTexts"),
  ]);

  return {
    ...titleBodyTexts,
    meals,
    type: t("type"),
    minMeals: t("minMeals"),
    period: t("period"),
    minRecipes: t("minRecipes"),
  };
}

export const getDaySchema = (texts: DaySchemaTexts) =>
  z
    .object({
      type: z.enum(dayTypes as [string, ...string[]], {
        invalid_type_error: texts.type,
      }),
      meals: z.array(getMealSchema(texts.meals)).min(1, texts.minMeals),
    })
    .and(getTitleBodySchema(texts));

export interface CheckoutSchemaTexts {
  confirmPrice: string;
}

export const getCheckoutSchema = (
  price: number,
  { confirmPrice }: CheckoutSchemaTexts,
) =>
  z.object({
    userConfirmedPrice: z.coerce
      .number()
      .refine((value) => value === price, confirmPrice),
  });

export interface AdminEmailSchemaTexts {
  minSubject: string;
  minContent: string;
  email: string;
}

export const getAdminEmailSchema = ({
  minSubject,
  minContent,
  email,
}: AdminEmailSchemaTexts) =>
  z.object({
    subject: z.string().min(1, minSubject),
    content: z.string().min(1, minContent),
    email: z.string().email(email),
  });

export const getAdminEmailSchemaTexts =
  async (): Promise<AdminEmailSchemaTexts> => {
    const t = await getTranslations("zod.AdminEmailSchemaTexts");
    return {
      minSubject: t("minSubject"),
      minContent: t("minContent"),
      email: t("email"),
    };
  };
export const activities = {
  // BMR: 1,
  Sedentary: 1.2,
  Light: 1.375,
  Moderate: 1.455,
  Active: 1.55,
  VeryActive: 1.725,
} as const;

export type ActivitiesTexts = Record<keyof typeof activities, string>;

export async function getActivitiesTexts(): Promise<ActivitiesTexts> {
  const t = await getTranslations("zod.ActivitiesTexts");
  return {
    // BMR: t("BMR"),
    Sedentary: t("Sedentary"),
    Light: t("Light"),
    Moderate: t("Moderate"),
    Active: t("Active"),
    VeryActive: t("VeryActive"),
  };
}
export const genders = ["male", "female"] as const;

export type GenderText = Record<(typeof genders)[number], string>;

export async function getGenderTexts(): Promise<GenderText> {
  const t = await getTranslations("zod.GenderTexts");
  return {
    female: t("female"),
    male: t("male"),
  };
}

export interface CalculatorSchemaTexts {
  minAge: string;
  maxAge: string;
  validGender: string;
  minHeight: string;
  maxHeight: string;
  minWeight: string;
  maxWeight: string;
  validActivity: string;
  validIntake: string;
}

export async function getCalculatorSchemaTexts(): Promise<CalculatorSchemaTexts> {
  const t = await getTranslations("zod.CalculatorSchemaTexts");
  return {
    minAge: t("minAge"),
    maxAge: t("maxAge"),
    validGender: t("validGender"),
    minHeight: t("minHeight"),
    maxHeight: t("maxHeight"),
    minWeight: t("minWeight"),
    maxWeight: t("maxWeight"),
    validActivity: t("validActivity"),
    validIntake: t("validIntake"),
  };
}

export const getCalculatorSchema = ({
  minAge,
  maxAge,
  validGender,
  minHeight,
  maxHeight,
  minWeight,
  maxWeight,
  validActivity,
  validIntake,
}: CalculatorSchemaTexts) =>
  z.object({
    age: z.coerce
      .number({ invalid_type_error: minAge })
      .min(15, minAge)
      .max(85, maxAge),
    height: z.coerce
      .number({ invalid_type_error: minHeight })
      .min(0.01, minHeight),
    weight: z.coerce
      .number({ invalid_type_error: minWeight })
      .min(0.01, minWeight),
    gender: z.enum(genders, { errorMap: () => ({ message: validGender }) }),
    activity: z.enum(
      [
        // "BMR",
        "Sedentary",
        "Light",
        "Moderate",
        "Active",
        "VeryActive",
      ],
      {
        errorMap: () => ({ message: validActivity }),
      },
    ),
    intake: z.enum(
      [
        "Maintain weight",
        "Mild weight loss",
        "Weight loss",
        "Extreme weight loss",
        "Mild weight gain",
        "Weight gain",
        "Fast Weight gain",
      ],
      {
        errorMap: () => ({ message: validIntake }),
      },
    ),
  });

export interface CommentSchemaTexts extends TitleBodySchemaTexts {
  body: string;
}

export const getCommentSchemaTexts = async (): Promise<CommentSchemaTexts> => {
  const [titleBodyTexts, t] = await Promise.all([
    getTitleBodySchemaTexts(),
    getTranslations("zod.CommentSchemaTexts"),
  ]);
  return {
    ...titleBodyTexts,
    body: t("body"),
  };
};

export const getCommentSchema = (texts: CommentSchemaTexts) =>
  getTitleBodySchema(texts).extend({
    body: z.string().refine(
      (v) =>
        DOMPurify.sanitize(v, {
          ALLOWED_TAGS: [],
          ALLOWED_ATTR: [],
        })
          .trim()
          .replace(/\s+/g, " ").length >= 20,
      texts.body,
    ),
  });

export interface DiffusionSchemaTexts {
  minPrompt: string;
  maxPrompt: string;
  minNegativePrompt: string;
  maxNegativePrompt: string;
}

export const getDiffusionSchemaTexts =
  async (): Promise<DiffusionSchemaTexts> => {
    const t = await getTranslations("zod.DiffusionSchemaTexts");
    return {
      minPrompt: t("minPrompt"),
      maxPrompt: t("maxPrompt"),
      minNegativePrompt: t("minNegativePrompt"),
      maxNegativePrompt: t("maxNegativePrompt"),
    };
  };

export const getDiffusionSchema = ({
  minPrompt,
  maxPrompt,
  minNegativePrompt,
  maxNegativePrompt,
}: DiffusionSchemaTexts) =>
  z.object({
    prompt: z
      .string()
      .transform((val) => val.trim().replace(/\s+/g, " "))
      .refine((val) => val.length >= 20, { message: minPrompt })
      .refine((val) => val.length <= 100, { message: maxPrompt }),
    negativePrompt: z
      .string()
      .transform((val) => val.trim().replace(/\s+/g, " "))
      .refine((val) => val.length >= 10, { message: minNegativePrompt })
      .refine((val) => val.length <= 100, { message: maxNegativePrompt }),
    numImages: z.coerce.number().min(1).max(3),
  });
export type DiffusionSchemaType = z.infer<
  ReturnType<typeof getDiffusionSchema>
>;
export interface AdminAICreatePostSchemaTexts extends DiffusionSchemaTexts {
  description: string;
}
export const getAdminAICreatePostSchemaTexts = async () => {
  const [diffusionTexts, t] = await Promise.all([
    getDiffusionSchemaTexts(),
    getTranslations("zod.AdminAICreatePostSchemaTexts"),
  ]);

  return {
    ...diffusionTexts,
    description: t("description"),
  };
};

export const getAdminAICreatePostSchema = ({
  description,
  ...diffusion
}: AdminAICreatePostSchemaTexts) =>
  z
    .object({
      description: z
        .string({ message: description })
        .transform((val) => val.trim().replace(/\s+/g, " "))
        .refine((val) => val.length >= 30, { message: description }),
      images: z.array(fileItemSchema).min(0, ""),
      numberOfPosts: z.coerce.number().min(1).max(10),
    })
    .and(getDiffusionSchema(diffusion));

export interface AnswerFromBodySchemaTexts {
  minQuestion: string;
}

export function getAnswerFromBodySchemaTexts(): Promise<AnswerFromBodySchemaTexts> {
  return getTranslations("zod.AnswerFromBodySchemaTexts").then((t) => ({
    minQuestion: t("minQuestion"),
  }));
}

export const getAnswerFromBodySchema = ({
  minQuestion,
}: AnswerFromBodySchemaTexts) =>
  z.object({
    question: z.string().min(10, minQuestion),
    k: z.coerce.number().int().min(1).max(100),
  });

export type CommentSchemaType = z.infer<ReturnType<typeof getCommentSchema>>;

export type CalculatorSchemaType = z.infer<
  ReturnType<typeof getCalculatorSchema>
>;

export type AdminEmailSchemaType = z.infer<
  ReturnType<typeof getAdminEmailSchema>
>;

export type CheckoutSchemaType = z.infer<ReturnType<typeof getCheckoutSchema>>;

export type PlanSchemaType = z.infer<ReturnType<typeof getPlanSchema>>;

export type IngredientQuantitySchemaType = z.infer<
  ReturnType<typeof getIngredientQuantitySchema>
>;

export type RecipeSchemaType = z.infer<ReturnType<typeof getRecipeSchema>>;

export type IngredientNutritionalFactType = z.infer<
  ReturnType<typeof getIngredientNutritionalFactSchema>
>;

export type IngredientSchemaType = z.infer<
  ReturnType<typeof getIngredientSchema>
>;
export type NutritionalFactSchemaType = z.infer<
  ReturnType<typeof getNutritionalFactSchema>
>;

export type ImageType = z.infer<ReturnType<typeof getImageSchema>>;
export type VideoType = z.infer<ReturnType<typeof getVideoSchema>>;
export type TitleBodyType = z.infer<ReturnType<typeof getTitleBodySchema>>;
export type PostType = z.infer<ReturnType<typeof getPostSchema>>;

export type EmailType = z.infer<ReturnType<typeof getEmailSchema>>;
export type ResetPasswordType = z.infer<
  ReturnType<typeof getResetPasswordSchema>
>;
export type SignInType = z.infer<ReturnType<typeof getSignInSchema>>;
export type RegisterType = z.infer<ReturnType<typeof getRegistrationSchema>>;
export type UpdateProfileType = z.infer<
  ReturnType<typeof getUpdateProfileSchema>
>;

export type MealSchemaType = z.infer<ReturnType<typeof getMealSchema>>;
export type DaySchemaType = z.infer<ReturnType<typeof getDaySchema>>;

// export interface BasePropsForm {
//   schema: SzType;
// }
// todo intl
export const conversationMessageSchema = z.object({
  content: z.string().min(1, "Message must be at least 1 character"),
});
export type ConversationMessageType = z.infer<typeof conversationMessageSchema>;

export type AdminAICreatePostSchemaType = z.infer<
  ReturnType<typeof getAdminAICreatePostSchema>
>;

export type AnswerFromBodySchemaType = z.infer<
  ReturnType<typeof getAnswerFromBodySchema>
>;
