"use client";
import { fetchEventSource } from "@microsoft/fetch-event-source";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { useChat } from "ai/react";
import { choseRandomItemsFromArray, choseRandomNumber } from "@/lib/utils";
import { tags } from "@/lib/constants";

export default function TestPageContent() {
  const handleClick = async () => {
    const randomTags = choseRandomItemsFromArray<string>(
      tags as unknown as string[],
      choseRandomNumber(0, tags.length - 1),
    );
    // fetchEventSource("http://localhost:3000/api/ai-idea", {
    //   method: "POST",
    //   body: JSON.stringify({
    //     fields: [],
    //     input: "health",
    //     targetedField: "title",
    //     item: "post",
    //   }),
    //   onmessage: (e) => {
    //     setMessage((prevState) => prevState + e.data);
    //   },
    // });

    const resp = await fetch("/api/ai-idea/json", {
      method: "POST",
      body: JSON.stringify({
        fields: [],
        input: "health",
        targetedField: "title",
        item: "post",
        streamResponse: false,
      }),
    }).then((res) => res.json());

    console.log("handleClick", resp);
  };
  return (
    <div className="w-full h-full flex flex-col items-center justify-center p-20">
      {/*<ArchiveQueueCards prefix="post" locale={locale as Locale} {...texts} />*/}

      <Button onClick={handleClick}>Click me</Button>
    </div>
  );
}
