import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";
import { SortDirection, sortDirections } from "@/types/fetch-utils";
import { Session } from "next-auth";
import { ApproveDto, DietType, WithUserDto } from "@/types/dto";
import { notFound } from "next/navigation";
import { SortingOption } from "@/components/list/grid-list";
import { Dispatch, SetStateAction } from "react";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function isDeepEqual<T>(obj1: T, obj2: T): boolean {
  if (obj1 === obj2) {
    return true;
  }

  if (
    typeof obj1 !== "object" ||
    typeof obj2 !== "object" ||
    obj1 === null ||
    obj2 === null
  ) {
    return obj1 === obj2;
  }

  if (obj1 instanceof Date && obj2 instanceof Date) {
    return obj1.getTime() === obj2.getTime();
  }

  if (Array.isArray(obj1) && Array.isArray(obj2)) {
    if (obj1.length !== obj2.length) {
      return false;
    }
    for (let i = 0; i < obj1.length; i++) {
      if (!isDeepEqual(obj1[i], obj2[i])) {
        return false;
      }
    }
    return true;
  }

  if (obj1 instanceof Map && obj2 instanceof Map) {
    if (obj1.size !== obj2.size) {
      return false;
    }
    for (const [key, value] of obj1) {
      if (!obj2.has(key) || !isDeepEqual(value, obj2.get(key))) {
        return false;
      }
    }
    return true;
  }

  if (obj1 instanceof Set && obj2 instanceof Set) {
    if (obj1.size !== obj2.size) {
      return false;
    }
    for (const item of obj1) {
      if (!obj2.has(item)) {
        return false;
      }
    }
    return true;
  }

  const keys1 = Object.keys(obj1) as Array<keyof T>;
  const keys2 = Object.keys(obj2) as Array<keyof T>;

  if (keys1.length !== keys2.length) {
    return false;
  }

  for (const key of keys1) {
    const val1 = obj1[key];
    const val2 = obj2[key];

    const areObjects = isObject(val1) && isObject(val2);
    if (
      (areObjects && !isDeepEqual(val1, val2)) ||
      (!areObjects && val1 !== val2)
    ) {
      return false;
    }
  }

  return true;
}

// Helper function to determine if a value is an object and not null
function isObject(value: any): value is object {
  return value !== null && typeof value === "object";
}

// export function makeSortFetchParams(
//   sort: Record<string | number | symbol, string>,
// ) {
//   return Object.fromEntries(
//     Object.entries(sort).filter(([_, value]) => value !== "none"),
//   );
// }

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

// export function parseSortString<T extends string>(
//   sortStr: string,
//   validKeys: T[],
// ) {
//   return sortStr
//     .split(",")
//     .map((part) => part.split(":") as [T, SortDirection])
//     .filter(
//       ([key, value]) =>
//         validKeys.includes(key) && sortDirections.includes(value),
//     )
//     .reduce<Record<(typeof validKeys)[number], SortDirection>>(
//       (acc, [key, value]) => {
//         acc[key] = value;
//         return acc;
//       },
//       {} as Record<(typeof validKeys)[number], SortDirection>,
//     );
// }

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

export function checkApprovePrivilege(
  authUser: NonNullable<Session["user"]>,
  entity: ApproveDto,
) {
  const isAdmin = authUser.role === "ROLE_ADMIN";
  const isOwner = entity?.userId === parseInt(authUser.id);
  const isOwnerOrAdmin = isOwner || isAdmin;

  if (!entity.approved && !isOwnerOrAdmin) {
    notFound();
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
) {
  if (
    entity.userId !== parseInt(authUser.id) &&
    authUser.role !== "ROLE_ADMIN"
  ) {
    notFound();
  }
  const isAdmin = authUser.role === "ROLE_ADMIN";
  const isOwner = entity.userId === parseInt(authUser.id);

  return {
    isAdmin,
    isOwner,
  };
}

export function checkOwner(
  authUser: NonNullable<Session["user"]>,
  entity: ApproveDto,
) {
  if (entity.userId !== parseInt(authUser.id)) {
    notFound();
  }
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
  // if (dietTypes.length === 0) {
  //   return "OMNIVORE";
  // }
  //
  // const dietHierarchy: Record<DietType, number> = {
  //   VEGAN: 1,
  //   VEGETARIAN: 2,
  //   // CARNIVORE: 3,
  //   OMNIVORE: 4,
  // };
  //
  // let mostRestrictiveDiet: DietType = "OMNIVORE";
  //
  // dietTypes.forEach((diet) => {
  //   if (dietHierarchy[diet] < dietHierarchy[mostRestrictiveDiet]) {
  //     mostRestrictiveDiet = diet;
  //   }
  // });
  //
  // return mostRestrictiveDiet;
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
