import { fetchStream } from "@/hoooks/fetchStream";
import {
  PageableResponse,
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
      size: 3,
    },
    queryParams: {
      title: input.trim(),
      approved: "true",
    },
  });

  if (response.error) {
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
        `Item: ${i + 1} \t ` +
        `Title: ${content.title} \t ` +
        `Link: ${siteUrl}/${locale}/${modelName}/single/${content.id} \n`;
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
      const ret = await getItemTool(
        input,
        token,
        "/posts/tags/withUser",
        "posts",
        siteUrl,
        locale,
      );

      console.log("getPostsByTitle", ret);
      return ret;
    },
    "get_posts_by_title",
    "Search a POST about nutrition, health or well-being by title and return the relevant content",
    "The query for the post title",
  );

  const getMealPlansByTitle = createTool(
    async (input: string) => {
      console.log("getMealPlansByTitle", input);
      const ret = await getItemTool(
        input,
        token,
        "/plans/filtered/withUser",
        "plans",
        siteUrl,
        locale,
      );
      console.log("getMealPlansByTitle", ret);
      return ret;
    },
    "get_meal_plans_by_title",
    "Search a MEAL PLAN by title and return the relevant content",
    "The query for the meal plan title",
  );
  return [getPostsByTitle, getMealPlansByTitle];
}
