import {
  CommentFormTexts,
  getCommentFormTexts,
} from "@/texts/components/forms";
import { getTranslations } from "next-intl/server";
import { CommentAccordionTexts } from "@/components/posts/comment-accordion";

export interface PostCommentsTexts {
  author: string;
  loadMore: string;
  commentsFormTexts: CommentFormTexts;
  sortLabel: string;
  newest: string;
  oldest: string;
  edited: string;
  editHeader: string;
  commentAccordionTexts: CommentAccordionTexts;
}

export async function getPostCommentsTexts(): Promise<PostCommentsTexts> {
  const [t, commentsFormTexts, commentAccordionTexts] = await Promise.all([
    getTranslations("components.posts.PostCommentsTexts"),
    getCommentFormTexts(),
    getCommentAccordionTexts(),
  ]);
  return {
    author: t("author"),
    loadMore: t("loadMore"),
    commentsFormTexts,
    sortLabel: t("sortLabel"),
    newest: t("newest"),
    oldest: t("oldest"),
    edited: t("edited"),
    editHeader: t("editHeader"),
    commentAccordionTexts,
  };
}
export async function getCommentAccordionTexts(): Promise<CommentAccordionTexts> {
  const [t, commentsFormTexts] = await Promise.all([
    getTranslations("components.posts.CommentAccordionTexts"),
    getCommentFormTexts(),
  ]);
  return {
    commentFormTexts: commentsFormTexts,
    englishError: t("englishError"),
    toxicError: t("toxicError"),
    englishHeading: t("englishHeading"),
  };
}
