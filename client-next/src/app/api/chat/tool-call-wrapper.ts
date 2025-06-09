import { ChatOllama } from "@langchain/ollama";
import { DynamicTool } from "@langchain/core/tools";
import {
  HumanMessage,
  SystemMessage,
  ToolMessage,
} from "@langchain/core/messages";
import { z } from "zod";
import { getRedisCache } from "@/lib/langchain/redis-cache";
import { normalizeText } from "@/lib/utils";
import { v4 as uuidv4 } from "uuid";
import { emitError } from "@/logger";
import { getFixingParser } from "@/lib/langchain/fixing-parser";
import removeMd from "remove-markdown";

interface ToolCallWrapperArgs {
  tools: DynamicTool[];
  input: string;
  userChatHistory: HumanMessage[];
  previousToolCalls: {
    tool_call_id?: string;
    content: string;
    input: string;
  }[];
}

const modelName = process.env.OLLAMA_MODEL;
const ollamaBaseUrl = process.env.OLLAMA_BASE_URL;

if (!modelName || !ollamaBaseUrl) {
  throw new Error(
    "OLLAMA_MODEL and OLLAMA_BASE_URL must be set in the environment",
  );
}
const toolCallSchema = z.array(
  z.object({
    tool_name: z
      .enum(["get_posts_by_title", "get_meal_plans_by_title", "no_op"])
      .describe("The name of the function to call"),
    input: z.string().describe("The input for the function"),
  }),
);

type ToolCall = z.infer<typeof toolCallSchema>[number];

const formatInstructions = `Respond only in valid JSON. The JSON object you return should match the following schema:
{[{ tool_name: "string", input: "string" }]}

An array of JSON objects: Where the tool_name is one of the following: "get_posts_by_title", "get_meal_plans_by_title", "no_op", and the input is a string for the tool.
`;

const getSystemMessage = (
  previousToolCalls: ToolCallWrapperArgs["previousToolCalls"],
) =>
  new SystemMessage(`
### Task:
You are an AI model designed to select the **all the most relevant functions** based on the user's query and chat history.  
Your primary objective is to identify ALL the functions that best fulfill the user's intent by analyzing their query and the chat history.  
You MUST fix typos in the query, but do not alter its original intent.
You MUST infer words or collocations of words like "some", "any", "it", etc. from the context when making a decision. Use the chat history to determine the most relevant substitute and use the substitution when making a decision. Enhance words: "post", "plan" in the inference, the more RECENT the better.

### Instructions:
- The output MUST follow this specific instructions:\n${formatInstructions}.\n DO NOT ADD ANY ADDITIONAL INFORMATION.
- DO NOT include any additional information or explanations.
- Always base your decision on the **explicit query** and the **context from the chat history**.
- Enhance the most recent chat history to ensure accurate decisions.
- Infer words or collocations of words like "some", "any", "it", etc. from the context. Enhance words: "post", "plan" in the inference, the more RECENT the better.

### Functions Available:
1. **get_posts_by_title**:
   - **Description**: __Searches for a POST__ about **nutrition, health, or well-being** by its TITLE and returns relevant content.
   - **Input**: A clean and concise search input for the post title extracted from the query.
   - **When to Use**: Use this function ONLY if the query suggests a **SEARCHING post by title** or **SEARCHING post by a specific criteria**. related to the specified topics. Do not use for general queries.
   - **Important**: Bad calls are EXPENSIVE! Be certain the query fits the criteria before choosing this function. Use it ONLY for SEARCHING posts, NOT general questions about posts.
   - **Key Word**: "post" 
   
2. **get_meal_plans_by_title**:
   - **Description**: __Searches for a MEAL PLAN__ related to nutrition by its TITLE and returns relevant content.
   - **Input**: A clean and concise search input for the plan title extracted from the query.
   - **When to Use**: Use this function ONLY if the context suggests a **SEARCHING plan by title**,**SEARCHING plan by an objective**, or **SEARCHING plan by a specific criteria**. Do not use for general queries.
   - **Important**: Bad calls are EXPENSIVE! Only use this when sure the query fits the criteria before choosing this function. Use it ONLY for SEARCHING plans, NOT general questions about plans.
   - **Key Word**: "plan" 

3. **no_op**:
   - **Description**: A fallback or default function when NO other function is relevant.
   - **Input**: A dummy input.
   - **When to Use**: Use this function if none of the above functions apply. This is a CHEAP option and should be the default for uncertainty.

### Guidelines for Selection:
- **Fix typos**: Correct typos in the query, but do not change the original intent.
- **Infer words**: Use the chat history to infer words or collocations of words like "some", "any", "it", etc. when making a decision. Enhance words: "post", "plan" in the inference, the more RECENT the better.
- **Be certain**: Choose **get_posts_by_title** or **get_meal_plans_by_title** only when the has a clear intent for searching for the relevant content, not general questions about posts or plans.
- **Default to no_op**: When in doubt, or when no function fits the query, use **no_op**.
- **Keep input concise**: Extract only the relevant title from the query for the function input.
- **Purpose**: the functions "get_posts_by_title" and "get_meal_plans_by_title" are for searching. NEVER use them for general inquiries about posts or plans.
- **Hint words**: If you see the word "post" or "plan" in the query, it is a good hint to use the corresponding function.
- **Think**: Do not blindly just choose base on the words "post" or "plan", think about the context and the user's intent.
- **Select ALL relevant functions**: Choose ALL functions that are relevant to the query and context. You can select more than one function if necessary.
${previousToolCalls.length === 0 ? "" : `- **Previous Tool Calls**: Try to avoid repeating the same tool calls with the same input.`}

### Important: The output MUST follow this specific instructions:\n${formatInstructions}.\n DO NOT ADD ANY ADDITIONAL INFORMATION.

${
  previousToolCalls.length === 0
    ? `### User Chat History:
Below is the user chat history for context:`
    : `### User Chat History And Previous Tool Calls:
    Below is the user chat history and previous tool calls for context:`
}
`);

