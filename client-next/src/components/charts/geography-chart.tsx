"use client";

import { CountryOrderSummary, CountrySummaryType } from "@/types/dto";
import { ResponsiveChoropleth } from "@nivo/geo";
import { geoData } from "@/../public/geoData";
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
import { ZoomInIcon, ZoomOutIcon } from "lucide-react";
import Loader from "@/components/ui/spinner";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import * as React from "react";
import { AnimatePresence, motion } from "framer-motion";
import { cn } from "@/lib/utils";
import { useLocale } from "next-intl";
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import getCountryISO2 from "country-iso-3-to-2";
import { Skeleton } from "@/components/ui/skeleton";

export interface GeographyChartTexts {
  zoomInLabel: string;
  zoomOutLabel: string;
  centerLabel: string;
  resetLabel: string;
  selectLabels: Record<CountrySummaryType, string>;
}

interface Props extends GeographyChartTexts {}
export default function GeographyChart({
  zoomInLabel,
  zoomOutLabel,
  centerLabel,
  resetLabel,
  selectLabels,
}: Props) {
  const locale = useLocale();

  const nrFormater = new Intl.NumberFormat(locale, {
    notation: "compact",
    maximumFractionDigits: 1,
  });
  const regionNames = new Intl.DisplayNames([locale], { type: "region" });
  const [radioOption, setRadioOption] = useState<CountrySummaryType>(
    CountrySummaryType.COUNT,
  );

  const { messages, error, isFinished } = useFetchStream<CountryOrderSummary>({
    path: "/orders/admin/summaryByCountry",
    method: "GET",
    authToken: true,
    queryParams: {
      type: radioOption,
    },
  });

  const customValueFormat = useCallback(
    (value: number) => {
      // if (value >= 1e6) {
      //   return new Intl.NumberFormat(locale, {
      //     notation: "compact",
      //     maximumFractionDigits: 1,
      //   }).format(value);
      // } else if (value >= 1e3) {
      //   return new Intl.NumberFormat(locale, {
      //     notation: "compact",
      //     maximumFractionDigits: 1,
      //   }).format(value);
      // } else {
      //   return value.toString();
      // }

      return nrFormater.format(value);
    },
    [nrFormater],
  );

  const [scale, setScale] = useState(1);

  const max = useMemo(
    () => messages.reduce((acc, curr) => Math.max(acc, curr.value), 0),
    [messages],
  );
  const domain = useMemo(() => [0, Math.round(max)], [max]);

  const colorScale = scaleSequential(interpolateBlues).domain(domain);

  const legendItems = useMemo(() => {
    const steps = 9;
    const stepValue = (domain[1] - domain[0]) / steps;
    return Array.from({ length: steps }, (_, i) => {
      const startValue = domain[0] + stepValue * i;
      const endValue = domain[0] + stepValue * (i + 1);
      return {
        startValue: startValue,
        endValue: endValue,
        color: colorScale(startValue),
      };
    });
  }, [domain, colorScale]);

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
            <div className="w-full h-full relative  ">
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
                <div>
                  <DropDownMenuGeographySelect
                    radioOption={radioOption}
                    onRadioOptionChange={setRadioOption}
                    labels={selectLabels}
                  />
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
