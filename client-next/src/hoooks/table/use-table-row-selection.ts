"use client";

import { useCallback, useMemo, useState } from "react";
import {
  Row,
  RowSelectionState,
  Table as TableType,
} from "@tanstack/react-table";

interface Args<T> {
  table: TableType<T>;
  getRowId: (row: T) => string;
}

export default function useTableRowSelection<T>() {
  const [rowSelection, setRowSelection] = useState<RowSelectionState>({});
  const [persistedRows, setPersistedRows] = useState<Row<T>[]>([]);
  const selectedLength = useMemo(
    () => Object.keys(rowSelection).length,
    [rowSelection],
  );

  const updatePersistedRows = useCallback(
    ({ getRowId, table }: Args<T>) => {
      const ids = Object.keys(rowSelection);

      if (ids.length === 0) {
        setPersistedRows([]);
        return;
      }

      const prevMap = new Map(
        persistedRows.map((row) => [getRowId(row.original), row]),
      );
      const tableRows = table.getCoreRowModel().rows;
      const tableRowMap = new Map(
        tableRows.map((row) => [getRowId(row.original), row]),
      );

      const updated = ids
        .map((id) => prevMap.get(id) || tableRowMap.get(id))
        .filter((row): row is Row<T> => Boolean(row));
      setPersistedRows(updated);
    },
    [rowSelection],
  );

  const clearRowSelection = useCallback(() => {
    setRowSelection({});
  }, []);

  return {
    rowSelection,
    setRowSelection,
    persistedRows,
    setPersistedRows,
    selectedLength,
    updatePersistedRows,
    clearRowSelection,
  };
}
