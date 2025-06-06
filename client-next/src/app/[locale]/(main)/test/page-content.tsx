"use client";

import { Role } from "@/types/fetch-utils";
import useList from "@/hoooks/useList";
import { PostResponse, ResponseWithUserDtoEntity } from "@/types/dto";
import { SortingOption } from "@/components/list/grid-list";

interface Props {
  options: SortingOption[];
}
const minRole: Role = "ROLE_ADMIN";
export default function TestPage({ options }: Props) {
  const { messages } = useList<ResponseWithUserDtoEntity<PostResponse>>({
    path: "/posts/tags/withUser",
    sortingOptions: options,
  });
  return (
    <div className="flex flex-col items-center justify-center mt-20">
      {messages.length > 0 ? (
        messages.map((message) => (
          <div key={message.content.model.content.id} className="mb-4">
            <h2 className="text-xl font-bold">
              {message.content.model.content.title}
            </h2>
          </div>
        ))
      ) : (
        <p>No posts available.</p>
      )}
    </div>
  );
}
