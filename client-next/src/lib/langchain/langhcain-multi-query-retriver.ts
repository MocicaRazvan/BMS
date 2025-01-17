import { ChatPromptTemplate } from "@langchain/core/prompts";
import { BaseOutputParser } from "@langchain/core/output_parsers";
import { ChatOllama } from "@langchain/ollama";
import { MultiQueryRetriever } from "langchain/retrievers/multi_query";
import { LLMChain } from "langchain/chains";
import { BaseRetrieverInterface } from "@langchain/core/retrievers";
import { SystemMessage } from "@langchain/core/messages";
type LineList = {
  lines: string[];
};
const modelName = process.env.OLLAMA_MODEL;
const ollamaBaseUrl = process.env.OLLAMA_BASE_URL;

function getMultiQuerySystemPrompt(extraQuestion?: string) {
  return `You are an AI language model assistant on a nutritional related site. Your ONLY task is
        to generate ##{queryCount}## different versions of the given user
        input to retrieve relevant documents from a vector database.
        By generating multiple perspectives on the user input,
        your goal is to help the user overcome some of the limitations
        of distance-based similarity. 
        
        Keep the original meaning of the input intact, do not alter it or add any new information that was not present in the original input.
        
        While rephrasing, you may focus on areas related to well-being, nutrition, dietary advice, or meal plans when applicable. However, avoid introducing new or unrelated information. 

        
        All inputs should be in English.
        
        Generate exactly **{queryCount}** rephrased queries.
        
        **Output format**:
          Provide these alternative inputs separated by newlines between XML tags. For example:
          
          <inputs>
          Input 1
          Input 2
          Input 3
          </inputs>
        
        **Final Rules**:
        - Always make sure to include the original input at the end.
        - Do not include any additional information or commentary.
        - You MUST respect the output format and the user intent.
        
        ##Original input##: {question}${extraQuestion ? extraQuestion : ""}`;
}

export class LineListOutputParser extends BaseOutputParser<LineList> {
  static lc_name() {
    return "LineListOutputParser";
  }

  lc_namespace = ["langchain", "retrievers", "multiquery"];

  private readonly originalInput: string;
  private readonly extraInput: string | undefined;

  constructor(originalInput: string, extraInput?: string) {
    super();
    this.originalInput = originalInput;
    this.extraInput = extraInput;
  }

  async parse(text: string): Promise<LineList> {
    const startKeyIndex = text.indexOf("<inputs>");
    const endKeyIndex = text.indexOf("</inputs>");
    const inputsStartIndex =
      startKeyIndex === -1 ? 0 : startKeyIndex + "<inputs>".length;
    const inputsEndIndex = endKeyIndex === -1 ? text.length : endKeyIndex;
    const lines = text
      .slice(inputsStartIndex, inputsEndIndex)
      .trim()
      .split("\n")
      .filter((line) => line.trim() !== "")
      .map((line) => line.trim());

    if (this.extraInput && this.extraInput.trim().length > 0) {
      lines.push(this.extraInput.trim());
    }

    const newLines = [...new Set(lines.concat(this.originalInput))].filter(
      (t) => t.trim().length > 0,
    );
    console.log("newLines", newLines);
    return { lines: newLines };
  }

  getFormatInstructions(): string {
    throw new Error("Not implemented.");
  }
}
interface MultiQueryRetrieverArgs {
  retriever: BaseRetrieverInterface;
  queryCount?: number;
  llmChain?: LLMChain<LineList, ChatOllama>;
  extraQuestion?: string;
  originalInput: string;
  extraInput?: string;
}
export function getMultiQueryRetriever({
  retriever,
  queryCount = 3,
  llmChain = undefined,
  extraQuestion,
  originalInput,
  extraInput,
}: MultiQueryRetrieverArgs) {
  if (!llmChain) {
    const llm = new ChatOllama({
      model: modelName,
      baseUrl: ollamaBaseUrl,
      keepAlive: "-1m",
      temperature: 0.2,
      cache: false,
      numCtx: process.env.OLLAMA_NUM_CTX
        ? parseInt(process.env.OLLAMA_NUM_CTX)
        : 2048,
    });
    const multiQueryRetrieverPrompt = ChatPromptTemplate.fromMessages([
      ["system", getMultiQuerySystemPrompt(extraQuestion)],
    ]);
    llmChain = new LLMChain({
      llm,
      prompt: multiQueryRetrieverPrompt,
    });
  }
  llmChain.outputParser = new LineListOutputParser(originalInput, extraInput);
  return new MultiQueryRetriever({
    retriever,
    llmChain,
    queryCount,
  });
}
