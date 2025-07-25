import { FieldInputTexts } from "@/components/forms/input-file";
import { getTranslations } from "next-intl/server";
import { TitleBodyTexts } from "@/components/forms/title-body";
import { InputMultipleSelectorTexts } from "@/components/forms/input-multiselector";
import {
  CommentSchemaTexts,
  getAddDayToCalendarSchemaTexts,
  getAdminEmailSchemaTexts,
  getAnswerFromBodySchemaTexts,
  getCommentSchemaTexts,
  getDaySchemaTexts,
  getDiffusionSchemaTexts,
  getIngredientNutritionalFactSchemaTexts,
  getIngredientQuantitySchemaTexts,
  getMealSchemaTexts,
  getPlanSchemaTexts,
  getPostSchemaTexts,
  getRecipeSchemaTexts,
  getTitleBodySchemaTexts,
  getUpdateProfileSchemaTexts,
  macroKeys,
  UpdateProfileSchemaTexts,
} from "@/types/forms";
import { ButtonSubmitTexts } from "@/components/forms/button-submit";
import { ChatMessageFormTexts } from "@/components/forms/chat-message-form";
import {
  FieldTexts,
  IngredientFormTexts,
} from "@/components/forms/ingredient-form";
import { getIngredientPieChartTexts } from "@/texts/components/charts";
import { ChildInputMultipleSelectorTexts } from "@/components/forms/child-input-multipleselector";
import { RecipeFormTexts } from "@/components/forms/recipe-form";
import { PlanFormTexts } from "@/components/forms/plan-form";
import { CheckoutDrawerTexts } from "@/components/forms/checkout-drawer";
import { AdminEmailTexts } from "@/components/forms/admin-email";
import { DayFromTexts } from "@/components/forms/day-form";
import { DayType, dayTypes, planObjectives } from "@/types/dto";
import { UploadingProgressTexts } from "@/components/forms/uploading-progress";
import { AIGeneratePopTexts } from "@/components/forms/ai-generate-pop";
import { DiffusionImagesFormTexts } from "@/components/forms/diffusion-images-form";
import { UseNavigationGuardI18nTexts } from "@/hoooks/use-navigation-guard-i18n-form";
import { PasswordStrengthIndicatorTexts } from "@/components/forms/passowrd-strength-indicator";
import { AnswerFromBodyFormTexts } from "@/components/forms/answer-from-body-form";
import { getImageCropTexts } from "@/texts/components/common";
import { getEditorTexts } from "@/texts/components/editor";
import { CalendarDayFormTexts } from "@/components/forms/calendar-day-form";
import { AddDayToCalendarTexts } from "@/components/forms/add-day-to-calendar";
import { EmailFromFieldTexts } from "@/components/forms/email-form-field";

export type FormType = "create" | "update";

export async function getInputFileText(
  type: "image" | "video",
  cnt: "multiple" | "single" = "multiple",
): Promise<FieldInputTexts> {
  const [t, t1, imageCropTexts] = await Promise.all([
    getTranslations("zod.forms.components.InputFile"),
    getTranslations("zod.forms.components.FieldInputTexts"),
    getImageCropTexts(),
  ]);
  const singular = t("types." + type);
  const plural = t("types." + type + "s");
  return {
    title: t1("title", { type: singular }),
    showList: t1("showList", {
      type: cnt === "multiple" ? plural : singular,
      cnt,
    }),
    hideList: t1("hideList", {
      type: cnt === "multiple" ? plural : singular,
      cnt,
    }),
    loading: t("loading", { type: cnt === "multiple" ? plural : singular }),
    draggingActive: t1("draggingActive", { type: plural }),
    draggingInactive: t1("draggingInactive", { type: singular }),
    clearAll: t1("clearAll", { type: plural }),
    loadCount1: t1("loadCount1", { type }),
    loadCountMany: t1("loadCountMany", { type }),
    itemTexts: {
      header: t1("ItemTexts.header", { type }),
      tooltipContent: t1("ItemTexts.tooltipContent", {
        type,
      }),
    },
    imageCropTexts,
  };
}

export async function getTitleBodyText(): Promise<TitleBodyTexts> {
  const [t, editorTexts] = await Promise.all([
    getTranslations("zod.forms.components.TitleBodyTexts"),
    getEditorTexts(),
  ]);
  return {
    editorTexts,
    title: t("title"),
    body: t("body"),
    titlePlaceholder: t("titlePlaceholder"),
    bodyPlaceholder: t("bodyPlaceholder"),
    aiResponseTexts: {
      discardButtonText: t("aiResponseTexts.discardButtonText"),
      warningText: t("aiResponseTexts.warningText"),
      useButtonText: t("aiResponseTexts.useButtonText"),
    },
  };
}

