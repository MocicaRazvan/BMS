"use client";

import PostsTable, { PostTableTexts } from "@/components/table/posts-table";
import { SortingOptionsTexts } from "@/lib/constants";
import { SortingOption } from "@/hoooks/useList";

export interface UserPostsPageContentTexts {
  postTableTexts: PostTableTexts;
  sortingPostsSortingOptions: SortingOptionsTexts;
  title: string;
  header: string;
}

interface Props extends UserPostsPageContentTexts {
  id: string;
  sortingOptions: SortingOption[];
}

export default function UserPostsPageContent({
  sortingPostsSortingOptions,
  postTableTexts,
  sortingOptions,
  id,
}: Props) {
  return (
    <PostsTable
      path={`/posts/trainer/tags/${id}`}
      forWhom="trainer"
      {...postTableTexts}
      sortingOptions={sortingOptions}
      sizeOptions={[10, 20, 30, 40]}
    />
  );
}
