"use client";

import { CountryOrderSummary, CountrySummaryType } from "@/types/dto";
import { ResponsiveChoropleth } from "@nivo/geo";
import { geoData } from "@/../public/geoData";
import * as React from "react";
import {
  Dispatch,
  SetStateAction,
  useCallback,
  useMemo,
  useState,
} from "react";
import useFetchStream from "@/hoooks/useFetchStream";
import {
  TransformComponent,
  TransformWrapper,
  useTransformEffect,
} from "react-zoom-pan-pinch";
import { scaleSequential } from "d3-scale";
import { interpolateBlues } from "d3-scale-chromatic";
import { Button } from "@/components/ui/button";
import { DownloadIcon, ZoomInIcon, ZoomOutIcon } from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { AnimatePresence, motion } from "framer-motion";
import { cn } from "@/lib/utils";
import { useLocale } from "next-intl";
import getCountryISO2 from "country-iso-3-to-2";
import { Skeleton } from "@/components/ui/skeleton";
import { useTheme } from "next-themes";
import { useGenerateImage } from "recharts-to-png";
import FileSaver from "file-saver";
import CreationFilter, {
  CreationFilterTexts,
} from "@/components/list/creation-filter";
import useDateRangeFilterParams from "@/hoooks/useDateRangeFilterParams";

const LEGEND_STEPS = 9;

export interface GeographyChartTexts {
  zoomInLabel: string;
  zoomOutLabel: string;
  centerLabel: string;
  resetLabel: string;
  selectLabels: Record<CountrySummaryType, string>;
  creationFilterTexts: CreationFilterTexts;
}

