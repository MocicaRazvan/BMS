import {
  CommentFormTexts,
  getCommentFormTexts,
} from "@/texts/components/forms";
import { getTranslations } from "next-intl/server";
import { CommentAccordionTexts } from "@/components/posts/comment-accordion";
import { getEditorTexts } from "@/texts/components/editor";
import { EditorTexts } from "@/components/editor/editor";

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
  deleteCommentDialog: string;
  editCommentLabel: string;
  editorTexts: EditorTexts;
}

export async function getPostCommentsTexts(): Promise<PostCommentsTexts> {
  const [t, commentsFormTexts, commentAccordionTexts, editorTexts] =
    await Promise.all([
      getTranslations("components.posts.PostCommentsTexts"),
      getCommentFormTexts(),
      getCommentAccordionTexts(),
      getEditorTexts(),
    ]);
  return {
    editorTexts,
    author: t("author"),
    loadMore: t("loadMore"),
    commentsFormTexts,
    sortLabel: t("sortLabel"),
    newest: t("newest"),
    oldest: t("oldest"),
    edited: t("edited"),
    editHeader: t("editHeader"),
    commentAccordionTexts,
    deleteCommentDialog: t("deleteCommentDialog"),
    editCommentLabel: t("editCommentLabel"),
  };
}
export async function getCommentAccordionTexts(): Promise<CommentAccordionTexts> {
  const [t, commentsFormTexts, editorTexts] = await Promise.all([
    getTranslations("components.posts.CommentAccordionTexts"),
    getCommentFormTexts(),
    getEditorTexts(),
  ]);
  return {
    editorTexts,
    commentFormTexts: commentsFormTexts,
    englishError: t("englishError"),
    toxicError: t("toxicError"),
    englishHeading: t("englishHeading"),
  };
}
