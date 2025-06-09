"use client";

import { Role } from "@/types/fetch-utils";
import React from "react";
import Editor, { EditorTexts } from "@/components/editor/editor";

interface Props {
  editorTexts: EditorTexts;
}
const minRole: Role = "ROLE_ADMIN";

export default function TestPage({ editorTexts }: Props) {
  return (
    <div style={{ padding: 20 }}>
      <Editor descritpion={""} onChange={() => {}} texts={editorTexts} />
    </div>
  );
}
