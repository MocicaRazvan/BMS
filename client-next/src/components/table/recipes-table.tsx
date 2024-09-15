"use client";

import { ColumnActionsTexts } from "@/texts/components/table";
import { recipeColumnActions } from "@/lib/constants";
import { DataTable, DataTableTexts } from "@/components/table/data-table";
import { UseApprovedFilterTexts } from "@/components/list/useApprovedFilter";
import useFilterDropdown, {
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import { ExtraTableProps } from "@/types/tables";
import useList, { UseListProps } from "@/hoooks/useList";
import { WithUser } from "@/lib/user";
import useBinaryFilter from "@/components/list/useBinaryFilter";
import { dietTypes } from "@/types/forms";
import { Link, useRouter } from "@/navigation";
import {
  CustomEntityModel,
  IngredientNutritionalFactResponse,
  PostResponse,
  RecipeResponse,
  ResponseWithEntityCount,
} from "@/types/dto";
import { Suspense, useMemo } from "react";
import { ColumnDef } from "@tanstack/react-table";
import { format, parseISO } from "date-fns";
import { Badge } from "@/components/ui/badge";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { MoreHorizontal } from "lucide-react";
import AlertDialogApprovePost from "@/components/dialogs/posts/approve-post";
import LoadingSpinner from "@/components/common/loading-spinner";
import AlertDialogApproveRecipes from "@/components/dialogs/recipes/approve-recipe";
import useClientNotFound from "@/hoooks/useClientNotFound";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";

export interface RecipeTableColumnsTexts {
  id: string;
  title: string;
  userLikes: string;
  userDislikes: string;
  createdAt: string;
  updatedAt: string;
  type: string;
  count: string;
  approved: {
    header: string;
    true: string;
    false: string;
  };
  actions: ColumnActionsTexts<typeof recipeColumnActions>;
}

export interface RecipeTableTexts {
  dataTableTexts: DataTableTexts;
  useApprovedFilterTexts: UseApprovedFilterTexts;
  dietDropdownTexts: UseFilterDropdownTexts;
  recipeTableColumnsTexts: RecipeTableColumnsTexts;
  search: string;
}

interface Props
  extends ExtraTableProps,
    RecipeTableTexts,
    UseListProps,
    WithUser {
  isSidebarOpen?: boolean;
}

export default function RecipeTable({
  forWhom,
  dataTableTexts,
  path,
  extraQueryParams,
  sizeOptions,
  sortingOptions,
  useApprovedFilterTexts,
  dietDropdownTexts,
  search,
  recipeTableColumnsTexts,
  authUser,
  mainDashboard = false,
  extraArrayQueryParam,
  isSidebarOpen = false,
}: Props) {
  const {
    field: approvedField,
    updateFieldSearch: updateApproveField,
    fieldCriteriaCallBack: approvedFieldCriteriaCallBack,
  } = useBinaryFilter({
    fieldKey: "approved",
    trueText: useApprovedFilterTexts.approved,
    falseText: useApprovedFilterTexts.notApproved,
    all: useApprovedFilterTexts.all,
  });

  const {
    value: dietType,
    updateFieldDropdownFilter: updateDietType,
    filedFilterCriteriaCallback: dietTypeCriteriaCallback,
  } = useFilterDropdown({
    items: dietTypes.map((value) => ({
      value,
      label: dietDropdownTexts.labels[value],
    })),
    fieldKey: "type",
    noFilterLabel: dietDropdownTexts.noFilterLabel,
  });

  const { navigateToNotFound } = useClientNotFound();

  const router = useRouter();
  const isAdmin = authUser?.role === "ROLE_ADMIN";

  const {
    messages,
    pageInfo,
    filter,
    setFilter,
    debouncedFilter,
    sort,
    setSort,
    sortValue,
    setSortValue,
    items,
    updateSortState,
    isFinished,
    error,
    setPageInfo,
    refetch,
    updateFilterValue,
    clearFilterValue,
    resetCurrentPage,
  } = useList<ResponseWithEntityCount<CustomEntityModel<RecipeResponse>>>({
    path,
    extraQueryParams: {
      ...(extraQueryParams && extraQueryParams),
      ...(approvedField !== null && { approved: approvedField.toString() }),
      ...(dietType && { type: dietType }),
    },
    extraArrayQueryParam,
    extraUpdateSearchParams: (p) => {
      updateDietType(p);
      updateApproveField(p);
      updateDietType(p);
    },
    sizeOptions,
    sortingOptions,
  });

  const data: ResponseWithEntityCount<RecipeResponse>[] = useMemo(
    () =>
      items.map((i) => ({
        model: i.model.content,
        count: i.count,
      })),
    [items],
  );

  const columns: ColumnDef<ResponseWithEntityCount<RecipeResponse>>[] = useMemo(
    () => [
      {
        id: recipeTableColumnsTexts.id,
        accessorKey: "model.id",
        header: () => (
          <p className="font-bold text-lg text-left">
            {recipeTableColumnsTexts.id}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.id}</p>,
      },
      {
        id: recipeTableColumnsTexts.title,
        accessorKey: "model.title",
        header: () => (
          <p className="font-bold text-lg text-left">
            {recipeTableColumnsTexts.title}
          </p>
        ),
        cell: ({ row }) => (
          <OverflowTextTooltip text={row.original.model.title} />
        ),
      },
      {
        id: recipeTableColumnsTexts.type,
        accessorKey: "model.type",
        header: () => (
          <p className="font-bold text-lg text-left">
            {recipeTableColumnsTexts.type}
          </p>
        ),
        cell: ({ row }) => {
          const colorMap = {
            CARNIVORE: "destructive",
            VEGAN: "success",
            OMNIVORE: "default",
            VEGETARIAN: "accent",
          };

          return (
            <Badge
              variant={
                colorMap[row.original.model.type] as
                  | "default"
                  | "destructive"
                  | "success"
                  | "accent"
                  | "secondary"
                  | "outline"
                  | null
                  | undefined
              }
            >
              {row.original.model.type}
            </Badge>
          );
        },
      },
      {
        id: recipeTableColumnsTexts.count,
        accessorKey: "count",
        header: () => (
          <p className="font-bold text-lg text-left">
            {recipeTableColumnsTexts.count}
          </p>
        ),
        cell: ({ row }) => (
          <div className="max-w-16 text-nowrap overflow-x-hidden">
            <p>{row.original.count}</p>
          </div>
        ),
      },
      {
        id: recipeTableColumnsTexts.userLikes,
        accessorKey: "model.userLikes",
        header: () => (
          <p className="font-bold text-lg text-left">
            {recipeTableColumnsTexts.userLikes}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.userLikes.length}</p>,
      },
      {
        id: recipeTableColumnsTexts.userDislikes,
        accessorKey: "model.userDislikes",
        header: () => (
          <p className="font-bold text-lg text-left">
            {recipeTableColumnsTexts.userDislikes}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.userDislikes.length}</p>,
      },
      {
        id: recipeTableColumnsTexts.createdAt,
        accessorKey: "model.createdAt",
        header: () => (
          <p className="font-bold text-lg text-left">
            {recipeTableColumnsTexts.createdAt}
          </p>
        ),
        cell: ({ row }) => (
          <p>{format(parseISO(row.original.model.createdAt), "dd/MM/yyyy")}</p>
        ),
      },
      {
        id: recipeTableColumnsTexts.updatedAt,
        accessorKey: "model.updatedAt",
        header: () => (
          <p className="font-bold text-lg text-left">
            {recipeTableColumnsTexts.updatedAt}
          </p>
        ),
        cell: ({ row }) => (
          <p>{format(parseISO(row.original.model.updatedAt), "dd/MM/yyyy")}</p>
        ),
      },
      {
        id: recipeTableColumnsTexts.approved.header,
        accessorKey: "model.approved",
        header: () => (
          <div className="font-bold text-lg text-left">
            {recipeTableColumnsTexts.approved.header}
          </div>
        ),
        cell: ({ row }) => (
          <Badge
            variant={row.original.model.approved ? "success" : "destructive"}
          >
            {
              recipeTableColumnsTexts.approved[
                row.original.model.approved.toString() as "true" | "false"
              ]
            }
          </Badge>
        ),
      },
      {
        id: "actions",
        cell: ({ row }) => {
          const {
            button,
            disapprove,
            label,
            view,
            viewOwner,
            update,
            approve,
            viewOwnerItems,
          } = recipeTableColumnsTexts.actions;

          return (
            <DropdownMenu modal>
              <DropdownMenuTrigger asChild>
                <Button
                  variant="ghost"
                  className="h-8 w-8 p-0 hover:bg-background"
                >
                  <span className="sr-only">{button}</span>
                  <MoreHorizontal className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuLabel className="mb-3">{label}</DropdownMenuLabel>
                <DropdownMenuItem
                  className="cursor-pointer"
                  onClick={() =>
                    router.push(
                      forWhom === "trainer"
                        ? `/trainer/recipes/single/${row.original.model.id}`
                        : `/admin/recipes/single/${row.original.model.id}`,
                    )
                  }
                >
                  {view}
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                {!(forWhom === "trainer") && (
                  <>
                    <DropdownMenuItem asChild>
                      <Link
                        href={
                          mainDashboard
                            ? `/admin/users/${row.original.model.userId}`
                            : `/users/single/${row.original.model.userId}`
                        }
                        className="cursor-pointer"
                      >
                        {viewOwner}
                      </Link>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    {mainDashboard && (
                      <>
                        <DropdownMenuItem asChild>
                          <Link
                            href={`/admin/users/${row.original.model.userId}/recipes`}
                            className="cursor-pointer"
                          >
                            {viewOwnerItems}
                          </Link>
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                      </>
                    )}
                  </>
                )}

                {forWhom === "trainer" &&
                  parseInt(authUser.id) === row.original.model.userId && (
                    <>
                      <DropdownMenuItem asChild>
                        <Link
                          className="cursor-pointer"
                          href={`/trainer/recipes/update/${row.original.model.id}`}
                        >
                          {update}
                        </Link>
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                    </>
                  )}

                <div className="h-1" />
                {isAdmin && (
                  <DropdownMenuItem
                    asChild
                    onClick={(e) => {
                      e.stopPropagation();
                    }}
                    className="mt-5 py-2"
                  >
                    <AlertDialogApproveRecipes
                      recipe={row.original.model}
                      authUser={authUser}
                      callBack={refetch}
                    />
                  </DropdownMenuItem>
                )}
              </DropdownMenuContent>
            </DropdownMenu>
          );
        },
      },
    ],
    [
      recipeTableColumnsTexts.id,
      recipeTableColumnsTexts.title,
      recipeTableColumnsTexts.type,
      recipeTableColumnsTexts.count,
      recipeTableColumnsTexts.userLikes,
      recipeTableColumnsTexts.userDislikes,
      recipeTableColumnsTexts.createdAt,
      recipeTableColumnsTexts.updatedAt,
      recipeTableColumnsTexts.approved,
      recipeTableColumnsTexts.actions,
      forWhom,
      mainDashboard,
      authUser,
      isAdmin,
      refetch,
      router,
    ],
  );

  const finalCols = useMemo(
    () => (isSidebarOpen ? columns.slice(0, -3) : columns),
    [columns, isSidebarOpen],
  );

  if (error?.status) {
    return navigateToNotFound();
  }

  return (
    <div className="px-1 w-full space-y-8 lg:space-y-14 overflow-x-hidden ">
      <Suspense fallback={<LoadingSpinner />}>
        <DataTable
          sizeOptions={sizeOptions}
          fileName="recipes"
          isFinished={isFinished}
          columns={finalCols}
          data={data || []}
          pageInfo={pageInfo}
          setPageInfo={setPageInfo}
          {...dataTableTexts}
          searchInputProps={{
            value: filter.title || "",
            searchInputTexts: { placeholder: search },
            onChange: updateFilterValue,
            onClear: clearFilterValue,
          }}
          radioSortProps={{
            setSort,
            sort,
            sortingOptions,
            setSortValue,
            sortValue,
            callback: resetCurrentPage,
          }}
          extraCriteria={
            <div className="flex items-start justify-center gap-8 flex-1 flex-wrap">
              <div className="flex items-center justify-end gap-4 flex-1 flex-wrap">
                {dietTypeCriteriaCallback(resetCurrentPage)}
                {approvedFieldCriteriaCallBack(resetCurrentPage)}
              </div>
            </div>
          }
        />
      </Suspense>
    </div>
  );
}
