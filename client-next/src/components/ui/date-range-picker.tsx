/* eslint-disable max-lines */
"use client";
import {
  type FC,
  ReactElement,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { Button, ButtonProps } from "./button";
import { Popover, PopoverContent, PopoverTrigger } from "./popover";
import { Calendar } from "./calendar";
import { DateInput } from "./date-input";
import { Label } from "./label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "./select";
import { Switch } from "./switch";
import {
  CheckIcon,
  ChevronDownIcon,
  ChevronUpIcon,
} from "@radix-ui/react-icons";
import { cn } from "@/lib/utils";
import { format, Locale } from "date-fns";
import { enUS } from "date-fns/locale";
import { CalendarIcon } from "lucide-react";

export type DateRangeParams = Record<"from" | "to", string>;
const formatDate = (date: Date, locale: Locale = enUS): string => {
  return format(date, "P", { locale });
};

const getDateAdjustedForTimezone = (dateInput: Date | string): Date => {
  if (typeof dateInput === "string") {
    // Split the date string to get year, month, and day parts
    const parts = dateInput.split("-").map((part) => parseInt(part, 10));
    // Create a new Date object using the local timezone
    // Note: Month is 0-indexed, so subtract 1 from the month part
    const date = new Date(parts[0], parts[1] - 1, parts[2]);
    return date;
  } else {
    // If dateInput is already a Date object, return it directly
    return dateInput;
  }
};

export interface DateRange {
  from: Date;
  to: Date | undefined;
}

interface Preset {
  name: string;
  label: string;
}

// Define presets
const BASE_PRESETS: Preset[] = [
  { name: "today", label: "" },
  { name: "yesterday", label: "" },
  { name: "last7", label: "" },
  { name: "last14", label: " " },
  { name: "last30", label: "" },
  { name: "thisWeek", label: "" },
  { name: "lastWeek", label: "" },
  { name: "thisMonth", label: "" },
  { name: "lastMonth", label: "" },
  { name: "pastYear", label: "" },
];

export interface DateRangePickerTexts {
  compare: string;
  cancel: string;
  update: string;
  presets: Record<(typeof BASE_PRESETS)[number]["name"], string>;
  none: {
    false: string;
    true: string;
  };
  noRangeSelected: string;
}

export interface DateRangePickerProps {
  /** Click handler for applying the updates from DateRangePicker. */
  onUpdate?: (
    values: { range: DateRange; rangeCompare?: DateRange },
    none?: boolean,
  ) => void;
  /** Initial value for start date */
  initialDateFrom?: Date | string;
  /** Initial value for end date */
  initialDateTo?: Date | string;
  /** Initial value for start date for compare */
  initialCompareFrom?: Date | string;
  /** Initial value for end date for compare */
  initialCompareTo?: Date | string;
  /** Alignment of popover */
  align?: "start" | "center" | "end";
  /** Option for locale */
  locale?: Locale;
  /** Option for showing compare feature */
  showCompare?: boolean;
  defaultPreset?: (typeof BASE_PRESETS)[number]["name"];
  hiddenPresets?: (typeof BASE_PRESETS)[number]["name"][];
  showNone?: boolean;
  defaultNone?: boolean;
  triggerVariant?: ButtonProps["variant"];
  triggerClassName?: string;
}

/** The DateRangePicker component allows a user to select a range of dates */
export const DateRangePicker: FC<
  DateRangePickerProps & DateRangePickerTexts
> & {
  filePath: string;
} = ({
  initialDateFrom = new Date(new Date().setHours(0, 0, 0, 0)),
  initialDateTo,
  initialCompareFrom,
  initialCompareTo,
  onUpdate,
  align = "end",
  locale = enUS,
  showCompare = true,
  compare,
  cancel,
  presets,
  update,
  defaultPreset,
  hiddenPresets = [],
  showNone,
  defaultNone = false,
  noRangeSelected,
  triggerVariant = "outline",
  triggerClassName,
  none: { false: noneFalseText, true: noneTrueText },
}): ReactElement => {
  const defaultInitialRangeRef = useRef<DateRange>({
    from: getDateAdjustedForTimezone(initialDateFrom),
    to: initialDateTo
      ? getDateAdjustedForTimezone(initialDateTo)
      : getDateAdjustedForTimezone(initialDateFrom),
  });

  const [none, setNone] = useState(defaultNone);
  const prevNone = useRef(defaultNone);

  const toggleNone = useCallback(() => {
    setNone((prev) => {
      prevNone.current = prev;
      return !prev;
    });
  }, []);

  const [isOpen, setIsOpen] = useState(false);

  const [range, setRange] = useState<DateRange>(defaultInitialRangeRef.current);

  const PRESETS = useMemo(
    () =>
      BASE_PRESETS.filter(
        (preset) => !hiddenPresets?.includes(preset.name),
      ).map((preset) => ({
        ...preset,
        label: presets[preset.name],
      })),
    [hiddenPresets, presets],
  );
  const getPresetRange = useCallback(
    (presetName: string): DateRange => {
      const preset = PRESETS.find(({ name }) => name === presetName);
      if (!preset) throw new Error(`Unknown date range preset: ${presetName}`);
      const from = new Date();
      const to = new Date();
      const first = from.getDate() - from.getDay();

      switch (preset.name) {
        case "today":
          from.setHours(0, 0, 0, 0);
          to.setHours(23, 59, 59, 999);
          break;
        case "yesterday":
          from.setDate(from.getDate() - 1);
          from.setHours(0, 0, 0, 0);
          to.setDate(to.getDate() - 1);
          to.setHours(23, 59, 59, 999);
          break;
        case "last7":
          from.setDate(from.getDate() - 6);
          from.setHours(0, 0, 0, 0);
          to.setHours(23, 59, 59, 999);
          break;
        case "last14":
          from.setDate(from.getDate() - 13);
          from.setHours(0, 0, 0, 0);
          to.setHours(23, 59, 59, 999);
          break;
        case "last30":
          from.setDate(from.getDate() - 29);
          from.setHours(0, 0, 0, 0);
          to.setHours(23, 59, 59, 999);
          break;
        case "thisWeek":
          from.setDate(first);
          from.setHours(0, 0, 0, 0);
          to.setHours(23, 59, 59, 999);
          break;
        case "lastWeek":
          from.setDate(from.getDate() - 7 - from.getDay());
          to.setDate(to.getDate() - to.getDay() - 1);
          from.setHours(0, 0, 0, 0);
          to.setHours(23, 59, 59, 999);
          break;
        case "thisMonth":
          from.setDate(1);
          from.setHours(0, 0, 0, 0);
          to.setHours(23, 59, 59, 999);
          break;
        case "lastMonth":
          from.setMonth(from.getMonth() - 1);
          from.setDate(1);
          from.setHours(0, 0, 0, 0);
          to.setDate(0);
          to.setHours(23, 59, 59, 999);
          break;
        case "pastYear":
          from.setFullYear(from.getFullYear() - 1);
          from.setHours(0, 0, 0, 0);
          break;
      }

      return { from, to };
    },
    [PRESETS],
  );

  const isJustFromCurrentDate = useMemo(
    () =>
      `${range.from}` === `${getDateAdjustedForTimezone(initialDateFrom)}` &&
      (`${range.from}` === `${range.to}` || range.to === undefined),
    [initialDateFrom, range.from, range.to],
  );

  const [rangeCompare, setRangeCompare] = useState<DateRange | undefined>(
    initialCompareFrom
      ? {
          from: new Date(new Date(initialCompareFrom).setHours(0, 0, 0, 0)),
          to: initialCompareTo
            ? new Date(new Date(initialCompareTo).setHours(0, 0, 0, 0))
            : new Date(new Date(initialCompareFrom).setHours(0, 0, 0, 0)),
        }
      : undefined,
  );

  // Refs to store the values of range and rangeCompare when the date picker is opened
  const openedRangeRef = useRef<DateRange | undefined>();
  const openedRangeCompareRef = useRef<DateRange | undefined>();

  const [selectedPreset, setSelectedPreset] = useState<string | undefined>(
    undefined,
  );

  const [isSmallScreen, setIsSmallScreen] = useState(
    typeof window !== "undefined" ? window.innerWidth < 960 : false,
  );

  useEffect(() => {
    const handleResize = (): void => {
      setIsSmallScreen(window.innerWidth < 960);
    };

    window.addEventListener("resize", handleResize);

    // Clean up event listener on unmount
    return () => {
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  const setPreset = (preset: string): void => {
    const range = getPresetRange(preset);
    setRange(range);
    if (rangeCompare) {
      const rangeCompare = {
        from: new Date(
          range.from.getFullYear() - 1,
          range.from.getMonth(),
          range.from.getDate(),
        ),
        to: range.to
          ? new Date(
              range.to.getFullYear() - 1,
              range.to.getMonth(),
              range.to.getDate(),
            )
          : undefined,
      };
      setRangeCompare(rangeCompare);
    }
  };

  const checkPreset = (): void => {
    for (const preset of PRESETS) {
      const presetRange = getPresetRange(preset.name);

      const normalizedRangeFrom = new Date(range.from);
      normalizedRangeFrom.setHours(0, 0, 0, 0);
      const normalizedPresetFrom = new Date(
        presetRange.from.setHours(0, 0, 0, 0),
      );

      const normalizedRangeTo = new Date(range.to ?? 0);
      normalizedRangeTo.setHours(0, 0, 0, 0);
      const normalizedPresetTo = new Date(
        presetRange.to?.setHours(0, 0, 0, 0) ?? 0,
      );

      if (
        normalizedRangeFrom.getTime() === normalizedPresetFrom.getTime() &&
        normalizedRangeTo.getTime() === normalizedPresetTo.getTime()
      ) {
        setSelectedPreset(preset.name);
        return;
      }
    }

    setSelectedPreset(undefined);
  };

  const resetValues = (): void => {
    setRange(defaultInitialRangeRef.current);

    // setRange({
    //   from:
    //     typeof initialDateFrom === "string"
    //       ? getDateAdjustedForTimezone(initialDateFrom)
    //       : initialDateFrom,
    //   to: initialDateTo
    //     ? typeof initialDateTo === "string"
    //       ? getDateAdjustedForTimezone(initialDateTo)
    //       : initialDateTo
    //     : typeof initialDateFrom === "string"
    //       ? getDateAdjustedForTimezone(initialDateFrom)
    //       : initialDateFrom,
    // });
    // setRangeCompare(
    //   initialCompareFrom
    //     ? {
    //         from:
    //           typeof initialCompareFrom === "string"
    //             ? getDateAdjustedForTimezone(initialCompareFrom)
    //             : initialCompareFrom,
    //         to: initialCompareTo
    //           ? typeof initialCompareTo === "string"
    //             ? getDateAdjustedForTimezone(initialCompareTo)
    //             : initialCompareTo
    //           : typeof initialCompareFrom === "string"
    //             ? getDateAdjustedForTimezone(initialCompareFrom)
    //             : initialCompareFrom,
    //       }
    //     : undefined,
    // );
  };

  useEffect(() => {
    checkPreset();
  }, [range]);
  useEffect(() => {
    if (defaultPreset) {
      setPreset(defaultPreset);
      checkPreset();
    }
  }, [defaultPreset]);
  const PresetButton = ({
    preset,
    label,
    isSelected,
    disabled,
  }: {
    preset: string;
    label: string;
    isSelected: boolean;
    disabled?: boolean;
  }): ReactElement => (
    <Button
      className={cn(isSelected && "pointer-events-none")}
      variant="ghost"
      disabled={disabled}
      onClick={() => {
        setPreset(preset);
      }}
    >
      <>
        <span className={cn("pr-2 opacity-0", isSelected && "opacity-70")}>
          <CheckIcon width={18} height={18} />
        </span>
        {label}
      </>
    </Button>
  );

  // Helper function to check if two date ranges are equal
  const areRangesEqual = (a?: DateRange, b?: DateRange): boolean => {
    if (!a || !b) return a === b; // If either is undefined, return true if both are undefined
    return (
      a.from.getTime() === b.from.getTime() &&
      (!a.to || !b.to || a.to.getTime() === b.to.getTime())
    );
  };

  useEffect(() => {
    if (isOpen) {
      openedRangeRef.current = range;
      openedRangeCompareRef.current = rangeCompare;
    }
  }, [isOpen]);

  return (
    <Popover
      modal={false}
      open={isOpen}
      onOpenChange={(open: boolean) => {
        if (!open) {
          resetValues();
        }
        setIsOpen(open);
      }}
    >
      <PopoverTrigger asChild>
        <Button
          size={"lg"}
          variant={triggerVariant}
          className={cn(
            "flex items-center justify-between gap-5 w-80",
            triggerClassName,
          )}
        >
          <CalendarIcon className="h-5 w-5" />
          <div className="text-right ">
            <div
              className={cn(
                !none && "hidden py-0",
                none && "py-1 flex-1 font-semibold",
              )}
            >
              {noRangeSelected}
            </div>
            <div className={cn("py-1 flex-1", none && "hidden py-0")}>
              <div>{`${formatDate(range.from, locale)}${
                range.to != null ? " - " + formatDate(range.to, locale) : ""
              }`}</div>
            </div>
            {rangeCompare != null && (
              <div className="opacity-60 text-xs -mt-1">
                <>
                  {"vs."} {formatDate(rangeCompare.from, locale)}
                  {rangeCompare.to != null
                    ? ` - ${formatDate(rangeCompare.to, locale)}`
                    : ""}
                </>
              </div>
            )}
          </div>
          <div className="pl-1 opacity-60 -mr-2 scale-125">
            {isOpen ? (
              <ChevronUpIcon width={24} />
            ) : (
              <ChevronDownIcon width={24} />
            )}
          </div>
        </Button>
      </PopoverTrigger>
      <PopoverContent
        align={align}
        className="w-auto"
        onOpenAutoFocus={(e) => {
          e.preventDefault();
        }}
      >
        <div className="flex py-2">
          <div className="flex">
            <div className="flex flex-col">
              <div className="flex flex-col lg:flex-row gap-2 px-3 justify-end items-center lg:items-start pb-4 lg:pb-0">
                {showCompare && (
                  <div className="flex items-center space-x-2 pr-4 py-1">
                    <Switch
                      disabled={none}
                      defaultChecked={Boolean(rangeCompare)}
                      onCheckedChange={(checked: boolean) => {
                        if (checked) {
                          if (!range.to) {
                            setRange({
                              from: range.from,
                              to: range.from,
                            });
                          }
                          setRangeCompare({
                            from: new Date(
                              range.from.getFullYear(),
                              range.from.getMonth(),
                              range.from.getDate() - 365,
                            ),
                            to: range.to
                              ? new Date(
                                  range.to.getFullYear() - 1,
                                  range.to.getMonth(),
                                  range.to.getDate(),
                                )
                              : new Date(
                                  range.from.getFullYear() - 1,
                                  range.from.getMonth(),
                                  range.from.getDate(),
                                ),
                          });
                        } else {
                          setRangeCompare(undefined);
                        }
                      }}
                      id="compare-mode"
                    />
                    <Label htmlFor="compare-mode">{compare}</Label>
                  </div>
                )}
                <div className="flex flex-col gap-2">
                  <div className="flex gap-2">
                    <DateInput
                      disabled={none}
                      value={range.from}
                      onChange={(date) => {
                        const toDate =
                          range.to == null || date > range.to ? date : range.to;
                        setRange((prevRange) => ({
                          ...prevRange,
                          from: date,
                          to: toDate,
                        }));
                      }}
                    />
                    <div className="py-1">{"-"}</div>
                    <DateInput
                      disabled={none}
                      value={range.to}
                      onChange={(date) => {
                        const fromDate = date < range.from ? date : range.from;
                        setRange((prevRange) => ({
                          ...prevRange,
                          from: fromDate,
                          to: date,
                        }));
                      }}
                    />
                  </div>
                  {rangeCompare != null && (
                    <div className="flex gap-2">
                      <DateInput
                        disabled={none}
                        value={rangeCompare?.from}
                        onChange={(date) => {
                          if (rangeCompare) {
                            const compareToDate =
                              rangeCompare.to == null || date > rangeCompare.to
                                ? date
                                : rangeCompare.to;
                            setRangeCompare((prevRangeCompare) => ({
                              ...prevRangeCompare,
                              from: date,
                              to: compareToDate,
                            }));
                          } else {
                            setRangeCompare({
                              from: date,
                              to: new Date(),
                            });
                          }
                        }}
                      />
                      <div className="py-1">{"-"}</div>
                      <DateInput
                        disabled={none}
                        value={rangeCompare?.to}
                        onChange={(date) => {
                          if (rangeCompare && rangeCompare.from) {
                            const compareFromDate =
                              date < rangeCompare.from
                                ? date
                                : rangeCompare.from;
                            setRangeCompare({
                              ...rangeCompare,
                              from: compareFromDate,
                              to: date,
                            });
                          }
                        }}
                      />
                    </div>
                  )}
                </div>
              </div>
              {isSmallScreen && (
                <Select
                  disabled={none}
                  defaultValue={selectedPreset}
                  onValueChange={(value) => {
                    setPreset(value);
                  }}
                >
                  <SelectTrigger className="w-[180px] mx-auto mb-2">
                    <SelectValue placeholder="Select..." />
                  </SelectTrigger>
                  <SelectContent>
                    {PRESETS.map((preset) => (
                      <SelectItem key={preset.name} value={preset.name}>
                        {preset.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
              <div>
                <Calendar
                  disabled={none}
                  mode="range"
                  locale={locale}
                  onSelect={(value: { from?: Date; to?: Date } | undefined) => {
                    if (value?.from != null) {
                      setRange({ from: value.from, to: value?.to });
                    }
                  }}
                  selected={range}
                  numberOfMonths={isSmallScreen ? 1 : 2}
                  defaultMonth={
                    new Date(
                      new Date().setMonth(
                        new Date().getMonth() - (isSmallScreen ? 0 : 1),
                      ),
                    )
                  }
                />
              </div>
            </div>
          </div>
          {!isSmallScreen && (
            <div className="flex flex-col items-end gap-1 pr-2 pl-6 pb-6 ">
              <div className="flex w-full flex-col items-end gap-1 pr-2 pl-6 pb-6">
                {PRESETS.map((preset) => (
                  <PresetButton
                    disabled={none}
                    key={preset.name}
                    preset={preset.name}
                    label={preset.label}
                    isSelected={selectedPreset === preset.name}
                  />
                ))}
              </div>
            </div>
          )}
        </div>
        <div
          className={cn(
            "flex flex-col sm:flex-row  gap-2 py-2 pr-4 items-center",
            showNone ? "justify-between" : "justify-end",
          )}
        >
          {showNone && (
            <Button
              className="w-40 h-10"
              variant={none ? "outlineSuccess" : "outlineAmber"}
              onClick={() => {
                toggleNone();
                if (!none) {
                  setSelectedPreset(undefined);
                  setIsOpen(false);
                  onUpdate?.({ range, rangeCompare }, !none);
                }
              }}
            >
              {none ? noneTrueText : noneFalseText}
            </Button>
          )}
          <div className="flex justify-end gap-2 py-2 pr-4">
            <Button
              className="h-10"
              disabled={none}
              onClick={() => {
                setIsOpen(false);
                resetValues();
              }}
              variant="outline"
            >
              {cancel}
            </Button>
            <Button
              className="h-10"
              disabled={none}
              onClick={() => {
                setIsOpen(false);
                if (
                  !areRangesEqual(range, openedRangeRef.current) ||
                  !areRangesEqual(
                    rangeCompare,
                    openedRangeCompareRef.current,
                  ) ||
                  prevNone.current
                ) {
                  onUpdate?.({ range, rangeCompare }, none);
                } else if (
                  showNone &&
                  isJustFromCurrentDate &&
                  prevNone.current
                ) {
                  onUpdate?.({ range, rangeCompare }, none);
                }

                defaultInitialRangeRef.current = range;
              }}
            >
              {update}
            </Button>
          </div>
        </div>
      </PopoverContent>
    </Popover>
  );
};

DateRangePicker.displayName = "DateRangePicker";
DateRangePicker.filePath =
  "libs/shared/ui-kit/src/lib/date-range-picker/date-range-picker.tsx";
