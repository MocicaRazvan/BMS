import { fetchStream } from "@/hoooks/fetchStream";
import {
  PageableResponse,
  PlanResponse,
  PostResponse,
  ResponseWithUserDtoEntity,
  TitleBodyUserDto,
} from "@/types/dto";
import { Locale } from "@/navigation";
import { tool } from "@langchain/core/tools";
import { z } from "zod";

export async function getItemTool<T extends TitleBodyUserDto>(
  input: string,
  token: string,
  path: string,
  modelName: string,
  siteUrl: string,
  locale: Locale,
  extraMap?: (content: T) => string,
) {
  const response = await fetchStream<
    PageableResponse<ResponseWithUserDtoEntity<T>>[]
  >({
    path,
    method: "PATCH",
    token,
    cache: "no-cache",
    acceptHeader: "application/json",
    body: {
      page: 0,
      size: process.env.OLLAMA_TOOL_PAGE_SIZE
        ? parseInt(process.env.OLLAMA_TOOL_PAGE_SIZE)
        : 3,
    },
    queryParams: {
      title: input.trim(),
      approved: "true",
    },
  });

  console.log("getItemTool", response);

  if (response.error || response.messages[0].length === 0) {
    return "";
  }

  return response.messages[0].reduce(
    (
      acc,
      {
        content: {
          model: { content },
        },
      },
      i,
    ) => {
      acc +=
        `Item Number: ${i + 1} \t ` +
        `Title: ${content.title} \t ` +
        `URL: ${siteUrl}/${locale}/${modelName}/single/${content.id} \t`;
      if (extraMap) {
        acc += extraMap(content);
      }
      acc += `\n`;
      if (i === response.messages[0].length - 1) {
        acc += `\n`;
      }
      return acc;
    },
    `There were ${response.messages[0].length} for the ${modelName} search. \n\n`,
  );
}

export function createTool(
  func: (input: string) => Promise<string>,
  name: string,
  description: string,
  inputDescription: string,
) {
  return tool(func, {
    name,
    description,
    schema: z.string().describe(inputDescription),
  });
}

export function generateToolsForUser(
  token: string,
  siteUrl: string,
  locale: Locale,
) {
  const getPostsByTitle = createTool(
    async (input: string) => {
      console.log("getPostsByTitle", input);
      const ret = await getItemTool<PostResponse>(
        input,
        token,
        "/posts/tags/withUser",
        "posts",
        siteUrl,
        locale,
        (c) => `Tags: ${c.tags.join(", ")}\t `,
      );

      console.log("getPostsByTitle", ret);
      return ret;
    },
    "get_posts_by_title",
    "Search a POST about nutrition, health or well-being by title and return the relevant content." +
      "MUST be called ONLY if the query and the context suggest a search for a post by title." +
      "Bad calls are EXPENSIVE!",
    "The query for the post title, MUST be related to nutrition, health or well-being.",
  );

  const getMealPlansByTitle = createTool(
    async (input: string) => {
      console.log("getMealPlansByTitle", input);
      const ret = await getItemTool<PlanResponse>(
        input,
        token,
        "/plans/filtered/withUser",
        "plans",
        siteUrl,
        locale,
        (c) =>
          `Objective: ${c.objective}\t Number of days: ${c.days?.length}\t `,
      );
      console.log("getMealPlansByTitle", ret);
      return ret;
    },
    "get_meal_plans_by_title",
    "Search a MEAL PLAN by title and return the relevant content" +
      "MUST be called ONLY if the query and the context suggest a search for a meal plan by title." +
      "Bad calls are EXPENSIVE!",
    "The query for the meal plan title, MUST be related to meal plans.",
  );
  const noOp = createTool(
    async (_: string) => {
      return "";
    },
    "no_op",
    "No operation, fallback tool",
    "Dummy input",
  );
  return [getPostsByTitle, getMealPlansByTitle, noOp];
}
