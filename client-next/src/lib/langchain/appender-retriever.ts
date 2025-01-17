import { BaseRetriever, BaseRetrieverInput } from "@langchain/core/retrievers";
import { Document as LangDocument } from "langchain/document";
import type { CallbackManagerForRetrieverRun } from "@langchain/core/callbacks/manager";
import { Document } from "@langchain/core/documents";
export interface AppenderRetrieverInput extends BaseRetrieverInput {}
export class AppenderRetriever extends BaseRetriever {
  lc_namespace = ["langchain", "retrievers"];
  private readonly appendedDocs: LangDocument[];
  private baseRetriever;

  constructor(
    baseRetriever: BaseRetriever,
    appendedDocs: LangDocument[],
    fields?: AppenderRetrieverInput,
  ) {
    super(fields);
    this.baseRetriever = baseRetriever;
    this.appendedDocs = appendedDocs;
  }

  async _getRelevantDocuments(
    query: string,
    runManager?: CallbackManagerForRetrieverRun,
  ): Promise<Document[]> {
    const baseDocs = await this.baseRetriever._getRelevantDocuments(
      query,
      runManager,
    );
    console.log("baseDocs", baseDocs.length);
    const a = baseDocs.concat(this.appendedDocs);
    console.log("baseDocsa", a.length);
    return a;
  }
}
