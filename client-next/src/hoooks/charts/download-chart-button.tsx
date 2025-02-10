"use client";
import { useCurrentPng } from "recharts-to-png";
import { useCallback, useEffect, useMemo, useState } from "react";
import FileSaver from "file-saver";
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
  const [getPng, { ref, isLoading }] = useCurrentPng({
    backgroundColor: theme === "dark" ? "#1A202C" : "#f0f0f0",
    windowWidth: 1920,
    windowHeight: 1080,
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
    [JSON.stringify(data)],
  );

  const handleDownload = useCallback(
    async (name: string) => {
      const png = await getPng();

      const dateToAppend =
        minDate === maxDate ? minDate : `${minDate}_${maxDate}`;

      if (png) {
        FileSaver.saveAs(
          png,
          `${name.toLowerCase().trim().replace(/\s+/g, "_")}_${dateToAppend}.png`,
        );
      }
    },
    [getPng, maxDate, minDate],
  );

  const DownloadChartButton = ({ fileName }: { fileName: string }) => (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild={true}>
          <Button
            onClick={() => handleDownload(fileName)}
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
        </TooltipTrigger>
        <TooltipContent>
          <p>{text}</p>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );

  return { DownloadChartButton, downloadChartRef: ref };
}
