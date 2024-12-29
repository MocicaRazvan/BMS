import {
  BaseRetriever,
  type BaseRetrieverInput,
} from "@langchain/core/retrievers";
import type { CallbackManagerForRetrieverRun } from "@langchain/core/callbacks/manager";
import { Document } from "@langchain/core/documents";
export interface PrivilegedRetrieverInput extends BaseRetrieverInput {}

export class PrivilegedRetriever extends BaseRetriever {
  lc_namespace = ["langchain", "retrievers"];

  private readonly currentUserRole: string;
  private baseRetriever;

  constructor(
    baseRetriever: BaseRetriever,
    userRole: string,
    fields?: PrivilegedRetrieverInput,
  ) {
    super(fields);
    this.currentUserRole = userRole.toLowerCase().trim();
    this.baseRetriever = baseRetriever;
  }

  async _getRelevantDocuments(
    query: string,
    runManager?: CallbackManagerForRetrieverRun,
  ): Promise<Document[]> {
    return (
      await this.baseRetriever._getRelevantDocuments(query, runManager)
    ).filter(({ metadata }) => {
      if (
        this.currentUserRole === "admin" ||
        !metadata?.url ||
        !(typeof metadata.url === "string")
      ) {
        return true;
      }
      const parsedUrl = metadata.url.toLowerCase().trim();
      const includesTrainer = parsedUrl.includes("/trainer");
      const includesAdmin = parsedUrl.includes("/admin");

      if (this.currentUserRole === "trainer") {
        return !includesAdmin;
      }

      return !includesTrainer && !includesAdmin;
    });
  }
}
