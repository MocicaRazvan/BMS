"use client";

import { CountryOrderSummary, CountrySummaryType } from "@/types/dto";
import { useCallback, useEffect, useMemo, useState } from "react";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import { scaleSequential } from "d3-scale";
import { interpolateBlues } from "d3-scale-chromatic";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useLocale } from "next-intl";
import { useTheme } from "next-themes";
import useDateRangeFilterParams from "@/hoooks/useDateRangeFilterParams";
import {
  GeoDataType,
  GeographyChartTexts,
  LegendItem,
} from "@/components/charts/geography-chart-content";
import dynamic from "next/dynamic";
import { Skeleton } from "@/components/ui/skeleton";
import fetchFactory from "@/lib/fetchers/fetchWithRetry";
import geoDataUrl from "@/assets/data/geoData.json?url";
import { useWindowSize } from "react-use";
import { useGenerateImageDynamic } from "@/lib/recharts/recharts-to-img";

const LEGEND_STEPS = 9;

const DynamicGeographyChart = dynamic(
  () =>
    import("@/components/charts/geography-chart-content").then(
      (m) => m.GeographyChartContent,
    ),
  {
    ssr: false,
    loading: () => <Skeleton className="size-full" />,
  },
);

interface Props extends GeographyChartTexts {}
export default function GeographyChart(props: Props) {
  const locale = useLocale();
  const [geoData, setGeoData] = useState<GeoDataType>();

  useEffect(() => {
    let isMounted = true;
    (async () => {
      if (!isMounted) return;
      const res = await fetchFactory(fetch)(geoDataUrl, {
        cache: "default",
      });
      if (!res.ok) {
        console.error("Failed to fetch geo data", await res.text());
        return;
      }

      const data: GeoDataType = await res.json();
      setGeoData(data);
    })();

    return () => {
      isMounted = false;
    };
  }, []);

  const [radioOption, setRadioOption] = useState<CountrySummaryType>(
    CountrySummaryType.COUNT,
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

  // const max = useMemo(
  //   () =>
  //     // messages.reduce((acc, curr) => Math.max(acc, curr.value), 0),
  //     messages.length ? messages[0].maxGroupTotal : 0,
  //   [messages],
  // );
  const domain = useMemo(
    () => [0, messages.length ? Math.round(messages[0].maxGroupTotal) : 0],
    [messages],
  );

  const colorScale = scaleSequential(interpolateBlues).domain(domain);

  const legendItems: LegendItem[] = useMemo(() => {
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
  }, [domain, colorScale]);

  const { theme } = useTheme();
  const [isLocalLoading, setIsLocalLoading] = useState(false);
  const { width, height } = useWindowSize({
    initialHeight: 1080,
    initialWidth: 1920,
  });
  const [getDivJpeg, { ref, isLoading: isChartLoading }] =
    useGenerateImageDynamic<HTMLDivElement>({
      quality: 1,
      type: "image/jpeg",
      options: {
        ignoreElements: (element) =>
          ["BUTTON", "LABEL"].includes(element.tagName),
        windowWidth: width,
        windowHeight: height,
        scrollY: 0,
        scrollX: 0,
        scale: 1,
        width: 0.875 * width,
        height: 0.95 * height,
        // width: 1440,
        // height: 1045,
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
    setIsLocalLoading(true);
    const [jpeg, saveAs] = await Promise.all([
      getDivJpeg(),
      import("file-saver").then((m) => m.default),
    ]);

    if (jpeg) {
      saveAs(
        jpeg,
        `${props.selectLabels[radioOption].trim().replace(/\s+/g, "_")}_${new Date()
          .toISOString()
          .replace(/[\s,.]+/g, "_")}_geographyChart.jpeg`,
      );
    }

    setIsLocalLoading(false);
  }, [getDivJpeg, locale, radioOption, props.selectLabels]);
  return (
    <div
      className="h-[100vh] w-full mx-auto relative bg-accent-foreground/30 dark:bg-accent/80
      backdrop-blur supports-[backdrop-filter]:bg-accent-foreground/25 dark:supports-[backdrop-filter]:bg-accent/35
     p-4 rounded overflow-hidden border-2"
    >
      <div className="h-[100vh] w-full mx-auto relative">
        <DynamicGeographyChart
          {...props}
          isFinished={isFinished}
          isLoading={isLocalLoading || isChartLoading}
          outerRef={ref}
          legendItems={legendItems}
          radioOption={radioOption}
          updateCreatedAtRange={updateCreatedAtRange}
          setRadioOption={setRadioOption}
          handleDivDownload={handleDivDownload}
          domain={domain}
          messages={messages}
          geoData={geoData}
        />
      </div>
    </div>
  );
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
        <Button variant="outline" className="w-48">
          {labels[radioOption]}
        </Button>
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
