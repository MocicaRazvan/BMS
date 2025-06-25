export type IdbMessageResponse = {
  status: "success" | "error";
  entries?: Record<string, unknown[][]>;
  error?: Error;
  type?: IdbMessageType;
};

export enum IdbMessageType {
  LOAD_CACHE = "loadCache",
  DUMP_CACHE = "dumpCache",
  CLEAR_CACHE = "clearCache",
}

export interface LoadCacheIncomingMessage {
  type: IdbMessageType.LOAD_CACHE;
  afterTimestamp: number;
  userEmail: string;
}

export interface DumpCacheIncomingMessage {
  type: IdbMessageType.DUMP_CACHE;
  payload: Array<[string, unknown[][]]>;
  userEmail: string;
}

export interface ClearCacheIncomingMessage {
  type: IdbMessageType.CLEAR_CACHE;
}

export type IdbIncomingMessage =
  | LoadCacheIncomingMessage
  | DumpCacheIncomingMessage
  | ClearCacheIncomingMessage;
