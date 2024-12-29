import { NextRequest, NextResponse } from "next/server";
import "cheerio";
import { generateToolsForUser } from "@/app/api/chat/get-item-tool";
import { getToolsForInput } from "@/app/api/chat/tool-call-wrapper";
import { HumanMessage } from "@langchain/core/messages";

export async function POST(req: NextRequest) {
  const body = await req.json();
  const question = body.question;
  const history: string[] = body.history;
  const toolMessages = await getToolsForInput({
    input: question,
    tools: generateToolsForUser(
      "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyYXp2YW5tb2NpY2ExQGdtYWlsLmNvbSIsInJvbGVzIjpbeyJhdXRob3JpdHkiOiJST0xFX0FETUlOIn1dLCJlbWFpbCI6InJhenZhbm1vY2ljYTFAZ21haWwuY29tIiwicHJvdmlkZXIiOiJMT0NBTCIsImlhdCI6MTczNDU2MjMwNywiZXhwIjoxNzM4MTYyMzA3fQ.VrjICLDfPBulCxbNmsILSC61MSvTERiS1_LkfBsip9U",
      "localhost",
      "en",
    ),
    userChatHistory: history.map((t) => new HumanMessage(t)),
  });
  return NextResponse.json(
    { success: true, response: toolMessages },
    { status: 200 },
  );
}
