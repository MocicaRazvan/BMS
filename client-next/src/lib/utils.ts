import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";
import { SortDirection, sortDirections } from "@/types/fetch-utils";
import { Session } from "next-auth";
import { ApproveDto, DietType, WithUserDto } from "@/types/dto";
import { SortingOption } from "@/components/list/grid-list";
import { Dispatch, ReactNode, SetStateAction } from "react";
import { getTimezoneOffset, toZonedTime } from "date-fns-tz";
import { formatDistanceToNow } from "date-fns";
import { enUS, ro, Locale as DateFnsLocale } from "date-fns/locale";
import { Locale } from "@/navigation";
import isEqual from "lodash.isequal";
import { stripHtml } from "string-strip-html";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function isDeepEqual<T>(obj1: T, obj2: T): boolean {
  if (obj1 === null && obj2 !== null) return false;
  if (obj1 !== null && obj2 === null) return false;
  return isEqual(obj1, obj2);
}

function isObject(value: any): value is object {
  return value !== null && typeof value === "object";
}

export function makeSortFetchParams(sort: SortingOption[]) {
  return sort.reduce<Record<string, SortDirection>>(
    (acc, { property, direction }) => {
      if (direction !== "none") {
        acc[property] = direction;
      }
      return acc;
    },
    {},
  );
}

export function makeSortString(sort: Record<string | number | symbol, string>) {
  return Object.entries(sort)
    .map(([key, value]) => `${key}:${value}`)
    .join(",");
}

export function parseSortString<T extends string>(
  sortStr: string | null,
  options: SortingOption[],
) {
  if (!sortStr) return [];
  const validKeys = options.map((option) => option.property);
  return sortStr
    .split(",")
    .map((part) => part.split(":") as [T, Exclude<SortDirection, "none">])
    .filter(
      ([key, value]) =>
        validKeys.includes(key) &&
        sortDirections.filter((v) => v !== "none").includes(value),
    )
    .reduce<SortingOption[]>((acc, [key, val]) => {
      const foundOption = options.find(
        (o) => o.property === key && o.direction === val,
      );
      if (foundOption) {
        acc.push(foundOption);
      }
      return acc;
    }, []);
}

export function parseAndValidateNumbers(
  input: string | null,
  errorMessage: string,
  callback: () => never,
): number[] {
  if (!input) {
    callback();
  }
  return input.split(",").map((item) => {
    const number = Number(item);
    if (Number.isNaN(number)) callback();
    return number;
  });
}

export function parseQueryParamAsInt(
  paramValue: string | null,
  defaultValue: number,
): number;
export function parseQueryParamAsInt(
  paramValue: string | null,
  defaultValue: null,
): number | null;

export function parseQueryParamAsInt(
  paramValue: string | null,
  defaultValue: number | null,
): number | null {
  const parsedValue = parseInt(paramValue || "", 10);
  return isNaN(parsedValue) ? defaultValue : parsedValue;
}

export function getCSSVariableValue(variableName: string) {
  const style = getComputedStyle(document.documentElement);
  const isDarkMode = document.documentElement.classList.contains("dark");
  const modePrefix = isDarkMode ? "dark-" : "";
  return style.getPropertyValue(`--${modePrefix}${variableName}`).trim();
}

export function roundToDecimalPlaces(value: number, decimalPlaces: number) {
  return Math.round(value * 10 ** decimalPlaces) / 10 ** decimalPlaces;
}

export function addOnlyUnique<T>(
  arr: T[],
  item: T,
  comparator: (a: T, b: T) => boolean,
  filter?: (a: T) => boolean,
): T[] {
  if (filter) {
    arr = arr.filter(filter);
  }
  if (!arr.some((el) => comparator(el, item))) {
    arr.push(item);
  }
  return arr;
}
type SuccessCheckReturn = {
  isAdmin: boolean;
  isOwner: boolean;
  isOwnerOrAdmin: boolean;
};
type CheckReturn = SuccessCheckReturn | ReactNode;

export function isSuccessCheckReturn(
  privilegeReturn: CheckReturn,
): privilegeReturn is SuccessCheckReturn {
  return (
    typeof privilegeReturn === "object" &&
    privilegeReturn !== null &&
    "isOwnerOrAdmin" in privilegeReturn &&
    "isAdmin" in privilegeReturn &&
    "isOwner" in privilegeReturn
  );
}
export function checkApprovePrivilege(
  authUser: NonNullable<Session["user"]>,
  entity: ApproveDto,
  errorCallback: () => ReactNode,
): CheckReturn {
  const isAdmin = authUser.role === "ROLE_ADMIN";
  const isOwner = entity?.userId === parseInt(authUser.id);
  const isOwnerOrAdmin = isOwner || isAdmin;

  if (!entity.approved && !isOwnerOrAdmin) {
    return errorCallback();
  }
  return {
    isAdmin,
    isOwner,
    isOwnerOrAdmin,
  };
}

