"use client";

import { WithUser } from "@/lib/user";

import { ArchiveQueuesTableTexts } from "@/components/table/archive-queues-table";

interface Props extends WithUser {
  texts: ArchiveQueuesTableTexts;
}
export default function TestPage({ authUser, texts }: Props) {
  return (
    <div className="flex flex-col items-center justify-center mt-20"></div>
  );
}