export async function getInputMultipleSelectorTexts(
  type: "tags",
): Promise<InputMultipleSelectorTexts> {
  const t = await getTranslations(
    "zod.forms.components.InputMultipleSelectorTexts",
  );
  const typeLabel = t("type." + type);
  return {
    label: t("label", { type: typeLabel }),
    emptyIndicator: t("emptyIndicator"),
    placeholder: t("placeholder", { type: typeLabel }),
  };
}

export async function getButtonSubmitTexts(): Promise<ButtonSubmitTexts> {
  const t = await getTranslations("zod.forms.components.ButtonSubmitTexts");
  return {
    submitText: t("submitText"),
    loadingText: t("loadingText"),
  };
}
export async function getUploadingProgressTexts(
  type: "image" | "video",
): Promise<UploadingProgressTexts> {
  const t = await getTranslations(
    "zod.forms.components.UploadingProgressTexts",
  );
  const intlType = t("type." + type);
  return {
    loadedItems: t("loadedItems", { type: intlType }),
  };
}

export interface BaseFormTexts {
  header: string;
  altToast: string;
  descriptionToast: string;
  error: string;
  toastAction: string;
}

export const getBaseFormTexts = async (
  key: string,
  type: FormType,
): Promise<BaseFormTexts> => {
  const t = await getTranslations(`zod.forms.${key}.BaseFormTexts`);
  return {
    header: t("header", { type }),
    altToast: t("altToast", { type }),
    descriptionToast: t("descriptionToast", { type }),
    error: t("error", { type }),
    toastAction: t("toastAction"),
  };
};

export const getPostFormTexts = async (type: FormType) => {
  const [
    postSchemaTexts,
    fieldTexts,
    titleBodyTexts,
    inputMultipleSelectorTexts,
    buttonSubmitTexts,
    baseFormTexts,
    loadedImages,
    titleAIGeneratedPopTexts,
    bodyAIGeneratedPopTexts,
    diffusionImagesFormTexts,
    t,
  ] = await Promise.all([
    getPostSchemaTexts(),
    getInputFileText("image"),
    getTitleBodyText(),
    getInputMultipleSelectorTexts("tags"),
    getButtonSubmitTexts(),
    getBaseFormTexts("PostForm", type),
    getUploadingProgressTexts("image"),
    getAIGeneratePopTexts("post", "title"),
    getAIGeneratePopTexts("post", "body"),
    getDiffusionImagesFormTexts(),
    getTranslations("components.forms.PostFormTexts"),
  ]);
  return {
    postSchemaTexts,
    fieldTexts,
    titleBodyTexts,
    inputMultipleSelectorTexts,
    buttonSubmitTexts,
    baseFormTexts,
    loadedImages,
    titleAIGeneratedPopTexts,
    bodyAIGeneratedPopTexts,
    diffusionImagesFormTexts,
    aiCheckBoxes: {
      [t("aiCheckBoxes.title.field")]: t("aiCheckBoxes.title.description"),
      [t("aiCheckBoxes.body.field")]: t("aiCheckBoxes.body.description"),
      [t("aiCheckBoxes.tags.field")]: t("aiCheckBoxes.tags.description"),
    },
  };
};

export interface CommentFormTexts extends BaseFormTexts {
  commentSchemaTexts: CommentSchemaTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  titleBodyTexts: TitleBodyTexts;
  baseFormTextsUpdate: BaseFormTexts;
}

export const getCommentFormTexts = async (): Promise<CommentFormTexts> => {
  const [
    commentSchemaTexts,
    buttonSubmitTexts,
    titleBodyTexts,
    baseFormTexts,
    baseFormTextsUpdate,
  ] = await Promise.all([
    getCommentSchemaTexts(),
    getButtonSubmitTexts(),
    getTitleBodyText(),
    getBaseFormTexts("CommentForm", "create"),
    getBaseFormTexts("CommentForm", "update"),
  ]);
  return {
    commentSchemaTexts,
    buttonSubmitTexts,
    titleBodyTexts,
    ...baseFormTexts,
    baseFormTextsUpdate,
  };
};

export interface UpdateProfileTexts {
  updateProfileSchemaTexts: UpdateProfileSchemaTexts;
  fieldTexts: FieldInputTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  error: string;
  firstName: string;
  lastName: string;
}