export function checkOwnerOrAdmin(
  authUser: NonNullable<Session["user"]>,
  entity: WithUserDto,
  errorCallback: () => ReactNode,
): CheckReturn {
  if (
    entity.userId !== parseInt(authUser.id) &&
    authUser.role !== "ROLE_ADMIN"
  ) {
    // notFound();
    return errorCallback();
  }
  const isAdmin = authUser.role === "ROLE_ADMIN";
  const isOwner = entity.userId === parseInt(authUser.id);

  return {
    isAdmin,
    isOwner,
    isOwnerOrAdmin: isOwner || isAdmin,
  };
}

export function checkOwner(
  authUser: NonNullable<Session["user"]>,
  entity: WithUserDto,
  errorCallback: () => ReactNode,
):
  | {
      success: boolean;
    }
  | ReactNode {
  if (entity.userId !== parseInt(authUser.id)) {
    return errorCallback();
  }
  return {
    success: true,
  };
}

export function handleBaseError(
  e: unknown,
  setErrorMsg: Dispatch<SetStateAction<string>>,
  errorText = "Something went wrong",
) {
  if (e && typeof e === "object") {
    if ("message" in e && typeof e.message === "string") {
      setErrorMsg(e.message);
    } else if ("error" in e && typeof e.error === "string") {
      setErrorMsg(e.error);
    }
  } else {
    setErrorMsg(errorText);
  }
}

export function parseStringToBoolean(
  str: string | null | undefined,
): boolean | null {
  if (str === null || str === undefined) {
    return null;
  }

  const lowerStr = str.toLowerCase().trim();

  if (lowerStr === "true") {
    return true;
  } else if (lowerStr === "false") {
    return false;
  }

  return null;
}
export function determineMostRestrictiveDiet(
  dietTypes: DietType[],
): DietType | null {
  if (dietTypes.length === 0) return null;
  if (dietTypes.includes("OMNIVORE")) {
    return "OMNIVORE";
  } else if (dietTypes.includes("VEGETARIAN")) {
    return "VEGETARIAN";
  } else {
    return "VEGAN";
  }
}

export function generateUniqueNumber() {
  const timestamp = Date.now();
  const randomNum = Math.floor(Math.random() * 10000);
  return timestamp * 10000 + randomNum;
}

export function formatFromUtc(utcDate: Date, userTimeZone: string) {
  const zonedDate = toZonedTime(utcDate, userTimeZone);
  const offset = getTimezoneOffset(userTimeZone, zonedDate);
  return new Date(zonedDate.getTime() + offset);
}

export function fromDistanceToNowUtc(
  utcDate: Date,
  userTimeZone: string,
  locale: Locale,
) {
  return formatDistanceToNow(formatFromUtc(utcDate, userTimeZone), {
    addSuffix: true,
    locale: locale === "ro" ? ro : enUS,
  });
}

export const fromStringOfDotToObjectValue = <T extends object>(
  str: string,
  obj: T,
  lastIsLength = false,
) => {
  const keys = str.split(".");

  return keys.reduce((value, key, index) => {
    if (value && typeof value === "object" && key in value) {
      const currentValue = value[key as keyof typeof value];

      if (index === keys.length - 1 && lastIsLength) {
        return (currentValue as any[])?.length;
      }

      return currentValue;
    }

    return undefined;
  }, obj as any);
};

export const appendCreatedAtDesc = (url: string) =>
  url.includes("?")
    ? `${url}&sort=createdAt:desc`
    : `${url}?sort=createdAt:desc`;

export function truncate(text: string, length: number) {
  return text.length > length ? text.substring(0, length) + "..." : text;
}
export function trimString(title: string) {
  return title
    .trim()
    .replace(/^[`"']+/, "")
    .replace(/^[`"']+/, "")
    .replace(/^[`"']+/, "")
    .replace(/(\r\n|\n|\r)/gm, "")
    .replace(/^'"|"'$/g, "")
    .replace(/^[`"']+|[`"']+$/g, "")
    .replace(/^['"]+|['"]+$/g, "")
    .replace(/"/g, "")
    .replaceAll('"', "");
}

export function trimHTML(html: string) {
  return html.trim().replaceAll("*", "");
}
export function normalizeText<T>(text: T): T {
  if (typeof text === "string") {
    return text
      .toLowerCase()
      .replace(/\s+/g, " ")
      .replace(/[\u200B-\u200D\uFEFF]/g, "")
      .normalize("NFKC")
      .trim() as T;
  }
  return text;
}
export const toShuffleArray = <T>(array: T[]) => {
  const copiedArray: T[] = JSON.parse(JSON.stringify(array));
  for (let i = copiedArray.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [copiedArray[i], copiedArray[j]] = [copiedArray[j], copiedArray[i]];
  }
  return copiedArray;
};

export function choseRandomNumber(min: number, max: number) {
  return Math.floor(Math.random() * (max - min + 1) + min);
}

export function choseRandomItemsFromArray<T>(array: T[], count = 1) {
  return toShuffleArray(array).slice(0, count);
}
export function formatChartValue(value: number | string) {
  return typeof value === "number" && !Number.isInteger(value)
    ? value.toFixed(2)
    : value;
}

export const wrapItemToString = (i: unknown) => `${i}`;
export function removeHTML(html: string) {
  return stripHtml(html).result.replace(/\s+/g, " ").trim();
}
export const dateFnsLocaleMapper: Record<Locale, DateFnsLocale> = {
  en: enUS,
  ro: ro,
};
