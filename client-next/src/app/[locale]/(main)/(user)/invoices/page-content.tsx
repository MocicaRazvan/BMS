"use client";

import { WithUser } from "@/lib/user";
import useFetchStream from "@/hoooks/useFetchStream";
import { InvoiceResponse } from "@/types/dto";
import { format } from "date-fns";

interface Props extends WithUser {}

export default function InvoicePageContent({ authUser }: Props) {
  const { messages, error } = useFetchStream<InvoiceResponse>({
    path: `/orders/invoices/list`,
    method: "PATCH",
    authToken: true,
    body: {
      page: 0,
      size: 10, // 1
    },
  });
  //todo delete this
  console.log("invoices", messages);
  console.log("invoices error", error);

  if (!messages.length) return <div>No Invoices</div>;
  return (
    <div>
      {messages[0].invoices.map(({ url, number, creationDate }) => (
        <div key={number}>
          <a href={url} target={"_blank"}>
            {format(
              new Date(parseInt(creationDate) * 1000),
              "yyyy-MM-dd HH:mm:ss",
            )}
          </a>
        </div>
      ))}
    </div>
  );
}
