"use client";
import {
  createContext,
  Dispatch,
  ReactNode,
  SetStateAction,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { usePathname } from "@/navigation/navigation";
import { useEvent, usePrevious } from "react-use";
import { throttle } from "lodash-es";

interface Props {
  children: ReactNode;
}
export type ReindexState = Record<"column" | "task", number>;
interface KanbanRouteChangeContextType {
  setCallback: Dispatch<SetStateAction<((rs: ReindexState) => void) | null>>;
  callback: ((rs: ReindexState) => void) | null;
  reindexState: ReindexState;
  setReindexState: Dispatch<SetStateAction<ReindexState>>;
}

type KanbanReindexCallback = (rs: ReindexState) => void;

export const KanbanRouteChangeContext = createContext<
  KanbanRouteChangeContextType | undefined
>(undefined);
export function KanbanRouteChangeProvider({ children }: Props) {
  const [reindexState, setReindexState] = useState<ReindexState>({
    column: 0,
    task: 0,
  });

  const isReindexDirty = useRef(false);

  const setReindexStateDirty: Dispatch<SetStateAction<ReindexState>> =
    useCallback(
      (state) => {
        isReindexDirty.current = true;
        setReindexState(state);
      },
      [setReindexState],
    );

  const [callback, setCallback] = useState<KanbanReindexCallback | null>(null);
  const pathname = usePathname();
  const previousPathname = usePrevious(pathname);

  const callbackMemo = useCallback(() => {
    if (!callback || !isReindexDirty.current) return;
    // console.log("callbackMemo kanban", reindexState, isReindexDirty.current);
    try {
      callback(reindexState);
      isReindexDirty.current = false;
    } catch (e) {
      console.log("Error in KanbanRouteChangeProvider callbackMemo", e);
    }
  }, [callback, reindexState]);

  const laveCallbackMemo = useMemo(
    () =>
      throttle(() => {
        // console.log("laveCallbackMemo kanban");
        callbackMemo();
      }, 250),
    [callbackMemo],
  );

  const visibilityChangeHandler = useCallback(() => {
    if (document.visibilityState === "hidden") {
      laveCallbackMemo();
    }
  }, [laveCallbackMemo]);

  useEvent("beforeunload", laveCallbackMemo);
  useEvent("pagehide", laveCallbackMemo);
  useEvent("visibilitychange", visibilityChangeHandler);

  useEffect(() => {
    if (previousPathname?.includes("kanban") && !pathname.includes("kanban")) {
      // console.log("leaving kanban page", previousPathname, pathname);
      callbackMemo();
    }
  }, [
    callbackMemo,
    pathname,
    previousPathname,
    reindexState.column,
    reindexState.task,
  ]);

  return (
    <KanbanRouteChangeContext.Provider
      value={{
        setCallback,
        reindexState,
        setReindexState: setReindexStateDirty,
        callback,
      }}
    >
      {children}
    </KanbanRouteChangeContext.Provider>
  );
}

export function useKanbanRouteChange(cb: KanbanReindexCallback) {
  const ctx = useContext(KanbanRouteChangeContext);
  if (!ctx) {
    throw new Error(
      "useKanbanRouteChange must be used within a KanbanRouteChangeContext",
    );
  }

  useEffect(() => {
    ctx.setCallback(() => cb);
  }, [cb]);

  return {
    reindexState: ctx.reindexState,
    setReindexState: ctx.setReindexState,
  };
}