export const getUpdateProfileTexts = async (): Promise<UpdateProfileTexts> => {
  const [updateProfileSchemaTexts, fieldTexts, buttonSubmitTexts, t] =
    await Promise.all([
      getUpdateProfileSchemaTexts(),
      getInputFileText("image", "single"),
      getButtonSubmitTexts(),
      getTranslations("components.forms.UpdateProfileForm"),
    ]);
  return {
    updateProfileSchemaTexts,
    fieldTexts,
    buttonSubmitTexts,
    error: t("error"),
    firstName: t("firstName"),
    lastName: t("lastName"),
  };
};

export async function getChatMessageFormTexts(): Promise<ChatMessageFormTexts> {
  const [
    buttonSubmitTexts,
    titleBodyTexts,
    titleBodySchemaTexts,
    editorTexts,
    t,
  ] = await Promise.all([
    getButtonSubmitTexts(),
    getTitleBodyText(),
    getTitleBodySchemaTexts(),
    getEditorTexts(),
    getTranslations("components.forms.ChatMessageFormTexts"),
  ]);
  return {
    errorText: t("errorText"),
    buttonText: t("buttonText"),
    buttonSubmitTexts,
    titleBodyTexts,
    titleBodySchemaTexts,
    editorTexts,
  };
}
export async function getIngredientFormTexts(
  type: "create" | "update",
): Promise<IngredientFormTexts> {
  const [
    buttonSubmitTexts,
    baseFormTexts,
    ingredientNutritionalFactSchemaTexts,
    ingredientPieChartTexts,
    t,
  ] = await Promise.all([
    getButtonSubmitTexts(),
    getBaseFormTexts("IngredientForm", type),
    getIngredientNutritionalFactSchemaTexts(),
    getIngredientPieChartTexts(),
    getTranslations("components.forms.IngredientFormTexts"),
  ]);
  const macrosTexts = macroKeys.reduce<
    Record<(typeof macroKeys)[number], FieldTexts>
  >(
    (acc, key) => ({
      ...acc,
      [key]: {
        label: t(`macrosTexts.${key}.label`),
        placeholder: t(`macrosTexts.${key}.placeholder`),
      },
    }),
    {} as Record<(typeof macroKeys)[number], FieldTexts>,
  );

  return {
    name: {
      label: t("name.label"),
      placeholder: t("name.placeholder"),
    },
    unitType: {
      label: t("unitType.label"),
      placeholder: t("unitType.placeholder"),
      description: t("unitType.description"),
    },
    dietType: {
      label: t("dietType.label"),
      placeholder: t("dietType.placeholder"),
    },
    buttonSubmitTexts,
    ingredientPieChartTexts,
    ingredientNutritionalFactSchemaTexts,
    ...baseFormTexts,
    titleTaken: t("titleTaken"),
    disableTooltip: t("disableTooltip"),
    macrosTexts,
  };
}

export async function getChildInputMultipleSelectorTexts(
  type: "ingredients" | "recipes" | "meals" | "days",
): Promise<ChildInputMultipleSelectorTexts> {
  const t = await getTranslations(
    "zod.forms.components.ChildInputMultipleSelectorTexts",
  );
  const intlType = t("type." + type);
  return {
    noResults: t("noResults"),
    loading: t("loading", { type: intlType }),
    placeholder: t("placeholder", { type: intlType }),
  };
}

