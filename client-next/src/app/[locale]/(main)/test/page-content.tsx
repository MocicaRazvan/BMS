"use client";

import { Role } from "@/types/fetch-utils";
import React, { useRef } from "react";
import { EditorTexts } from "@/components/editor/editor";
import { useCounter } from "react-use";
import { ClientCacheInstance } from "@/providers/cache-provider";
import { Button } from "@/components/ui/button";
import {
  clearIndexedDB,
  deleteFromIndexedDB,
  saveToIndexedDB,
} from "@/lib/indexdb/idb-utils";

interface Props {
  editorTexts: EditorTexts;
}
const minRole: Role = "ROLE_ADMIN";

const randArray = () =>
  Array.from({ length: 3 }, () => Math.random().toString(36).substring(5));
const EmptyArry = [];
const cacheInstance = ClientCacheInstance.getInstance();
export default function TestPage({ editorTexts }: Props) {
  const key = useRef("asd");
  const [cnt, { inc }] = useCounter();
  // useEffect(() => {
  //   const restore = async () => {
  //     const expireBefore = Date.now() - 1000 * 60 * 10; // 10 minutes ago
  //     const entries = await loadCacheFromIndexedDB(expireBefore);
  //     console.log("Restored entries from IndexedDB:", entries);
  //     // const cache = ClientCacheInstance.getInstance();
  //     // for (const [key, value] of Object.entries(entries)) {
  //     //   cache.set(key, value);
  //     // }
  //   };
  //   void restore();
  // }, [cnt]);
  return (
    <div style={{ padding: 20 }} className="mx-auto space-y-5 space-x-5">
      <Button
        onClick={() => {
          key.current = Math.random().toString(36).substring(5);
          saveToIndexedDB(key.current, [randArray(), randArray()]);
        }}
      >
        Add to cache
      </Button>{" "}
      <Button
        onClick={() => {
          deleteFromIndexedDB(key.current);
        }}
      >
        Delete from cache
      </Button>
      <Button
        onClick={() => {
          inc();
        }}
      >
        Increment
      </Button>
      <Button
        onClick={() => {
          clearIndexedDB();
        }}
      >
        Delete db
      </Button>
    </div>
  );
}
