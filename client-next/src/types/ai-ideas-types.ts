export type TargetedFields = "title" | "description";
export interface AIIdeaActionArgs {
  targetedField: TargetedFields;
  fields: AiIdeasField[];
  item: string;
  input?: string;
  extraContext?: number;
  streamResponse?: boolean;
}

export interface AiIdeasField {
  name: string;
  isHtml: boolean;
  content: string;
  role: string;
}
