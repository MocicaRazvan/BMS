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
import { Dispatch, memo, SetStateAction, useCallback, useMemo } from "react";

import { ExtraTableProps } from "@/types/tables";
import { format, parseISO } from "date-fns";
import { Link } from "@/navigation/navigation";
import {
  DataTable,
  DataTableProps,
  DataTableTexts,
} from "@/components/table/data-table";
import useList, { UseListProps } from "@/hoooks/useList";
import useTagsExtraCriteria, {
  TagsExtraCriteriaWithCallback,
  UseTagsExtraCriteriaTexts,
} from "@/components/list/useTagsExtraCriteria";
import { UseApprovedFilterTexts } from "@/components/list/useApprovedFilter";
import AlertDialogApprovePost from "@/components/dialogs/posts/approve-post";
import { ColumnActionsTexts } from "@/texts/components/table";
import useBinaryFilter, {
  RadioBinaryCriteriaWithCallback,
} from "@/components/list/useBinaryFilter";
import { postColumnActions } from "@/types/constants";
import useClientNotFound from "@/hoooks/useClientNotFound";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
import CreationFilter, {
  CreationFilterTexts,
} from "@/components/list/creation-filter";
import { wrapItemToString } from "@/lib/utils";
import {
  RadioSortButton,
  RadioSortDropDownWithExtra,
  RadioSortDropDownWithExtraDummy,
} from "@/components/common/radio-sort";
import AlertDialogDeletePost from "@/components/dialogs/posts/delete-post";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import { Option } from "@/components/ui/multiple-selector";
import { ListSearchInputProps } from "@/components/forms/input-serach";

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

