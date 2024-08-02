"use client";

import PostsTable, { PostTableTexts } from "@/components/table/posts-table";
import { SortingOptionsTexts } from "@/lib/constants";
import { SortingOption } from "@/hoooks/useList";
import { WithUser } from "@/lib/user";

export interface UserPostsPageContentTexts {
  postTableTexts: PostTableTexts;
  sortingPostsSortingOptions: SortingOptionsTexts;
  title: string;
  header: string;
}

interface Props extends UserPostsPageContentTexts, WithUser {
  id: string;
  sortingOptions: SortingOption[];
}

export default function UserPostsPageContent({
  sortingPostsSortingOptions,
  postTableTexts,
  sortingOptions,
  id,
  authUser,
}: Props) {
  return (
    <PostsTable
      path={`/posts/trainer/tags/${id}`}
      forWhom="trainer"
      {...postTableTexts}
      sortingOptions={sortingOptions}
      sizeOptions={[10, 20, 30, 40]}
      authUser={authUser}
    />
  );
}
