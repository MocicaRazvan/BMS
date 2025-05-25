"use client";

import { Header, Table as TableType } from "@tanstack/react-table";
import { useMemo } from "react";

interface Args<T> {
  table: TableType<T>;
}

export const createHeaderKeySize = (id: string) => `--header-${id}-size`;
export const createColumnKeySize = (id: string) => `--col-${id}-size`;
export const createHeaderLeft = (id: string) => `--header-${id}-left`;
export const createColumnLeft = (id: string) => `--col-${id}-left`;
export const calcVar = (varName: string) => `calc(var(${varName}) * 1px)`;

export default function useTableColResize<T>({ table }: Args<T>) {
  return useMemo(() => {
    const headers = table.getFlatHeaders();
    const colSizes: { [key: string]: number } = {};
    const aggregatedColSizes: { [key: string]: number } = {};
    let prevHeader: Header<T, unknown> | null = null;
    let prevHeaderLeftKey: string | null = null;
    let prevColumnLeftKey: string | null = null;
    for (let i = 0; i < headers.length; i++) {
      const header = headers[i]!;
      colSizes[createHeaderKeySize(header.id)] = header.getSize();
      colSizes[createColumnKeySize(header.column.id)] = header.column.getSize();
      const curHeaderLeft = createHeaderLeft(header.id);
      const curColumnLeft = createColumnLeft(header.column.id);
      if (i === 0) {
        aggregatedColSizes[curHeaderLeft] = 0;
        aggregatedColSizes[curColumnLeft] = 0;
      } else if (prevHeader && prevHeaderLeftKey && prevColumnLeftKey) {
        aggregatedColSizes[curHeaderLeft] =
          aggregatedColSizes[prevHeaderLeftKey] + prevHeader.getSize();
        aggregatedColSizes[curColumnLeft] =
          aggregatedColSizes[prevColumnLeftKey] + prevHeader.column.getSize();
      }
      prevHeader = header;
      prevHeaderLeftKey = curHeaderLeft;
      prevColumnLeftKey = curColumnLeft;
    }
    return [colSizes, aggregatedColSizes];
  }, [table.getState().columnSizingInfo, table.getState().columnSizing]);
}
