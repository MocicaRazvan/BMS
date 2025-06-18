"use client";
import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { DownloadIcon } from "lucide-react";
import { useTheme } from "next-themes";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { getUseDownloadChartButtonTexts } from "@/texts/components/charts";
import Loader from "@/components/ui/spinner";
import { useWindowSize } from "react-use";
import { useCurrentPngDynamic } from "@/lib/recharts/recharts-to-img";

export interface DateString {
  date: string;
}
interface Args<T extends DateString> {
  data: T[];
}

export default function useDownloadChartButton<T extends DateString>({
  data,
}: Args<T>) {
  const [text, setText] = useState("");
  useEffect(() => {
    getUseDownloadChartButtonTexts().then((t) => setText(t.downloadChart));
  }, []);
  const { theme } = useTheme();
  const [isLoading, setIsLoading] = useState(false);
  const { width, height } = useWindowSize({
    initialHeight: 1080,
    initialWidth: 1920,
  });
  const [getPng, { ref, isLoading: isPngLoading }] = useCurrentPngDynamic({
    backgroundColor: theme === "dark" ? "#1A202C" : "#f0f0f0",
    windowWidth: width,
    windowHeight: height,
  });

  const { minDate, maxDate } = useMemo(
    () =>
      data.length === 0
        ? { minDate: new Date(), maxDate: new Date() }
        : data.reduce(
            (acc, { date }) => {
              const parts = date.split("-");
              let formattedDate = date;

              if (parts.length === 3) {
                const [dd, mm, yyyy] = parts;
                formattedDate = `${yyyy}-${mm}-${dd}`;
              } else if (parts.length === 2) {
                const [mm, yyyy] = parts;
                formattedDate = `${yyyy}-${mm}-01`;
              }

              if (formattedDate < acc.minDate) acc.minDate = formattedDate;
              if (formattedDate > acc.maxDate) acc.maxDate = formattedDate;

              return acc;
            },
            {
              minDate: (() => {
                const parts = data[0].date.split("-");
                return parts.length === 3
                  ? `${parts[2]}-${parts[1]}-${parts[0]}`
                  : `${parts[1]}-${parts[0]}-01`;
              })(),
              maxDate: (() => {
                const parts = data[0].date.split("-");
                return parts.length === 3
                  ? `${parts[2]}-${parts[1]}-${parts[0]}`
                  : `${parts[1]}-${parts[0]}-01`;
              })(),
            },
          ),
    [data],
  );

  const handleDownload = useCallback(
    async (name: string) => {
      setIsLoading(true);
      const [png, saveAs] = await Promise.all([
        getPng(),
        import("file-saver").then((m) => m.default),
      ]);

      const dateToAppend =
        minDate === maxDate ? minDate : `${minDate}_${maxDate}`;
      const now = new Date().toISOString();

      if (png) {
        saveAs(
          png,
          `${name.toLowerCase().trim().replace(/\s+/g, "_")}-${dateToAppend}-${now}.png`,
        );
      }
      setIsLoading(false);
    },
    [getPng, maxDate, minDate],
  );

  const isFinalLoading = isLoading || isPngLoading;

  const DownloadChartButton = ({ fileName }: { fileName: string }) => (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild={true}>
          <Button
            onClick={() => handleDownload(fileName)}
            disabled={isFinalLoading}
            type="button"
            className="min-w-14"
            variant="outline"
          >
            {isFinalLoading ? <Loader className="size-5" /> : <DownloadIcon />}
          </Button>
        </TooltipTrigger>
        <TooltipContent>
          <p>{text}</p>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );

  return { DownloadChartButton, downloadChartRef: ref };
}
