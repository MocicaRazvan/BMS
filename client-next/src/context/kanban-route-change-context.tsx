"use client";
import {
  createContext,
  Dispatch,
  ReactNode,
  SetStateAction,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { usePathname } from "@/navigation";

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

export const KanbanRouteChangeContext = createContext<
  KanbanRouteChangeContextType | undefined
>(undefined);
export function KanbanRouteChangeProvider({ children }: Props) {
  const [reindexState, setReindexState] = useState<ReindexState>({
    column: 0,
    task: 0,
  });

  const [callback, setCallback] = useState<((rs: ReindexState) => void) | null>(
    null,
  );
  const pathname = usePathname();
  const [currentPathname, setCurrentPathname] = useState<string | null>(
    pathname,
  );
  const [oldPathname, setOldPathname] = useState<string | null>(null);
  const callbackMemo = useCallback(() => {
    if (!callback) return;
    callback(reindexState);
  }, [reindexState.column, reindexState.task, callback]);

  useEffect(() => {
    setOldPathname(currentPathname);
    setCurrentPathname(pathname);
  }, [pathname]);

  useEffect(() => {
    window.addEventListener("beforeunload", callbackMemo);
    window.addEventListener("pagehide", callbackMemo);

    return () => {
      window.removeEventListener("beforeunload", callbackMemo);
      window.removeEventListener("pagehide", callbackMemo);
    };
  }, [reindexState.column, reindexState.task, callbackMemo]);

  useEffect(() => {
    if (
      oldPathname?.includes("kanban") &&
      !currentPathname?.includes("kanban")
    ) {
      console.log("leaving kanban page", oldPathname, currentPathname);
      callbackMemo();
    }
  }, [
    currentPathname,
    oldPathname,
    callbackMemo,
    reindexState.column,
    reindexState.task,
  ]);

  return (
    <KanbanRouteChangeContext.Provider
      value={{ setCallback, reindexState, setReindexState, callback }}
    >
      {children}
    </KanbanRouteChangeContext.Provider>
  );
}

export function useKanbanRouteChange(cb: (rs: ReindexState) => void) {
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