type Props = ExtraTableProps & PostTableTexts & UseListProps;

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
  mainDashboard = false,
  creationFilterTexts,
}: Props) {
  const { authUser } = useAuthUserMinRole();

  const { extraUpdateSearchParams, extraArrayQueryParam, tags, setTags } =
    useTagsExtraCriteria();

  const {
    field,
    updateFieldSearch,
    setField: setApproved,
  } = useBinaryFilter({
    fieldKey: "approved",
  });

  const { navigateToNotFound } = useClientNotFound();

  const isAdmin = authUser?.role === "ROLE_ADMIN";

  const {
    pageInfo,
    filter,
    sort,
    setSort,
    sortValue,
    items,
    isFinished,
    error,
    setPageInfo,
    refetch,
    updateFilterValue,
    clearFilterValue,
    resetCurrentPage,
    updateUpdatedAtRange,
    updateCreatedAtRange,
    initialFilterValue,
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
    debounceDelay: 0,
  });
  const radioArgs = useMemo(
    () => ({
      setSort,
      sortingOptions,
      sortValue,
      callback: resetCurrentPage,
      filterKey: "title",
    }),
    [setSort, sortingOptions, sortValue, resetCurrentPage],
  );

  const data = useMemo(() => items.map((i) => i.content), [items]);

  const columns: ColumnDef<PostResponse>[] = useMemo(
    () => [
      {
        id: postTableColumnsTexts.id,
        accessorKey: "id",
        enableResizing: true,
        minSize: 35,
        size: 35,
        header: () => (
          <p className="font-bold text-lg text-left">
            {postTableColumnsTexts.id}
          </p>
        ),
        cell: ({
          row: {
            original: { id },
          },
        }) => (
          <OverflowTextTooltip
            text={wrapItemToString(id)}
            triggerClassName="w-10 max-w-10"
          />
        ),
      },
      {
        id: postTableColumnsTexts.title,
        accessorKey: "title",
        enableResizing: true,
        minSize: 160,
        size: 160,
        header: () => (
          <RadioSortButton sortingProperty="title" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {postTableColumnsTexts.title}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row, cell }) => (
          <OverflowTextTooltip
            text={row.original.title}
            triggerStyle={{
              maxWidth: `calc(var(--col-${cell.column.id}-size) * 1px - 10px)`,
            }}
          />
        ),
      },
      {
        id: postTableColumnsTexts.userLikes,
        accessorKey: "userLikes",
        enableResizing: true,
        header: () => (
          <RadioSortButton
            sortingProperty="userLikesLength"
            radioArgs={radioArgs}
          >
            <p className="font-bold text-lg text-left">
              {postTableColumnsTexts.userLikes}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => <p>{row.original.userLikes.length}</p>,
      },
      {
        id: postTableColumnsTexts.userDislikes,
        accessorKey: "userDislikes",
        enableResizing: true,
        header: () => (
          <RadioSortButton
            sortingProperty="userDislikesLength"
            radioArgs={radioArgs}
          >
            <p className="font-bold text-lg text-left">
              {postTableColumnsTexts.userDislikes}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => <p>{row.original.userDislikes.length}</p>,
      },
      {
        id: postTableColumnsTexts.createdAt,
        accessorKey: "createdAt",
        header: () => (
          <RadioSortDropDownWithExtra
            radioArgs={radioArgs}
            sortingProperty="createdAt"
            trigger={
              <p className="font-bold text-lg text-left">
                {postTableColumnsTexts.createdAt}
              </p>
            }
            showNone={false}
            extraContent={
              <CreationFilter
                triggerVariant="ghost"
                triggerClassName="px-5"
                {...creationFilterTexts}
                updateCreatedAtRange={updateCreatedAtRange}
                hideUpdatedAt={true}
                showLabels={false}
              />
            }
          />
        ),
        cell: ({ row }) => (
          <p>{format(parseISO(row.original.createdAt), "dd/MM/yyyy")}</p>
        ),
      },
      {
        id: postTableColumnsTexts.updatedAt,
        accessorKey: "updatedAt",
        header: () => (
          <RadioSortDropDownWithExtra
            radioArgs={radioArgs}
            sortingProperty="updatedAt"
            trigger={
              <p className="font-bold text-lg text-left">
                {postTableColumnsTexts.updatedAt}
              </p>
            }
            extraContent={
              <CreationFilter
                triggerVariant="ghost"
                triggerClassName="px-5"
                {...creationFilterTexts}
                updateUpdatedAtRange={updateUpdatedAtRange}
                hideCreatedAt={true}
                showLabels={false}
              />
            }
          />
        ),
        cell: ({ row }) => (
          <p>{format(parseISO(row.original.updatedAt), "dd/MM/yyyy")}</p>
        ),
      },
      {
        id: postTableColumnsTexts.approved.header,
        accessorKey: "approved",
        header: () => (
          <RadioSortDropDownWithExtraDummy
            trigger={
              <div className="font-bold text-lg text-left">
                {postTableColumnsTexts.approved.header}
              </div>
            }
            extraContent={
              <RadioBinaryCriteriaWithCallback
                callback={resetCurrentPage}
                fieldKey="approved"
                texts={{
                  trueText: useApprovedFilterTexts.approved,
                  falseText: useApprovedFilterTexts.notApproved,
                  all: useApprovedFilterTexts.all,
                }}
                setGlobalFilter={setApproved}
              />
            }
          />
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
        enableResizing: false,
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
                <DropdownMenuItem className="cursor-pointer" asChild>
                  <Link
                    href={
                      forWhom === "trainer"
                        ? `/trainer/posts/single/${row.original.id}`
                        : `/admin/posts/single/${row.original.id}`
                    }
                  >
                    {view}
                  </Link>
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
                      <DropdownMenuItem
                        asChild
                        onClick={(e) => {
                          e.stopPropagation();
                        }}
                        className="mt-5 py-2"
                      >
                        <AlertDialogDeletePost
                          post={row.original}
                          title={row.original.title}
                          token={authUser.token}
                          callBack={refetch}
                        />
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
      radioArgs,
      creationFilterTexts,
      updateCreatedAtRange,
      updateUpdatedAtRange,
      resetCurrentPage,
      useApprovedFilterTexts,
      setApproved,
      forWhom,
      mainDashboard,
      authUser,
      refetch,
      isAdmin,
    ],
  );

  const getRowId = useCallback(
    (row: PostResponse) => wrapItemToString(row.id),
    [],
  );

  const ExtraCriteria = useCallback(() => {
    return (
      <PostsExtraCriteria
        tags={tags}
        setTags={setTags}
        useTagsExtraCriteriaTexts={useTagsExtraCriteriaTexts}
        resetCurrentPage={resetCurrentPage}
      />
    );
  }, [tags, setTags, useTagsExtraCriteriaTexts, resetCurrentPage]);

  const searchInputProps: ListSearchInputProps = useMemo(
    () => ({
      initialValue: initialFilterValue,
      searchInputTexts: { placeholder: search },
      onChange: updateFilterValue,
      onClear: clearFilterValue,
    }),
    [clearFilterValue, initialFilterValue, search, updateFilterValue],
  );

  const radioSortProps = useMemo(
    () => ({
      setSort,
      sort,
      sortingOptions,
      sortValue,
      callback: resetCurrentPage,
      filterKey: "title",
    }),
    [setSort, sort, sortingOptions, sortValue, resetCurrentPage],
  );

  const chartProps: DataTableProps<PostResponse>["chartProps"] = useMemo(
    () => ({
      aggregatorConfig: {
        "#": (_) => 1,
        [postTableColumnsTexts.userLikes]: (p) => p.userLikes.length,
        [postTableColumnsTexts.userDislikes]: (p) => p.userDislikes.length,
        ["#" + postTableColumnsTexts.approved.header]: (p) =>
          Number(p.approved),
      },
      dateField: "createdAt",
    }),
    [
      postTableColumnsTexts.approved.header,
      postTableColumnsTexts.userDislikes,
      postTableColumnsTexts.userLikes,
    ],
  );

  if (error?.status) {
    return navigateToNotFound();
  }

  return (
    <div className="px-1 pb-10 w-full  h-full space-y-8 lg:space-y-14">
      <DataTable
        sizeOptions={sizeOptions}
        fileName={`posts-${authUser.email}`}
        isFinished={isFinished}
        columns={columns}
        data={data || []}
        pageInfo={pageInfo}
        setPageInfo={setPageInfo}
        getRowId={getRowId}
        {...dataTableTexts}
        useRadioSort={false}
        searchInputProps={searchInputProps}
        radioSortProps={radioSortProps}
        chartProps={chartProps}
        showChart={true}
        ExtraCriteria={ExtraCriteria}
      />
    </div>
  );
}

const PostsExtraCriteria = memo(
  ({
    useTagsExtraCriteriaTexts,
    tags,
    setTags,
    resetCurrentPage,
  }: {
    tags: Option[];
    setTags: Dispatch<SetStateAction<Option[]>>;
    useTagsExtraCriteriaTexts: UseTagsExtraCriteriaTexts;
    resetCurrentPage: () => void;
  }) => {
    return (
      <div className="flex items-start justify-center gap-8 flex-1 flex-wrap">
        <div className="flex-1 flex-wrap">
          <TagsExtraCriteriaWithCallback
            texts={useTagsExtraCriteriaTexts}
            setTags={setTags}
            tags={tags}
            callback={resetCurrentPage}
          />
        </div>
      </div>
    );
  },
);

PostsExtraCriteria.displayName = "PostsExtraCriteria";
