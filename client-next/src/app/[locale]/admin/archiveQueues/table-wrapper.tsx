"use client";
import { ArchiveQueuesTableProps } from "@/components/table/archive-queues-table";
import dynamic from "next/dynamic";
import DataTableDynamicSkeleton from "@/components/table/data-table-dynamic-skeleton";
const DynamicDataTable = dynamic(
  () => import("@/components/table/archive-queues-table"),
  {
    ssr: false,
    loading: () => <DataTableDynamicSkeleton />,
  },
);

export default function ArchiveQueuesTableWrapper(
  props: ArchiveQueuesTableProps,
) {
  return <DynamicDataTable {...props} />;
}
