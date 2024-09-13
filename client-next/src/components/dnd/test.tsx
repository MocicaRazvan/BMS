"use client";

import { useSubscription } from "react-stomp-hooks";
import { useState } from "react";
import { fetchStream } from "@/hoooks/fetchStream";
import { ApproveDto, ResponseWithUserDtoEntity } from "@/types/dto";
import { Button } from "@/components/ui/button";
import { invalidatePath } from "@/app/[locale]/(main)/test/test-actions";
const email = "razvanmocica1@gmail.com"; // Original email
const sanitizedEmail = email.replace("@", "-").replace(".", "-"); // Sanitize email
export default function TextComp() {
  const [messages, setMessages] = useState<any[]>([]);

  const handleClick = async () => {};

  return (
    <div>
      <Button onClick={() => invalidatePath()}>Invalidate Path</Button>
    </div>
  );
}
