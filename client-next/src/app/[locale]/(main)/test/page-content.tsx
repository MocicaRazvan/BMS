"use client";

import { IntlMetadata } from "@/texts/metadata";
import { Role } from "@/types/fetch-utils";
import Fuse, { FuseResult } from "fuse.js";
import { useEffect, useMemo, useRef, useState } from "react";
import { Input } from "@/components/ui/input";
import FindInSite, { FindInSiteTexts } from "@/components/nav/find-in-site";
import useFetchStream from "@/hoooks/useFetchStream";
import { Button } from "@/components/ui/button";
import ArchiveContent from "@/app/[locale]/admin/dashboard/archive-content";
import { AdminDashboardPageTexts } from "@/app/[locale]/admin/dashboard/page-content";
import { useLocale } from "next-intl";
import { Locale } from "@/navigation";
import { WithUser } from "@/lib/user";
import useGetQueueArchive from "@/hoooks/useGetQueueArchive";
import { ArchiveQueue, QueueInformation } from "@/types/dto";
import TopTrainers from "@/components/charts/top-trainers";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import Editor, { EditorTexts } from "@/components/editor/editor";
import EditorEmojiPicker from "@/components/editor/editor-emoji-picker";
import EmojiPicker from "emoji-picker-react";
interface Props extends WithUser {
  metadataValues: {
    metadata: IntlMetadata;
    key: string;
    path: string;
    role: Role | "ROLE_PUBLIC";
  }[];
  texts: AdminDashboardPageTexts;
  editorTexts: EditorTexts;
}
export default function TestPage({
  metadataValues,
  texts,
  authUser,
  editorTexts,
}: Props) {
  const locale = useLocale();
  const [renderCount, setRenderCount] = useState(0);
  const renderCounter = useRef(0);
  const [emoji, setEmoji] = useState("");
  renderCounter.current = renderCounter.current + 1;
  const deleteQueue = useFetchStream<QueueInformation>({
    path: "/archive/queue",
    queryParams: {
      refresh: `${false}`,
      queueName: ArchiveQueue.COMMENT_DELETE_QUEUE,
    },
  });
  const [previousFinished, setPreviousFinished] = useState<boolean[]>([]);
  const [prviousRefetchState, setPreviousRefetchState] = useState<boolean[]>(
    [],
  );
  const [funcResult, setFuncResult] = useState(0);

  useEffect(() => {
    setRenderCount((prev) => prev + 1);

    setPreviousFinished((prev) => [...prev, deleteQueue.isFinished]);
  }, [deleteQueue.isFinished]);

  useEffect(() => {
    setPreviousRefetchState((prev) => [...prev, deleteQueue.refetchState]);
  }, [deleteQueue.refetchState]);

  const badReq = async () => {
    fetchStream({
      path: "/csrf/serverError",
    })
      .then((res) => {
        console.log("Bad Request", res);
      })
      .catch((err) => {
        console.error("Bad Request", err);
      });
  };

  return (
    <div className="w-full mt-20 space-y-16">
      <EditorEmojiPicker
        onEmojiSelect={(e) => {
          console.log("Emoji", e);
          setEmoji(e);
        }}
        texts={editorTexts.editorToolbarTexts.editorEmojiPickerTexts}
      />
      <p>{emoji}</p>
      <Editor descritpion={"test"} onChange={() => {}} texts={editorTexts} />

      {deleteQueue.isFinished ? deleteQueue.messages.join(", ") : "Loading..."}
      <p>Render count: {renderCount}</p>

      <p>Previous Finished: {previousFinished.join(", ")}</p>

      <p>Previous Refetch State: {prviousRefetchState.join(", ")}</p>

      <Button onClick={() => deleteQueue.refetch()}>Refetch</Button>

      <Button onClick={badReq}>Bad Request</Button>

      {/*<TopTrainers texts={texts.topTrainersTexts} locale={locale as Locale} />*/}
    </div>
  );
}

const Main = () => {
  const { messages, isFinished, refetch } = useFetchStream({
    path: "/users/roles",
  });
  const {
    messages: mesages2,
    isFinished: isFinished2,
    refetch: refetch2,
  } = useFetchStream({
    path: "/users/roles",
  });
  const [showInner, setShowInner] = useState(false);

  useEffect(() => {
    setTimeout(() => {
      setShowInner(true);
    }, 5000);
  }, []);

  return (
    <div className="w-full mt-20">
      <p>{isFinished ? messages.join(", ") : "Loading..."}</p>
      <p>{isFinished2 ? mesages2.join(", ") : "Loading..."}</p>

      {showInner && <InnerComponent />}
      <Button
        onClick={() => {
          refetch();
          refetch2();
        }}
      >
        Refetch
      </Button>
    </div>
  );
};

const InnerComponent = () => {
  const { messages, isFinished, refetch } = useFetchStream({
    path: "/users/roles",
  });
  return (
    <div className="w-full mt-20">
      Inner
      <p>{isFinished ? messages.join(", ") : "Loading..."}</p>
      <Button onClick={() => refetch()}>Refetch</Button>
    </div>
  );
};
