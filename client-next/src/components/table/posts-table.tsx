"use client";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

import { CustomEntityModel, PostResponse } from "@/types/dto";

import { ColumnDef } from "@tanstack/react-table";
import { MoreHorizontal } from "lucide-react";
import { Suspense, useMemo } from "react";

import { ExtraTableProps } from "@/types/tables";
import { format, parseISO } from "date-fns";
import { Link, useRouter } from "@/navigation";
import { DataTable, DataTableTexts } from "@/components/table/data-table";
import useList, { UseListProps } from "@/hoooks/useList";
import LoadingSpinner from "@/components/common/loading-spinner";
import useTagsExtraCriteria, {
  UseTagsExtraCriteriaTexts,
} from "@/components/list/useTagsExtraCriteria";
import { UseApprovedFilterTexts } from "@/components/list/useApprovedFilter";
import { WithUser } from "@/lib/user";
import AlertDialogApprovePost from "@/components/dialogs/posts/approve-post";
import { ColumnActionsTexts } from "@/texts/components/table";
import useBinaryFilter from "@/components/list/useBinaryFilter";
import { postColumnActions } from "@/lib/constants";
import useClientNotFound from "@/hoooks/useClientNotFound";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
import CreationFilter, {
  CreationFilterTexts,
} from "@/components/list/creation-filter";

export interface PostTableColumnsTexts {
  id: string;
  title: string;
  userLikes: string;
  userDislikes: string;
  createdAt: string;
  updatedAt: string;
  approved: {
    header: string;
    true: string;
    false: string;
  };
  actions: ColumnActionsTexts<typeof postColumnActions>;
}

export interface PostTableTexts {
  dataTableTexts: DataTableTexts;
  useApprovedFilterTexts: UseApprovedFilterTexts;
  useTagsExtraCriteriaTexts: UseTagsExtraCriteriaTexts;
  postTableColumnsTexts: PostTableColumnsTexts;
  search: string;
  creationFilterTexts: CreationFilterTexts;
}

type Props = ExtraTableProps & PostTableTexts & UseListProps & WithUser;