export async function getRecipeFormTexts(
  type: FormType,
): Promise<RecipeFormTexts> {
  const [
    ingredientQuantitySchemaTexts,
    recipeSchemaTexts,
    ingredientPieChartTexts,
    imagesText,
    videosText,
    buttonSubmitTexts,
    baseFormTexts,
    titleBodyTexts,
    childInputMultipleSelectorTexts,
    loadedImages,
    loadedVideos,
    titleAIGeneratedPopTexts,
    bodyAIGeneratedPopTexts,
    diffusionImagesFormTexts,
    t,
  ] = await Promise.all([
    getIngredientQuantitySchemaTexts(),
    getRecipeSchemaTexts(),
    getIngredientPieChartTexts(),
    getInputFileText("image", "multiple"),
    getInputFileText("video", "multiple"),
    getButtonSubmitTexts(),
    getBaseFormTexts("RecipeForm", type),
    getTitleBodyText(),
    getChildInputMultipleSelectorTexts("ingredients"),
    getUploadingProgressTexts("image"),
    getUploadingProgressTexts("video"),
    getAIGeneratePopTexts("recipe", "title"),
    getAIGeneratePopTexts("recipe", "body"),
    getDiffusionImagesFormTexts(),
    getTranslations("components.forms.RecipeFormTexts"),
  ]);

  return {
    ingredientQuantitySchemaTexts,
    recipeSchemaTexts,
    ingredientPieChartTexts,
    imagesText,
    videosText,
    buttonSubmitTexts,
    baseFormTexts,
    titleBodyTexts,
    childInputMultipleSelectorTexts,
    loadedImages,
    loadedVideos,
    titleAIGeneratedPopTexts,
    bodyAIGeneratedPopTexts,
    diffusionImagesFormTexts,
    aiCheckBoxes: {
      [t("aiCheckBoxes.title.field")]: t("aiCheckBoxes.title.description"),
      [t("aiCheckBoxes.body.field")]: t("aiCheckBoxes.body.description"),
      [t("aiCheckBoxes.ingredients.field")]: t(
        "aiCheckBoxes.ingredients.description",
      ),
    },
    error: t("error", { type }),
    addIngredient: t("addIngredient"),
    ingredientsLabel: t("ingredientsLabel"),
    removeChild: t("removeChild"),
    submitChild: t("submitChild"),
    ingredientChildLabel: t("ingredientChildLabel"),
    clearChild: t("clearChild"),
    quantityChildLabel: t("quantityChildLabel"),
    quantityChildPlaceholder: t("quantityChildPlaceholder"),
    dietType: t("dietType"),
    areIngredientsCompletedButNotSubmitted: t(
      "areIngredientsCompletedButNotSubmitted",
    ),
    continueBtn: t("continueBtn"),
  };
}

export async function getDayFromTexts(type: FormType): Promise<DayFromTexts> {
  const [
    titleBodyTexts,
    mealsChildInputMultipleSelectorTexts,
    recipesChildInputMultipleSelectorTexts,
    buttonSubmitTexts,
    baseFormTexts,
    daySchemaTexts,
    mealSchemaTexts,
    titleAIGeneratedPopTexts,
    bodyAIGeneratedPopTexts,
    t,
  ] = await Promise.all([
    getTitleBodyText(),
    getChildInputMultipleSelectorTexts("meals"),
    getChildInputMultipleSelectorTexts("recipes"),
    getButtonSubmitTexts(),
    getBaseFormTexts("DayForm", type),
    getDaySchemaTexts(),
    getMealSchemaTexts(),
    getAIGeneratePopTexts("day", "title"),
    getAIGeneratePopTexts("day", "body"),
    getTranslations("components.forms.DayFormTexts"),
  ]);

  return {
    titleBodyTexts,
    mealsChildInputMultipleSelectorTexts,
    recipesChildInputMultipleSelectorTexts,
    buttonSubmitTexts,
    baseFormTexts,
    daySchemaTexts,
    mealSchemaTexts,
    typeLabel: t("typeLabel"),
    typePlaceholder: t("typePlaceholder"),
    mealsLabel: t("mealsLabel"),
    addMeal: t("addMeal"),
    hourLabel: t("hourLabel"),
    hourPlaceholder: t("hourPlaceholder"),
    hourDescription: t("hourDescription"),
    minuteLabel: t("minuteLabel"),
    minutePlaceholder: t("minutePlaceholder"),
    minuteDescription: t("minuteDescription"),
    recipesLabel: t("recipesLabel"),
    submitMeal: t("submitMeal"),
    editMeal: t("editMeal"),
    removeMeal: t("removeMeal"),
    mealType: t("mealType"),
    dayDietType: t("dayDietType"),
    areMealsCompletedButNotSubmitted: t("areMealsCompletedButNotSubmitted"),
    continueBtn: t("continueBtn"),
    dayTypesLabels: dayTypes.reduce(
      (acc, cur) => ({ ...acc, [cur]: t(`dayTypes.${cur}`) }),
      {} as Record<DayType, string>,
    ),
    titleAIGeneratedPopTexts,
    bodyAIGeneratedPopTexts,
    aiCheckBoxes: {
      [t("aiCheckBoxes.title.field")]: t("aiCheckBoxes.title.description"),
      [t("aiCheckBoxes.body.field")]: t("aiCheckBoxes.body.description"),
      [t("aiCheckBoxes.type.field")]: t("aiCheckBoxes.type.description"),
      [t("aiCheckBoxes.recipes.field")]: t("aiCheckBoxes.recipes.description"),
    },
  };
}

