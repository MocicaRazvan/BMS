import {
  ChevronLeftIcon,
  ChevronRightIcon,
  DoubleArrowLeftIcon,
  DoubleArrowRightIcon,
} from "@radix-ui/react-icons";
import { Table } from "@tanstack/react-table";

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "./select";
import { Button } from "./button";
import { Dispatch, SetStateAction } from "react";
import { PageInfo } from "@/types/dto";

interface DataTablePaginationProps<TData> {
  table: Table<TData>;
  //   totalPages: number;
  //   totalElements: number;
  //   pagination: Pagination;
  //   setPagination: Dispatch<SetStateAction<Pagination>>;
  pageInfo: PageInfo;
  setPageInfo: Dispatch<SetStateAction<PageInfo>>;
}

export function DataTablePagination<TData>({
  table,
  pageInfo,
  setPageInfo,
}: //   totalPages,
//   totalElements,
//   pagination,
//   setPagination,

DataTablePaginationProps<TData>) {
  console.log(pageInfo);

  return (
    <div className="flex items-center justify-between px-2">
      <div className="flex-1 text-sm text-muted-foreground">
        {pageInfo.currentPage} of {pageInfo.totalPages} row(s) selected.
      </div>
      <div className="flex items-center space-x-6 lg:space-x-8">
        <div className="flex items-center space-x-2">
          <p className="text-sm font-medium">Rows per page</p>
          <Select
            value={`${pageInfo.pageSize}`}
            onValueChange={(value) => {
              setPageInfo((prev) => ({ ...prev, pageSize: +value }));
            }}
          >
            <SelectTrigger className="h-8 w-[70px]">
              <SelectValue placeholder={pageInfo.pageSize} />
            </SelectTrigger>
            <SelectContent side="top">
              {[5, 10, 20, 30, 40].map((pageSize) => (
                <SelectItem key={pageSize} value={`${pageSize}`}>
                  {pageSize}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="flex w-[100px] items-center justify-center text-sm font-medium">
          Page {pageInfo.currentPage + 1} of {pageInfo.totalPages}
        </div>
        <div className="flex items-center space-x-2">
          <Button
            variant="outline"
            className="hidden h-8 w-8 p-0 lg:flex"
            onClick={() => setPageInfo((prev) => ({ ...prev, currentPage: 0 }))}
            disabled={pageInfo.currentPage === 0}
          >
            <span className="sr-only">Go to first page</span>
            <DoubleArrowLeftIcon className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            className="h-8 w-8 p-0"
            onClick={() =>
              setPageInfo((prev) => ({
                ...prev,
                currentPage: prev.currentPage - 1,
              }))
            }
            disabled={pageInfo.currentPage === 0}
          >
            <span className="sr-only">Go to previous page</span>
            <ChevronLeftIcon className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            className="h-8 w-8 p-0"
            onClick={() =>
              setPageInfo((prev) => ({
                ...prev,
                currentPage: prev.currentPage + 1,
              }))
            }
            disabled={pageInfo.currentPage === pageInfo.totalPages - 1}
          >
            <span className="sr-only">Go to next page</span>
            <ChevronRightIcon className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            className="hidden h-8 w-8 p-0 lg:flex"
            onClick={() =>
              setPageInfo((prev) => ({
                ...prev,
                currentPage: prev.totalPages - 1,
              }))
            }
            disabled={pageInfo.currentPage === pageInfo.totalPages - 1}
          >
            <span className="sr-only">Go to last page</span>
            <DoubleArrowRightIcon className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}