export default function PostsTable({
  forWhom,
  dataTableTexts,
  path,
  extraQueryParams,
  sizeOptions,
  sortingOptions,
  useApprovedFilterTexts,
  useTagsExtraCriteriaTexts,
  search,
  postTableColumnsTexts,
  authUser,
  mainDashboard = false,
  creationFilterTexts,
}: Props) {
  const {
    extraUpdateSearchParams,
    extraCriteria,
    extraArrayQueryParam,
    extraCriteriaWithCallBack,
  } = useTagsExtraCriteria(useTagsExtraCriteriaTexts);
  // const { updateApprovedSearch, approveCriteria, approved } = useApprovedFilter(
  //   useApprovedFilterTexts,
  // );
  const { fieldCriteria, field, updateFieldSearch, fieldCriteriaCallBack } =
    useBinaryFilter({
      fieldKey: "approved",
      trueText: useApprovedFilterTexts.approved,
      falseText: useApprovedFilterTexts.notApproved,
      all: useApprovedFilterTexts.all,
    });

  const { navigateToNotFound } = useClientNotFound();

  const router = useRouter();
  const isAdmin = authUser?.role === "ROLE_ADMIN";
  // console.log("AUTH", authUser);
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
    updateUpdatedAtRange,
    updateCreatedAtRange,
  } = useList<CustomEntityModel<PostResponse>>({
    path,
    extraQueryParams: {
      ...(extraQueryParams && extraQueryParams),
      ...(field !== null && { approved: field.toString() }),
    },
    extraArrayQueryParam,
    extraUpdateSearchParams: (p) => {
      extraUpdateSearchParams(p);
      updateFieldSearch(p);
    },
    sizeOptions,
    sortingOptions,
  });

  const data = useMemo(() => items.map((i) => i.content), [items]);

  const columns: ColumnDef<PostResponse>[] = useMemo(
    () => [
      {
        id: postTableColumnsTexts.id,
        accessorKey: "id",
        header: () => (
          <p className="font-bold text-lg text-left">
            {postTableColumnsTexts.id}
          </p>
        ),
      },
      {
        id: postTableColumnsTexts.title,
        accessorKey: "title",
        header: () => (
          <p className="font-bold text-lg text-left">
            {postTableColumnsTexts.title}
          </p>
        ),
        cell: ({ row }) => <OverflowTextTooltip text={row.original.title} />,
      },
      {
        id: postTableColumnsTexts.userLikes,
        accessorKey: "userLikes",
        header: () => (
          <p className="font-bold text-lg text-left ">
            {postTableColumnsTexts.userLikes}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.userLikes.length}</p>,
      },
      {
        id: postTableColumnsTexts.userDislikes,
        accessorKey: "userDislikes",
        header: () => (
          <p className="font-bold text-lg text-left">
            {postTableColumnsTexts.userDislikes}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.userDislikes.length}</p>,
      },
      {
        id: postTableColumnsTexts.createdAt,
        accessorKey: "createdAt",
        header: () => (
          <p className="font-bold text-lg text-left">
            {postTableColumnsTexts.createdAt}
          </p>
        ),
        cell: ({ row }) => (
          <p>{format(parseISO(row.original.createdAt), "dd/MM/yyyy")}</p>
        ),
      },
      {
        id: postTableColumnsTexts.updatedAt,
        accessorKey: "updatedAt",
        header: () => (
          <p className="font-bold text-lg text-left">
            {postTableColumnsTexts.updatedAt}
          </p>
        ),
        cell: ({ row }) => (
          <p>{format(parseISO(row.original.updatedAt), "dd/MM/yyyy")}</p>
        ),
      },
      {
        id: postTableColumnsTexts.approved.header,
        accessorKey: "approved",
        header: () => (
          <div className="font-bold text-lg text-left">
            {postTableColumnsTexts.approved.header}
          </div>
        ),
        cell: ({ row }) => (
          <Badge variant={row.original.approved ? "success" : "destructive"}>
            {
              postTableColumnsTexts.approved[
                row.original.approved.toString() as "true" | "false"
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
            duplicate,
            viewOwnerItems,
          } = postTableColumnsTexts.actions;

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
                        ? `/trainer/posts/single/${row.original.id}`
                        : `/admin/posts/single/${row.original.id}`,
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
                            ? `/admin/users/${row.original.userId}`
                            : `/users/single/${row.original.userId}`
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
                            href={`/admin/users/${row.original.userId}/posts`}
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
                  parseInt(authUser.id) === row.original.userId && (
                    <>
                      <DropdownMenuItem asChild>
                        <Link
                          className="cursor-pointer"
                          href={`/trainer/posts/update/${row.original.id}`}
                        >
                          {update}
                        </Link>
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem asChild>
                        <Link
                          className="cursor-pointer"
                          href={`/trainer/posts/duplicate/${row.original.id}`}
                        >
                          {duplicate}
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
                    <AlertDialogApprovePost
                      post={row.original}
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
      postTableColumnsTexts.id,
      postTableColumnsTexts.title,
      postTableColumnsTexts.userLikes,
      postTableColumnsTexts.userDislikes,
      postTableColumnsTexts.createdAt,
      postTableColumnsTexts.updatedAt,
      postTableColumnsTexts.approved,
      postTableColumnsTexts.actions,
      forWhom,
      mainDashboard,
      authUser,
      isAdmin,
      refetch,
      router,
    ],
  );

  if (error?.status) {
    return navigateToNotFound();
  }

  return (
    <div className="px-1 pb-10 w-full  h-full space-y-8 lg:space-y-14">
      <Suspense fallback={<LoadingSpinner />}>
        <DataTable
          sizeOptions={sizeOptions}
          fileName={`posts-${authUser.email}`}
          isFinished={isFinished}
          columns={columns}
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
            filterKey: "title",
          }}
          extraCriteria={
            <div className="flex items-start justify-center gap-8 flex-1 flex-wrap">
              <div className="flex-1 flex-wrap">
                {extraCriteriaWithCallBack(resetCurrentPage)}
              </div>
              {fieldCriteriaCallBack(resetCurrentPage)}
            </div>
          }
          rangeDateFilter={
            <CreationFilter
              {...creationFilterTexts}
              updateCreatedAtRange={updateCreatedAtRange}
              updateUpdatedAtRange={updateUpdatedAtRange}
            />
          }
        />
      </Suspense>
    </div>
  );
}
