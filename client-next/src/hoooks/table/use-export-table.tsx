"use client";
import { download, generateCsv, mkConfig } from "export-to-csv";
import { useCallback, useMemo } from "react";
import { useFormatter } from "next-intl";
import { ColumnDef, Row, Table } from "@tanstack/react-table";
import { fromStringOfDotToObjectValue } from "@/lib/utils";
import { format, parseISO } from "date-fns";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import "@/lib/calibri-normal-font-jspdf";

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
  const formatIntl = useFormatter();

  const csvConfig = useMemo(
    () =>
      mkConfig({
        fieldSeparator: ",",
        filename: `${fileName}-${new Date().toISOString()}`,
        decimalSeparator: ".",
        showColumnHeaders: true,
      }),
    [fileName],
  );

  const getExport = useCallback(
    (rows: Row<T>[]) => {
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
            console.log("cur", cur);
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
      return () => ({
        tableColumnHeaders,
        tableRows,
      });
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

  const exportCsv = useCallback(
    (rows: Row<T>[]) => {
      const { tableColumnHeaders, tableRows } = getExport(rows)();
      const formattedRows = tableRows.map((row) =>
        tableColumnHeaders.reduce(
          (acc, cur, index) => {
            acc[cur.id] = row[index];
            // optimization
            return acc;
          },
          {} as Record<string, string | number>,
        ),
      );
      const finalConfig = {
        ...csvConfig,
        columnHeaders: tableColumnHeaders.map((h) => h.id),
      };
      if (formattedRows.length > 0) {
        download(finalConfig)(generateCsv(finalConfig)(formattedRows));
      }
    },
    [csvConfig, getExport],
  );

  const exportPdf = useCallback(
    (rows: Row<T>[]) => {
      const doc = new jsPDF({
        orientation: "landscape",
      });

      const { tableColumnHeaders, tableRows } = getExport(rows)();
      autoTable(doc, {
        head: [tableColumnHeaders.map((h) => h.id)],
        body: tableRows,
        useCss: true,
        theme: "striped",
        headStyles: { fillColor: [22, 160, 133] },
        bodyStyles: { fillColor: [244, 244, 244] },
        alternateRowStyles: { fillColor: [255, 255, 255] },
        startY: 20,
        margin: { top: 20, bottom: 20 },
        styles: {
          fontSize: 10,
          cellPadding: 4,
          overflow: "linebreak",
          minCellWidth: 20,
          font: "calibri",
          fontStyle: "normal",
        },
      });
      doc.save(`${fileName}-${new Date().toISOString()}.pdf`);
    },
    [JSON.stringify(columns), fileName, getExport],
  );

  return {
    exportCsv,
    exportPdf,
  };
}
