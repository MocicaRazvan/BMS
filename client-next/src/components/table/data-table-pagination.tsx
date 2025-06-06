import {
  ChevronLeftIcon,
  ChevronRightIcon,
  DoubleArrowLeftIcon,
  DoubleArrowRightIcon,
} from "@radix-ui/react-icons";

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Dispatch, memo, SetStateAction } from "react";
import { PageInfo } from "@/types/dto";
import { cn, isDeepEqual } from "@/lib/utils";

export interface DataTablePaginationTexts {
  pageSize: string;
  page: string;
  of: string;
  firstPage: string;
  lastPage: string;
  previousPage: string;
  nextPage: string;
}

interface DataTablePaginationProps extends DataTablePaginationTexts {
  pageInfo: PageInfo;
  setPageInfo: Dispatch<SetStateAction<PageInfo>>;
  sizeOptions?: number[];
  col?: boolean;
}

export const DataTablePagination = memo(function DataTablePagination({
  pageInfo,
  setPageInfo,
  sizeOptions = [5, 10, 20, 30, 40],
  pageSize,
  page,
  of,
  firstPage,
  lastPage,
  previousPage,
  nextPage,
  col = false,
}: DataTablePaginationProps) {
  const disableAll = pageInfo.totalPages === 0;
  return (
    <div className="flex items-center justify-end px-2 mt-2 overflow-hidden flex-wrap">
      <div
        className={cn(
          "flex items-center space-x-6 lg:space-x-8",
          col &&
            "flex-col justify-center items-start gap-3 space-x-0 lg:space-x-0",
        )}
      >
        <div className="flex items-center space-x-6 lg:space-x-8">
          <div className="flex items-center space-x-2">
            <p className="text-sm font-medium">{pageSize}</p>
            <Select
              value={`${pageInfo.pageSize}`}
              onValueChange={(value) => {
                setPageInfo((prev) => ({
                  ...prev,
                  pageSize: +value,
                  currentPage: 0,
                }));
              }}
            >
              <SelectTrigger className="h-8 w-[70px] !outline-none !ring-0">
                <SelectValue placeholder={pageInfo.pageSize} />
              </SelectTrigger>
              <SelectContent side="top">
                {sizeOptions.map((pageSize) => (
                  <SelectItem
                    key={pageSize}
                    value={`${pageSize}`}
                    className="cursor-pointer"
                  >
                    {pageSize}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div
            className={cn(
              "flex items-center justify-center text-sm font-medium text-center",
              !col && "min-w-[100px]",
            )}
          >
            {page} {pageInfo.currentPage + 1} {of}{" "}
            {pageInfo.totalPages !== 0 ? pageInfo.totalPages : 1}
          </div>
        </div>
        <div
          className={cn(
            "flex items-center space-x-2",
            col && "w-full justify-center ",
          )}
        >
          <Button
            variant="outline"
            className="hidden h-8 w-8 p-0 lg:flex"
            onClick={() => setPageInfo((prev) => ({ ...prev, currentPage: 0 }))}
            disabled={pageInfo.currentPage === 0 || disableAll}
          >
            <span className="sr-only">{firstPage}</span>
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
            disabled={pageInfo.currentPage === 0 || disableAll}
          >
            <span className="sr-only">{previousPage}</span>
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
            disabled={
              pageInfo.currentPage === pageInfo.totalPages - 1 || disableAll
            }
          >
            <span className="sr-only">{nextPage}</span>
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
            disabled={
              pageInfo.currentPage === pageInfo.totalPages - 1 || disableAll
            }
          >
            <span className="sr-only">{lastPage}</span>
            <DoubleArrowRightIcon className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}, areEqual);

function areEqual(
  prevProps: DataTablePaginationProps,
  nextProps: DataTablePaginationProps,
) {
  return (
    isDeepEqual(prevProps.pageInfo, nextProps.pageInfo) &&
    prevProps.setPageInfo === nextProps.setPageInfo
  );
}
