import { BaseMessageFields, MessageContent } from "@langchain/core/messages";

export interface LangchainExtraToolTypeMessage {
  id: string[];
  type: string;
  lc: number;
  artifact?: any;
  kwargs: {
    additional_kwargs: BaseMessageFields["additional_kwargs"];
    content: MessageContent;
    id?: string;
    name?: string;
    response_metadata: BaseMessageFields["response_metadata"];
    status?: "success" | "error";
    tool_call_id: string;
  };
}