const parsePreviousToolCalls = (
  previousToolCalls: ToolCallWrapperArgs["previousToolCalls"],
) =>
  previousToolCalls
    .map(
      (m) =>
        `Tool Name - ${m.tool_call_id} \\t Tool Content - ${m.content} \\t Input - ${m.input || "unknown"}\``,
    )
    .join("\n");

function getHumanMessage(
  input: string,
  previousToolCalls: ToolCallWrapperArgs["previousToolCalls"],
) {
  return new HumanMessage(`
### Current Query:
The current query is: "${input}"
${previousToolCalls.length > 0 ? `### Previous Tool Calls: ${parsePreviousToolCalls(previousToolCalls)}` : ""}

### Reminders:
1. You MUST fix typos in the query, but do not change its original intent.
2. Use both the **chat history** and **query context** to understand the whole context at a deeper level do not blindly just follow the words "post" or "plan" and determine the most relevant functions.
3. Extract **only the relevant part of the query** for the functions input. Never put in the input words like "search", "find", "post", "plan", "read" or any punctuation.
4. You MUST infer words or collocations of words like "some", "any", "it", etc from the context. Enhance words: "post", "plan" in the inference, the more RECENT the better.
5. You can select **ALL relevant functions** that are applicable to the query and context.
6. When in doubt, default to the **no_op** function.
${
  previousToolCalls.length === 0
    ? `7. The output MUST follow this specific instructions:\n${formatInstructions}.\n DO NOT ADD ANY ADDITIONAL INFORMATION.`
    : `7. Try to avoid repeating the same tool calls with the same input.
8. The output MUST follow this specific instructions:\n${formatInstructions}.\n DO NOT ADD ANY ADDITIONAL INFORMATION.`
}`);
}

const toolModel = new ChatOllama({
  model: modelName,
  baseUrl: ollamaBaseUrl,
  streaming: false,
  keepAlive: "-1m",
  cache: getRedisCache(),
  temperature: 0,
  numCtx: process.env.OLLAMA_NUM_CTX
    ? parseInt(process.env.OLLAMA_NUM_CTX)
    : 2048,
}).pipe(getFixingParser(toolCallSchema));
// .withStructuredOutput(toolCallSchema);
// .bindTools(tools);
function mapToResponse(toolsByName: Record<string, DynamicTool>, t: ToolCall) {
  const tool = toolsByName[t.tool_name];
  const sanitizedInput = removeMd(
    t.input
      .toLowerCase()
      .replace(/[?!]/g, " ")
      .replace(/[^\w\s]/g, " ")
      .replace(
        /\b(some|any|post|plan|posts|plans|buy\w*|search\w*|find\w*|read\w*|filter\w*|browse\w*|purchase\w*|meal\w*|see\w*)\b/gi,
        " ",
      )
      .normalize("NFKC")
      .replace(/\s+/g, " "),
  ).trim();
  console.log("Sanitized Input: ", sanitizedInput);
  return {
    tool,
    sanitizedInput,
  };
}

function mapToToolMessage(t: {
  tool: DynamicTool;
  result: string;
  input: string;
}) {
  return new ToolMessage({
    content: t.result,
    name: t.tool.name,
    id: uuidv4(),
    tool_call_id: t.tool.name,
    status: "success",
    response_metadata: {
      timestamp: new Date().getTime(),
      input: t.input,
    },
  });
}

export async function getToolsForInput({
  tools,
  input,
  userChatHistory,
  previousToolCalls,
}: ToolCallWrapperArgs): Promise<ToolMessage[]> {
  input = normalizeText(input);
  userChatHistory = userChatHistory.map((t) => {
    t.content = normalizeText(t.content);
    return t;
  });
  const toolsByName = tools.reduce<Record<string, DynamicTool>>((acc, t) => {
    acc[t.name] = t;
    return acc;
  }, {});
  if (tools.length === 0) {
    return [];
  }

  const messages = [
    getSystemMessage(previousToolCalls),
    ...userChatHistory,
    getHumanMessage(input, previousToolCalls),
  ];

  try {
    const toolResp = await toolModel.invoke(messages);
    console.log("Tool Response: ", toolResp);
    if (!Array.isArray(toolResp)) {
      return [];
    }
    return (
      await Promise.all(
        toolResp
          .filter((t) => t.tool_name !== "no_op")
          .map((t) => mapToResponse(toolsByName, t))
          .filter(
            (t) => t.sanitizedInput.length > 0 && t.sanitizedInput !== " ",
          )
          .map(async ({ tool, sanitizedInput }) => ({
            tool,
            result: await tool.invoke(sanitizedInput),
            input: sanitizedInput,
          })),
      )
    )
      .filter((t) => typeof t.result === "string" && t.result.length > 0)
      .map(mapToToolMessage);
  } catch (e) {
    console.error(e);
    if (e instanceof Error) {
      emitError(e);
    }
    return [];
  }
}
