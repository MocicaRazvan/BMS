"use client";
import { useCallback, useState } from "react";
import { useFormatter } from "next-intl";
import { ColumnDef, Row, Table } from "@tanstack/react-table";
import { fromStringOfDotToObjectValue } from "@/lib/utils";
import fetchFactory from "@/lib/fetchers/fetchWithRetry";
import { getCsrfNextAuthHeader } from "@/actions/get-csr-next-auth";

enum ROUTES {
  CSV = "/api/table/export-csv",
  PDF = "/api/table/export-pdf",
}

type ExportRoute = (typeof ROUTES)[keyof typeof ROUTES];

export interface UseExportTableArgs<T extends Record<string, any>, V> {
  lastLengthColumns?: string[];
  dateColumns?: string[];
  currencyColumns?: string[];
  fileName: string;
  hidePDFColumnIds?: string[];
  table: Table<T>;
  columns: ColumnDef<T, V>[];
  specialPDFColumns?: {
    key: string;
    handler: (value: object) => string;
  }[];
}
export default function useExportTable<T extends Record<string, any>, V>({
  lastLengthColumns = ["userLikes", "userDislikes"],
  dateColumns = ["createdAt", "updatedAt"],
  currencyColumns = ["price", "total"],
  fileName,
  hidePDFColumnIds = [],
  table,
  columns,
  specialPDFColumns = [],
}: UseExportTableArgs<T, V>) {
  const [isExporting, setIsExporting] = useState<Record<ExportRoute, boolean>>({
    [ROUTES.PDF]: false,
    [ROUTES.CSV]: false,
  });
  const formatIntl = useFormatter();

  const getExport = useCallback(
    async (rows: Row<T>[]) => {
      const [format, parseISO] = await Promise.all([
        import("date-fns/format").then((m) => m.format),
        import("date-fns/parseISO").then((m) => m.parseISO),
      ]);

      const tableColumnHeaders = columns
        .filter((c) =>
          table
            .getAllColumns()
            .filter((column) => column.getIsVisible())
            .map((column) => column.id)
            .includes(c?.id || ""),
        )
        .reduce(
          (acc, cur) => {
            const obj = {
              id: "",
              accessorKey: "",
            };

            if (cur.id) {
              if (
                cur.id === "actions" ||
                cur.id === "select" ||
                hidePDFColumnIds.includes(cur.id)
              ) {
                return acc;
              }
              obj.id = cur.id;
            }
            if ("accessorKey" in cur) {
              obj.accessorKey = String(cur.accessorKey);
            }
            acc.push(obj);
            return acc;
          },
          [] as {
            id: string;
            accessorKey: string;
          }[],
        );

      const tableRows = rows.map(({ original }) =>
        tableColumnHeaders.map((h) => {
          const {
            isLastLengthColumn,
            isDateColumn,
            isNumberColumn,
            isSpecial,
          } = h.accessorKey.split(".").reduce(
            (acc, key) => {
              if (
                lastLengthColumns.includes(key) ||
                key.toLowerCase().includes("ids")
              ) {
                acc.isLastLengthColumn = true;
              }
              if (dateColumns.includes(key)) {
                acc.isDateColumn = true;
              }
              if (currencyColumns.includes(key)) {
                acc.isNumberColumn = true;
              }

              if (specialPDFColumns.find((s) => s.key === h.accessorKey)) {
                acc.isSpecial = true;
              }

              return acc;
            },
            {
              isLastLengthColumn: false,
              isDateColumn: false,
              isNumberColumn: false,
              isSpecial: false,
            },
          );
          let value = fromStringOfDotToObjectValue(
            h.accessorKey,
            original,
            isLastLengthColumn,
          );
          if (Array.isArray(value)) {
            value = value.join(", ");
          } else if (isDateColumn) {
            value = format(parseISO(value), "dd/MM/yyyy");
          } else if (isNumberColumn) {
            value = formatIntl.number(value, {
              style: "currency",
              currency: "EUR",
              maximumFractionDigits: 2,
            });
          } else if (typeof value === "object" && isSpecial) {
            value = specialPDFColumns
              .find((s) => s.key === h.accessorKey)
              ?.handler(value);
          }
          return value as string | number;
        }),
      );
      return {
        tableColumnHeaders,
        tableRows,
      };
    },
    [
      JSON.stringify(columns),
      formatIntl,
      JSON.stringify(hidePDFColumnIds),
      JSON.stringify(specialPDFColumns),
      JSON.stringify(lastLengthColumns),
      JSON.stringify(dateColumns),
      JSON.stringify(currencyColumns),
    ],
  );

  const exportBase = useCallback(
    async (rows: Row<T>[], route: string, extension: string) => {
      setIsExporting((prev) => ({
        ...prev,
        [route]: true,
      }));
      const [{ tableColumnHeaders, tableRows }, csrf] = await Promise.all([
        getExport(rows),
        getCsrfNextAuthHeader(),
      ]);
      const finalFileName = `${fileName}-${new Date().toISOString()}.${extension}`;
      try {
        const [res, saveAs] = await Promise.all([
          fetchFactory(fetch)(route, {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json", ...csrf },
            body: JSON.stringify({
              tableColumnHeaders,
              tableRows,
              fileName: finalFileName,
            }),
          }),
          import("file-saver").then((m) => m.default),
        ]);
        if (!res.ok) {
          const body = await res.json();
          console.log("Error exporting file:", body, route);
          return;
        }
        const blob = await res.blob();
        saveAs(blob, finalFileName);
      } catch (error) {
        console.log("Error exporting file:", error, route);
      } finally {
        setIsExporting((prev) => ({ ...prev, [route]: false }));
      }
    },
    [fileName, getExport],
  );

  const exportCsv = useCallback(
    (rows: Row<T>[]) => exportBase(rows, ROUTES.CSV, "csv"),
    [exportBase],
  );

  const exportPdf = useCallback(
    (rows: Row<T>[]) => exportBase(rows, ROUTES.PDF, "pdf"),
    [exportBase],
  );

  const isCsvExporting = isExporting[ROUTES.CSV];
  const isPdfExporting = isExporting[ROUTES.PDF];
  const isOneExporting = isPdfExporting || isCsvExporting;

  return {
    exportCsv,
    exportPdf,
    isPdfExporting,
    isCsvExporting,
    isOneExporting,
  };
}