export async function getPlanFormTexts(type: FormType): Promise<PlanFormTexts> {
  const [
    titleBodyTexts,
    childInputMultipleSelectorTexts,
    imagesText,
    buttonSubmitTexts,
    planSchemaTexts,
    baseFormTexts,
    loadedImages,
    titleAIGeneratedPopTexts,
    bodyAIGeneratedPopTexts,
    diffusionImagesFormTexts,
    t,
  ] = await Promise.all([
    getTitleBodyText(),
    getChildInputMultipleSelectorTexts("days"),
    getInputFileText("image"),
    getButtonSubmitTexts(),
    getPlanSchemaTexts(),
    getBaseFormTexts("PlanForm", type),
    getUploadingProgressTexts("image"),
    getAIGeneratePopTexts("day", "title"),
    getAIGeneratePopTexts("day", "body"),
    getDiffusionImagesFormTexts(),
    getTranslations("components.forms.PlanFormTexts"),
  ]);

  return {
    titleBodyTexts,
    childInputMultipleSelectorTexts,
    imagesText,
    buttonSubmitTexts,
    planSchemaTexts,
    baseFormTexts,
    loadedImages,
    diffusionImagesFormTexts,
    pricePlaceholder: t("pricePlaceholder"),
    priceLabel: t("priceLabel"),
    dietMessage: t("dietMessage"),
    daysLabel: t("daysLabel"),
    daysPlaceholder: t("daysPlaceholder"),
    objectiveLabel: t("objectiveLabel"),
    objectivePlaceholder: t("objectivePlaceholder"),
    dayIndex: t("dayIndex"),
    objectives: planObjectives.reduce(
      (acc, k) => ({ ...acc, [k]: t(`objectives.${k}`) }),
      {} as Record<(typeof planObjectives)[number], string>,
    ),
    titleAIGeneratedPopTexts,
    bodyAIGeneratedPopTexts,
    aiCheckBoxes: {
      [t("aiCheckBoxes.title.field")]: t("aiCheckBoxes.title.description"),
      [t("aiCheckBoxes.body.field")]: t("aiCheckBoxes.body.description"),
      [t("aiCheckBoxes.objective.field")]: t(
        "aiCheckBoxes.objective.description",
      ),
      [t("aiCheckBoxes.days.field")]: t("aiCheckBoxes.days.description"),
    },
  };
}

export async function getCheckoutDrawerTexts(): Promise<CheckoutDrawerTexts> {
  const [buttonSubmitTexts, t] = await Promise.all([
    getButtonSubmitTexts(),
    getTranslations("components.forms.CheckoutDrawerTexts"),
  ]);

  return {
    buttonSubmitTexts,
    error: t("error"),
    anchor: t("anchor"),
    title: t("title"),
    header: t("header"),
    label: t("label"),
    description: t("description"),
    placeholder: t("placeholder"),
  };
}

export async function getAdminEmailTexts(): Promise<AdminEmailTexts> {
  const [adminEmailSchemaTexts, buttonSubmitTexts, editorTexts, t] =
    await Promise.all([
      getAdminEmailSchemaTexts(),
      getButtonSubmitTexts(),
      getEditorTexts(),
      getTranslations("components.forms.AdminEmailTexts"),
    ]);
  return {
    adminEmailSchemaTexts,
    buttonSubmitTexts,
    editorTexts,
    title: t("title"),
    error: t("error"),
    preview: t("preview"),
    toastDescription: t("toastDescription"),
    mxError: t("mxError"),
    emailDescription: t("emailDescription"),
    items: {
      ...["email", "subject", "content"].reduce(
        (acc, key) => ({
          ...acc,
          [key]: {
            label: t(`items.${key}.label`),
            placeholder: t(`items.${key}.placeholder`),
          },
        }),
        {} as Record<
          ["email", "subject", "content"][number],
          { label: string; placeholder: string }
        >,
      ),
    },
  };
}
export async function getAIGeneratePopTexts(
  item: "post" | "recipe" | "plan" | "day",
  thing: "title" | "body",
): Promise<AIGeneratePopTexts> {
  const [buttonSubmitTexts, t] = await Promise.all([
    getButtonSubmitTexts(),
    getTranslations("components.forms.AIGeneratePopTexts"),
  ]);
  return {
    buttonSubmitTexts,
    anchorText: t("anchorText"),
    descriptionText: t("descriptionText", {
      item: t("item." + item),
      thing: t("thing." + thing),
    }),
    toxicError: t("toxicError"),
    englishError: t("englishError"),
    forFunText: t("forFunText"),
    forFunBtnText: t("forFunBtnText"),
    placeholder: t("placeholder"),
  };
}

