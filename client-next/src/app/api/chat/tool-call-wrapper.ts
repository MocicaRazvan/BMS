import { ChatOllama } from "@langchain/ollama";
import { DynamicTool } from "@langchain/core/tools";
import {
  HumanMessage,
  SystemMessage,
  ToolMessage,
} from "@langchain/core/messages";

interface ToolCallWrapperArgs {
  tools: DynamicTool[];
  input: string;
  userChatHistory: HumanMessage[];
}
const modelName = process.env.OLLAMA_MODEL;
const ollamaBaseUrl = process.env.OLLAMA_BASE_URL;

if (!modelName || !ollamaBaseUrl) {
  throw new Error(
    "OLLAMA_MODEL and OLLAMA_BASE_URL must be set in the environment",
  );
}
export async function getToolsForInput({
  tools,
  input,
  userChatHistory,
}: ToolCallWrapperArgs): Promise<ToolMessage[]> {
  const toolsByName = tools.reduce<Record<string, DynamicTool>>((acc, t) => {
    acc[t.name] = t;
    return acc;
  }, {});
  if (tools.length === 0) {
    return [];
  }
  const toolModel = new ChatOllama({
    model: modelName,
    baseUrl: ollamaBaseUrl,
    streaming: true,
    // verbose: true,
    keepAlive: "-1m",
    cache: false,
    temperature: 0,
    numCtx: process.env.OLLAMA_NUM_CTX
      ? parseInt(process.env.OLLAMA_NUM_CTX)
      : 2048,
  }).bindTools(tools);
  const messages = [
    new SystemMessage(`
### Task:
You are an AI model designed to decide and execute a **tool call** based on the user's query and chat history.  
Your primary objective is to identify the most relevant tool to fulfill the user's intent and execute the call __if necessary__.  
You must call the tool directly and provide no other response or explanation.  
You must ensure that the chat history and the user's query are considered when making the tool call.

### Instructions:
- __Do not__ call tools if they are not relevant to the user's query or context, your job is not to always make a call.
- IT IS BETTER TO SKIP A CALL THAN TO MAKE AN INCORRECT ONE.
- TOOL CALLS ARE EXPENSIVE AND SHOULD BE USED JUDICIOUSLY.
- If you are unsure about the relevance of a tool, avoid making a tool call.
- Do not use tools for general inquiries, definitions, or unrelated questions.
- You can make slight adjustments to the input for clarity or to fix minor typos, but do not alter the original intent.

### Important Notes:
- Always base your decision on the user's **explicit query** and the **context provided by the chat history**.
- Only execute the relevant **tool call**.  
- If no tool is relevant to the user's query or context, refrain from selecting or executing any tool. You are not obligated to make a selection in every instance.  
- Do not provide any explanation, justification, or extra response when making a tool call.
- If you are unsure about the relevance of a tool, it is __better to avoid making a tool call__.
- Tool calls are expensive you should be SURE when calling them.
- ALWAYS enhance the most recent chat history when making a decision.


### Takeaway:
- **Select the most relevant tool based on the query and the chat history.**
- **Make the tool call directly without any additional response.**
- **Do not make unnecessary tool calls.**

### User History:
 Below is the user chat history:
`),
    ...userChatHistory,
    new HumanMessage(`
    The current query is: "${input}".
    
    **Remember**:
    - You can fix typos in the query but do not alter the original intent.
    - Based on the query and the chat history, select the most relevant tool and make the call. 
    - You are NOT obligated to make a selection in every instance, it is BETTER to avoid making a tool call if you are unsure about the relevance of a tool.
    - Tools are EXPENSIVE and should be used JUDICIOUSLY, DO NOT make unnecessary calls.
    - Use the chat history and the query to determine the most relevant tool.
    - Do not use tools for general inquiries, definitions, or unrelated questions.
    `),
  ];
  const toolResp = await toolModel.invoke(messages);
  if (!toolResp.tool_calls) {
    return [];
  }
  console.log("TOOL RESPONSE", toolResp);
  try {
    return await Promise.all(
      toolResp.tool_calls.map(async (t) => {
        console.log(`Calling the ${t.name} tool.`);
        const selectedTool = toolsByName[t.name];
        return await selectedTool.invoke(t);
      }),
    );
  } catch (e) {
    console.error(e);
    return [];
  }
}
`
---

### Tools Available:
1. **get_posts_by_title**:
   - **Purpose**: Search for a post related to nutrition, health, or well-being by its title and return the content.
   - **When to Use**: If the query and the context suggest a search for a a post by title.
   - **Important**: Expensive tool call, use judiciously.

2. **get_meal_plans_by_title**:
   - **Purpose**: Search for a meal plan by its title and return the content.
   - **When to Use**: If the query and the context suggest a search for a meal plan by title.
   - **Important**: Expensive tool call, use judiciously.
---

### Decision Rules:
1. **Determine Intent and Context**:
   - Analyze the query and the chat history to understand the user's intent.
   - Consider the user's query and the context provided by the chat history when selecting a tool.

2. **Match Query to Tool**:
   - Use **get_posts_by_title** for queries about posts and their titles.
   - Use **get_meal_plans_by_title** for queries about meal plans and their titles.

3. **Input Optimization**:
   - Make slight adjustments to the input for clarity or to fix minor typos, but do not alter the original intent.

4. **When Not to Use a Tool**:
   - If the query and the history do not align with the purpose of any tool, DO NOT make any tool call.
   - For general inquiries, definitions, or unrelated questions, AVOID selecting a tool.
   - Examples of unrelated queries: "Tell me a joke", "What is your favorite color?", "Who is the president?"
   
5. **Execution Without Additional Response**:
   - If a tool is selected, execute the tool call immediately without adding explanations, extra text, or commentary.

6. **Err on the Side of Caution**:
   - If you are unsure about whether a tool is relevant, do not make a tool call. It is better to skip the call than to make an incorrect one.

### Examples:

**Example 1: Query Matches Tool**
- **Query**: "Find the meal plan titled 'Keto Beginner Guide.'"
- **Tool Selection**: "get_meal_plans_by_title"

**Example 2: Query Matches Tool**
- **Query**: "Search for the post titled 'Healthy Eating Tips.'"
- **Tool Selection**: "get_posts_by_title"

**Example 3: Query Matches Tool**
- **Query**: "Are there any plans for 'gaining mass'?"
- **Tool Selection**: "get_meal_plans_by_title"

**Example 4: Query Matches Tool**
- **Query**: "Are posts available for 'weight loss'?"
- **Tool Selection**: "get_posts_by_title"

**Example 5: Query Matches Tool**
- **History1**: "How to buy plans?"
- **Query**: " Are there some plans for 'weight loss'?"
- **Tool Selection**: "get_meal_plans_by_title"

**Example 5: Query Matches Tool**
- **History1**: "Does the site have posts?"
- **Query**: "Is one for 'kids'?"
- **Tool Selection**: "get_posts_by_title"

**Example 6: No Tool Needed**
- **Query**: "How to buy plans?"
- **Tool Selection**: NONE

**Example 7: No Tool Needed**
- **Query**: "What is a meal plan?"
- **Tool Selection**: NONE

**Example 8: No Tool Needed**
- **Query**: "tell me a joke"
- **Tool Selection**: NONE

**Example 9: Query Matches Tool**
- **History1**: "What are meal plans?"
- **History2**: "Are there any about 'veganism'?"
- **Query**: "How to calculate macros?"
- **Tool Selection**: NONE

**Example 10: Query Matches Tool**
- **History1**: "Do you have posts on the site?"
- **History2**: "About 'healthy eating'?"
- **Query**: "Tell me a joke"
- **Tool Selection**: NONE

---`;
