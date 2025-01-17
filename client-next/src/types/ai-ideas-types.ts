export type TargetedFields = "title" | "description";
export interface AIIdeaActionArgs {
  targetedField: TargetedFields;
  fields: AiIdeasField[];
  item: string;
  input?: string;
  extraContext?: number;
}

export interface AiIdeasField {
  name: string;
  isHtml: boolean;
  content: string;
  role: string;
}
export function isAIIdeaActionArgs(obj: any): obj is AIIdeaActionArgs {
  if (typeof obj !== "object" || obj === null) return false;

  const hasValidTargetedField =
    typeof obj.targetedField === "string" &&
    ["title", "description"].includes(obj.targetedField);

  const hasValidFields =
    Array.isArray(obj.fields) &&
    obj.fields.every((field: any) => {
      return (
        typeof field?.name === "string" &&
        typeof field?.isHtml === "boolean" &&
        typeof field?.content === "string" &&
        typeof field?.role === "string"
      );
    });

  const hasValidItem = typeof obj.item === "string";

  const hasValidInput =
    obj.input === undefined || typeof obj.input === "string";

  const hasValidExtraContext =
    obj.extraContext === undefined || typeof obj.extraContext === "number";

  return (
    hasValidTargetedField &&
    hasValidFields &&
    hasValidItem &&
    hasValidInput &&
    hasValidExtraContext
  );
}
