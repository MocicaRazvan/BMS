"use client";

import { ArchiveQueuesTableTexts } from "@/components/table/archive-queues-table";
import { useEffect, useRef } from "react";
import useGetUser from "@/hoooks/use-get-user";
import { Role } from "@/types/fetch-utils";

interface Props {
  texts: ArchiveQueuesTableTexts;
}
const minRole: Role = "ROLE_ADMIN";
export default function TestPage({ texts }: Props) {
  const { authUser, status } = useGetUser(minRole);
  const statusRef = useRef<string[]>([]);
  useEffect(() => {
    statusRef.current.push(status);
  }, [status]);
  return (
    <div className="flex flex-col items-center justify-center mt-20">
      {JSON.stringify(statusRef)} {minRole}
    </div>
  );
}
