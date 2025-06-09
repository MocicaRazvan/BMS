"use client";
import {
  Dispatch,
  RefObject,
  SetStateAction,
  useCallback,
  useMemo,
  useState,
} from "react";
import {
  TransformComponent,
  TransformWrapper,
  useTransformEffect,
} from "react-zoom-pan-pinch";
import { AnimatePresence, motion } from "framer-motion";
import { Button } from "@/components/ui/button";
import { DownloadIcon, ZoomInIcon, ZoomOutIcon } from "lucide-react";
import CreationFilter, {
  CreationFilterTexts,
} from "@/components/list/creation-filter";
import { Skeleton } from "@/components/ui/skeleton";
import { ResponsiveChoropleth } from "@nivo/geo";
import { cn } from "@/lib/utils";
import getCountryISO2 from "country-iso-3-to-2";
import { DropDownMenuGeographySelect } from "@/components/charts/geography-chart";
import { useLocale } from "next-intl";
import { CountryOrderSummary, CountrySummaryType } from "@/types/dto";
import useDateRangeFilterParams from "@/hoooks/useDateRangeFilterParams";

export interface GeoDataType {
  type: string;
  features: (
    | {
        type: string;
        properties: {
          name: string;
        };
        geometry: {
          type: string;
          coordinates: number[][][];
        };
        id: string;
      }
    | {
        type: string;
        properties: {
          name: string;
        };
        geometry: {
          type: string;
          coordinates: number[][][][];
        };
        id: string;
      }
  )[];
}

export interface GeographyChartTexts {
  zoomInLabel: string;
  zoomOutLabel: string;
  centerLabel: string;
  resetLabel: string;
  selectLabels: Record<CountrySummaryType, string>;
  creationFilterTexts: CreationFilterTexts;
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

export interface LegendItem {
  startValue: number;
  endValue: number;
  color: string;
}

interface Props extends GeographyChartTexts {
  isFinished: boolean;
  outerRef: RefObject<HTMLDivElement>;
  legendItems: LegendItem[];
  radioOption: CountrySummaryType;
  isLoading: boolean;
  updateCreatedAtRange: ReturnType<
    typeof useDateRangeFilterParams
  >["updateRange"];
  setRadioOption: Dispatch<SetStateAction<CountrySummaryType>>;
  handleDivDownload: () => Promise<void>;
  domain: number[];
  messages: CountryOrderSummary[];
  geoData: GeoDataType | undefined;
}
export function GeographyChartContent({
  isFinished,
  outerRef,
  legendItems,
  radioOption,
  zoomInLabel,
  zoomOutLabel,
  centerLabel,
  resetLabel,
  selectLabels,
  creationFilterTexts,
  isLoading,
  updateCreatedAtRange,
  setRadioOption,
  handleDivDownload,
  domain,
  messages,
  geoData,
}: Props) {
  const locale = useLocale();
  const regionNames = new Intl.DisplayNames([locale], { type: "region" });

  const [scale, setScale] = useState(1);

  const nrFormater = useMemo(
    () =>
      new Intl.NumberFormat(locale, {
        notation: "compact",
        maximumFractionDigits: radioOption === "TOTAL_AMOUNT" ? 1 : 0,
      }),
    [locale, radioOption],
  );
  const customValueFormat = useCallback(
    (value: number) => {
      return nrFormater.format(value);
    },
    [nrFormater],
  );
  return (
    <TransformWrapper
      initialScale={1}
      initialPositionX={0}
      initialPositionY={0}
      wheel={{ step: 100 }}
    >
      {({ zoomIn, zoomOut, resetTransform, centerView, ...rest }) => (
        <div className="w-full h-full relative" ref={outerRef}>
          <AnimatePresence>
            {isFinished && (
              <motion.div
                initial={{ opacity: 0, scale: 0.5 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{
                  duration: 0.4,
                  delay: 0.5,
                  ease: "easeInOut",
                }}
                className="absolute  bottom-4 lg:bottom-10  right-0 lg:right-10
              shadow z-10 flex flex-row lg:flex-col bg-background
              p-2 rounded backdrop-blur supports-[backdrop-filter]:bg-background/60
                 items-start justify-between flex-wrap gap-2"
              >
                {legendItems.map((item, index) => (
                  <div key={index} className=" flex items-center mb-1">
                    <span
                      className="w-4 h-4 rounded-sm"
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
          <div className=" w-full z-10 flex items-center justify-between flex-wrap gap-1 mb-10 ">
            <div className="flex items-center justify-center flex-wrap gap-4">
              <Button onClick={() => zoomIn()} size="sm">
                {zoomInLabel} <ZoomInIcon className="ms-1" />
              </Button>
              <Button onClick={() => zoomOut()} size="sm">
                {zoomOutLabel} <ZoomOutIcon className="ms-1" />
              </Button>
              <Button onClick={() => centerView()} size="sm">
                {centerLabel}
              </Button>
              <Button onClick={() => resetTransform()} size="sm">
                {resetLabel}
              </Button>
            </div>
            <div className="flex w-full md:w-fit items-center justify-around mt-5 md:mt-0 md:justify-end gap-2.5 lg:gap-3 flex-w">
              <div>
                <CreationFilter
                  {...creationFilterTexts}
                  updateCreatedAtRange={updateCreatedAtRange}
                  hideUpdatedAt={true}
                />
              </div>
              <DropDownMenuGeographySelect
                radioOption={radioOption}
                onRadioOptionChange={setRadioOption}
                labels={selectLabels}
              />
              <Button
                onClick={() => handleDivDownload()}
                disabled={isLoading}
                type="button"
                className="w-14"
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
                <Skeleton className="mx-auto h-full" />
              ) : !geoData ? (
                <div className="mx-auto h-full animate-in fade-in duration-500 delay-300">
                  <Skeleton className="size-full" />
                </div>
              ) : (
                <div className="size-full animate-in fade-in duration-300">
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
                      feature.value > 0 && (
                        <div
                          style={{
                            transform: `scale(${1 / (0.9 * scale)}) translateY(${(scale - 1) * 65}%)`,
                          }}
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
                            {regionNames.of(getCountryISO2(feature.data?.id))}
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
                </div>
              )}
            </div>
          </TransformComponent>
        </div>
      )}
    </TransformWrapper>
  );
}
