"use client";

import { Role } from "@/types/fetch-utils";
import React, {
  useCallback,
  useEffect,
  useRef,
  useState,
  useSyncExternalStore,
} from "react";
import Editor, { EditorTexts } from "@/components/editor/editor";
import { useMountedState } from "react-use";
import {
  ClientCacheInstance,
  useCacheInstance,
} from "@/providers/cache-provider";
import { useSyncExternalStoreWithSelector } from "use-sync-external-store/with-selector";
import { isDeepEqual } from "@/lib/utils";
import { Button } from "@/components/ui/button";

interface Props {
  editorTexts: EditorTexts;
}
const minRole: Role = "ROLE_ADMIN";

const key = "key";
const subscribe = (s: () => void) => {
  const cacheInstance = ClientCacheInstance.getInstance();
  return cacheInstance.subscribe(key, s);
};

const randArray = () =>
  Array.from({ length: 3 }, () => Math.random().toString(36).substring(5));
const EmptyArry = [];
const cacheInstance = ClientCacheInstance.getInstance();
export default function TestPage({ editorTexts }: Props) {
  const [key, setKey] = useState<string>("key");
  const [dummyState, setDummyState] = useState<boolean>(false);

  const [manualFlat, setManualFlat] = useState<string[]>();

  const getSnapshot = useCallback(() => {
    console.log("getSnapshot called");
    return ClientCacheInstance.getInstance().getRaw(key);
  }, [key]);

  const value = useSyncExternalStoreWithSelector(
    subscribe,
    getSnapshot,
    getSnapshot,
    (a) => a?.flat(),
  );

  return (
    <div style={{ padding: 20 }} className="mx-auto space-y-5 space-x-5">
      {/*<Editor descritpion={""} onChange={() => {}} texts={editorTexts} />*/}
      <Button
        onClick={() => {
          cacheInstance.set(key, [randArray(), randArray()]);
        }}
      >
        RAND
      </Button>
      <Button
        onClick={() => {
          cacheInstance.remove(key);
        }}
      >
        Delete
      </Button>
      <Button
        onClick={() => {
          cacheInstance.replaceBatch(key, randArray(), 0);
        }}
      >
        Replace Batch
      </Button>
      <Button
        onClick={() => {
          setKey(Math.random().toString(36).substring(2, 15));
        }}
      >
        Random key
      </Button>
      <Button
        onClick={() => {
          setDummyState((prev) => !prev);
        }}
      >
        Toggle Dummy State
      </Button>
      <div>
        <h1>Test Page</h1>
        <p>Current value in cache:</p>
        <pre>{JSON.stringify(value, null, 2)}</pre>
      </div>
      <div>
        <h2>Manual Flat Cache</h2>
        <pre>{JSON.stringify(manualFlat, null, 2)}</pre>
        <Button
          onClick={() => {
            setManualFlat(() => {
              const a = cacheInstance.get<string>(key);
              return a || [];
            });
          }}
        >
          Set Manual Flat
        </Button>
      </div>
    </div>
  );
}