interface Props extends GeographyChartTexts {}
export default function GeographyChart({
  zoomInLabel,
  zoomOutLabel,
  centerLabel,
  resetLabel,
  selectLabels,
  creationFilterTexts,
}: Props) {
  const locale = useLocale();

  const regionNames = new Intl.DisplayNames([locale], { type: "region" });
  const [radioOption, setRadioOption] = useState<CountrySummaryType>(
    CountrySummaryType.COUNT,
  );

  const nrFormater = useMemo(
    () =>
      new Intl.NumberFormat(locale, {
        notation: "compact",
        maximumFractionDigits: radioOption === "TOTAL_AMOUNT" ? 1 : 0,
      }),
    [locale, radioOption],
  );

  const {
    updateRange: updateCreatedAtRange,
    queryParams: createdAtRangeParams,
  } = useDateRangeFilterParams("from", "to");

  const { messages, error, isFinished } = useFetchStream<CountryOrderSummary>({
    path: "/orders/admin/summaryByCountry",
    method: "GET",
    authToken: true,
    queryParams: {
      type: radioOption,
      ...createdAtRangeParams,
    },
  });

  const customValueFormat = useCallback(
    (value: number) => {
      return nrFormater.format(value);
    },
    [nrFormater],
  );

  const [scale, setScale] = useState(1);

  const max = useMemo(
    () =>
      // messages.reduce((acc, curr) => Math.max(acc, curr.value), 0),
      messages.length ? messages[0].maxGroupTotal : 0,
    [JSON.stringify(messages)],
  );
  const domain = useMemo(() => [0, Math.round(max)], [max]);

  const colorScale = scaleSequential(interpolateBlues).domain(domain);

  const legendItems = useMemo(() => {
    const steps = LEGEND_STEPS;
    const stepValue = (domain[1] - domain[0]) / steps;
    return Array.from({ length: steps }, (_, i) => {
      const startValue = domain[0] + stepValue * i;
      const endValue = domain[0] + stepValue * (i + 1);
      return {
        startValue,
        endValue,
        color: colorScale(startValue),
      };
    }).filter(({ startValue, endValue }) => startValue < endValue);
  }, [domain, colorScale, JSON.stringify(messages)]);

  const { theme } = useTheme();

  const [getDivJpeg, { ref, isLoading }] = useGenerateImage<HTMLDivElement>({
    quality: 1,
    type: "image/jpeg",
    options: {
      ignoreElements: (element) =>
        ["BUTTON", "LABEL"].includes(element.tagName),
      windowWidth: 1920,
      windowHeight: 1080,
      scrollY: 0,
      scrollX: 0,
      scale: 1,
      width: 1440,
      height: 1045,
      backgroundColor: theme === "dark" ? "#1A202C" : "#f0f0f0",
      x: 0,
      y: 0,
      logging: false,
      imageTimeout: 20000,
      useCORS: false,
      allowTaint: false,
      foreignObjectRendering: false,
    },
  });
  const handleDivDownload = useCallback(async () => {
    const jpeg = await getDivJpeg();

    if (jpeg) {
      FileSaver.saveAs(
        jpeg,
        `${selectLabels[radioOption].trim().replace(/\s+/g, "_")}_${new Date()
          .toLocaleString(locale, {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit",
          })
          .replace(/[\s,.]+/g, "_")}_geographyChart.jpeg`,
      );
    }
  }, [getDivJpeg, locale, radioOption, selectLabels]);
  return (
    <div
      className="h-[100vh] w-full mx-auto relative bg-accent-foreground/30 dark:bg-accent/80
      backdrop-blur supports-[backdrop-filter]:bg-accent-foreground/25 dark:supports-[backdrop-filter]:bg-accent/35
     p-4 rounded overflow-hidden border-2"
    >
      <div className="h-[100vh] w-full mx-auto relative ">
        <TransformWrapper
          initialScale={1}
          initialPositionX={0}
          initialPositionY={0}
          wheel={{ step: 100 }}
        >
          {({ zoomIn, zoomOut, resetTransform, centerView, ...rest }) => (
            <div className="w-full h-full relative  " ref={ref}>
              <AnimatePresence>
                {isFinished && (
                  <motion.div
                    initial={{ opacity: 0, scale: 0.5 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ duration: 0.4, delay: 0.5 }}
                    className="absolute  bottom-4 lg:bottom-10  right-0 lg:right-10
              shadow z-10 flex flex-row lg:flex-col bg-background
              p-2 rounded backdrop-blur supports-[backdrop-filter]:bg-background/60
                 items-start justify-between flex-wrap gap-2"
                  >
                    {legendItems.map((item, index) => (
                      <div key={index} className=" flex items-center mb-1">
                        <span
                          className=" w-4 h-4"
                          style={{ backgroundColor: item.color }}
                        ></span>
                        <p className=" ml-2">
                          {nrFormater.format(item.startValue)} {"- "}
                          {nrFormater.format(item.endValue)}
                        </p>
                      </div>
                    ))}
                  </motion.div>
                )}
              </AnimatePresence>
              <div className=" top-4 w-full right-4 z-10 flex items-center justify-between flex-wrap gap-2 mb-10">
                <div
                  className={"flex items-center justify-center flex-wrap gap-4"}
                >
                  <Button onClick={() => zoomIn()}>
                    {zoomInLabel} <ZoomInIcon className="ms-2" />
                  </Button>
                  <Button onClick={() => zoomOut()}>
                    {zoomOutLabel} <ZoomOutIcon className="ms-2" />
                  </Button>
                  <Button onClick={() => centerView()}>{centerLabel}</Button>
                  <Button onClick={() => resetTransform()}>{resetLabel}</Button>
                </div>
                <div className="flex w-full md:w-fit items-center justify-around mt-5 md:mt-0 md:justify-end gap-3 md:gap-8 lg:gap-12">
                  <CreationFilter
                    {...creationFilterTexts}
                    updateCreatedAtRange={updateCreatedAtRange}
                    hideUpdatedAt={true}
                  />
                  <DropDownMenuGeographySelect
                    radioOption={radioOption}
                    onRadioOptionChange={setRadioOption}
                    labels={selectLabels}
                  />
                  <Button
                    onClick={() => handleDivDownload()}
                    disabled={isLoading}
                    type="button"
                    className="min-w-[65px]"
                    variant="outline"
                  >
                    {isLoading ? (
                      <span className=" text-primary/60 animate-spin font-bold text-lg w-full h-full " />
                    ) : (
                      <DownloadIcon size={24} />
                    )}
                  </Button>
                </div>
                {/*{scale}*/}
              </div>

              <TransformComponent
                wrapperStyle={{ height: "100%", width: "100%" }}
                contentStyle={{ height: "100%", width: "100%" }}
              >
                <div className="h-full w-full mx-auto relative cursor-grabbing bg-opacity-50 ">
                  {!isFinished ? (
                    // <Loader className={"mx-auto h-full "} />
                    <Skeleton className={"mx-auto h-full "} />
                  ) : (
                    <>
                      <ScaleWrapper setScale={setScale} />
                      <ResponsiveChoropleth
                        data={messages}
                        features={geoData.features}
                        margin={{ top: 0, right: 0, bottom: 0, left: 0 }}
                        domain={domain}
                        unknownColor="var(--nivo-unknown)"
                        label="properties.name"
                        valueFormat={customValueFormat}
                        projectionScale={200}
                        projectionTranslation={[0.5, 0.5]}
                        projectionRotation={[0, 0, 0]}
                        borderWidth={0.8}
                        borderColor="black"
                        colors={"blues"}
                        tooltip={({ feature }) =>
                          feature.value > 0 &&
                          scale < 3.5 && (
                            <div
                              style={{ transform: `scale(${1 / scale})` }}
                              className={cn(
                                ` flex items-center justify-center p-2 
                              origin-center bg-background
                             rounded backdrop-blur supports-[backdrop-filter]:bg-background/70
                              `,
                              )}
                            >
                              <span
                                className=" w-4 h-4"
                                style={{ backgroundColor: feature.color }}
                              ></span>
                              <p className=" ms-2">{feature.formattedValue}</p>
                              <p className="ms-2">
                                {regionNames.of(
                                  getCountryISO2(feature.data?.id),
                                )}
                              </p>
                            </div>
                          )
                        }
                        theme={{
                          axis: {
                            domain: {
                              line: {
                                stroke: "hsl(var(--accent))",
                              },
                            },
                            legend: {
                              text: {
                                fill: "hsl(var(--accent))",
                              },
                            },
                            ticks: {
                              line: {
                                stroke: "hsl(var(--accent))",
                                strokeWidth: 1,
                              },
                              text: {
                                fill: "hsl(var(--accent))",
                              },
                            },
                          },
                          legends: {
                            text: {
                              fill: "hsl(var(--accent))",
                            },
                          },
                          tooltip: {
                            container: {
                              color: "hsl(var(--foreground))",
                              backgroundColor: "hsl(var(--background))",
                              transform: ` scale(${1 / scale})`,
                              transformOrigin: "center",
                              display: scale > 3.5 ? "none" : "block",
                            },
                          },
                        }}
                      />
                    </>
                  )}
                </div>
              </TransformComponent>
            </div>
          )}
        </TransformWrapper>
      </div>
    </div>
  );
}

function ScaleWrapper({
  setScale,
}: {
  setScale: Dispatch<SetStateAction<number>>;
}) {
  useTransformEffect(({ state }) => {
    setScale(state.scale);
  });

  return <></>;
}

interface DropDownMenuGeographySelectProps {
  onRadioOptionChange: (option: CountrySummaryType) => void;
  radioOption: CountrySummaryType;
  labels: Record<CountrySummaryType, string>;
}
export function DropDownMenuGeographySelect({
  radioOption,
  onRadioOptionChange,
  labels,
}: DropDownMenuGeographySelectProps) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline">{labels[radioOption]}</Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56">
        <DropdownMenuRadioGroup
          value={radioOption}
          onValueChange={(e) => onRadioOptionChange(e as CountrySummaryType)}
        >
          <DropdownMenuRadioItem value={CountrySummaryType.COUNT}>
            {labels[CountrySummaryType.COUNT]}
          </DropdownMenuRadioItem>
          <DropdownMenuRadioItem value={CountrySummaryType.TOTAL_AMOUNT}>
            {labels[CountrySummaryType.TOTAL_AMOUNT]}
          </DropdownMenuRadioItem>
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