export async function getDiffusionImagesFormTexts(): Promise<DiffusionImagesFormTexts> {
  const [schemaTexts, t] = await Promise.all([
    getDiffusionSchemaTexts(),
    getTranslations("components.forms.DiffusionImagesFormTexts"),
  ]);
  return {
    schemaTexts,
    numImagesLabel: t("numImagesLabel"),
    toxicError: t("toxicError"),
    negativePromptLabel: t("negativePromptLabel"),
    buttonSubmitText: {
      loadingText: t("buttonSubmitText.loadingText"),
      submitText: t("buttonSubmitText.submitText"),
    },
    triggerText: t("triggerText"),
    promptLabel: t("promptLabel"),
    promptPlaceholder: t("promptPlaceholder"),
    promptDescription: t("promptDescription"),
    negativePromptPlaceholder: t("negativePromptPlaceholder"),
    negativePromptDescription: t("negativePromptDescription"),
    englishError: t("englishError"),
  };
}
export async function getUseNavigationGuardI18nTexts(): Promise<UseNavigationGuardI18nTexts> {
  const t = await getTranslations(
    "components.forms.UseNavigationGuardI18nTexts",
  );
  return {
    confirm: t("confirm"),
  };
}

export async function getPasswordStrengthIndicatorTexts(): Promise<PasswordStrengthIndicatorTexts> {
  const t = await getTranslations(
    "components.forms.PasswordStrengthIndicatorTexts",
  );
  return {
    weak: t("weak"),
    medium: t("medium"),
    strong: t("strong"),
    strength: t("strength"),
    hasLetter: t("hasLetter"),
    hasNumber: t("hasNumber"),
    hasSpecialChar: t("hasSpecialChar"),
    minLength: t("minLength"),
  };
}

export async function getAnswerFromBodyFormTexts(): Promise<AnswerFromBodyFormTexts> {
  const [t, schemaTexts, buttonSubmitTexts] = await Promise.all([
    getTranslations("components.forms.AnswerFromBodyFormTexts"),
    getAnswerFromBodySchemaTexts(),
    getButtonSubmitTexts(),
  ]);

  return {
    schemaTexts,
    toxicError: t("toxicError"),
    englishError: t("englishError"),
    answerError: t("answerError"),
    questionLabel: t("questionLabel"),
    kLabel: t("kLabel"),
    questionPlaceholder: t("questionPlaceholder"),
    kPlaceholder: t("kPlaceholder"),
    disclaimerText: t("disclaimerText"),
    triggerText: t("triggerText"),
    matchScore: t("matchScore"),
    buttonSubmitTexts,
  };
}
export async function getCalendarDayFormTexts(): Promise<CalendarDayFormTexts> {
  const [t, buttonSubmitTexts, childInputMultipleSelectorTexts] =
    await Promise.all([
      getTranslations("components.forms.CalendarDayFormTexts"),
      getButtonSubmitTexts(),
      getChildInputMultipleSelectorTexts("days"),
    ]);
  return {
    buttonSubmitTexts,
    childInputMultipleSelectorTexts,
    title: t("title"),
    error: t("error"),
    placeholder: t("placeholder"),
    toastDescription: t("toastDescription"),
    description: t("description"),
  };
}

export async function getAddDayToCalendarTexts(): Promise<AddDayToCalendarTexts> {
  const [addDayToCalendarSchemaTexts, buttonSubmitTexts, t] = await Promise.all(
    [
      getAddDayToCalendarSchemaTexts(),
      getButtonSubmitTexts(),
      getTranslations("components.forms.AddDayToCalendarTexts"),
    ],
  );

  return {
    addDayToCalendarSchemaTexts,
    buttonSubmitTexts,
    errorText: t("errorText"),
    anchorText: t("anchorText"),
    titleText: t("titleText"),
    formPlaceholder: t("formPlaceholder"),
    formLabel: t("formLabel"),
    formDescription: t("formDescription"),
    toastDescription: t("toastDescription"),
    description: t("description"),
  };
}
export async function getEmailFromFieldTexts(): Promise<EmailFromFieldTexts> {
  const t = await getTranslations("components.forms.EmailFromFieldTexts");
  return {
    label: t("label"),
    description: t("description"),
  };
}
