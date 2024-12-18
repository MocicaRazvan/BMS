import { Control, Path, useFormContext } from "react-hook-form";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import Editor from "../editor/editor";
import { TitleBodyDto } from "@/types/dto";
import { AiIdeasField } from "@/types/ai-ideas-types";
import AIGeneratePop, {
  AIGeneratePopTexts,
  AIPopCallback,
} from "@/components/forms/ai-generate-pop";
import { useMemo } from "react";

export interface TitleBodyTexts {
  title: string;
  body: string;
  titlePlaceholder?: string;
  bodyPlaceholder?: string;
}

interface CustomFieldProps<TFieldValues extends TitleBodyDto> {
  control: Control<TFieldValues>;
  titleBodyTexts: TitleBodyTexts;
  hideTitle?: boolean;
  editorKey?: number;
  showAIPopTitle?: boolean;
  showAIPopDescription?: boolean;
  aiFields?: AiIdeasField[];
  aiTitleCallBack?: AIPopCallback;
  aiDescriptionCallBack?: AIPopCallback;
  aiItem?: string;
  aiCheckBoxes?: Record<string, string>;
  titleAIGeneratedPopTexts?: AIGeneratePopTexts;
  bodyAIGeneratedPopTexts?: AIGeneratePopTexts;
  extraBodyContext?: number;
}

export const TitleBodyForm = <TFieldValues extends TitleBodyDto>({
  control,
  titleBodyTexts: { body, bodyPlaceholder, titlePlaceholder, title },
  hideTitle = false,
  editorKey,
  showAIPopTitle = false,
  showAIPopDescription = false,
  aiFields = [],
  aiTitleCallBack = (r) => {},
  aiDescriptionCallBack = (r) => {},
  aiItem = "",
  aiCheckBoxes = {},
  titleAIGeneratedPopTexts,
  bodyAIGeneratedPopTexts,
  extraBodyContext = 5,
}: CustomFieldProps<TFieldValues>) => {
  const { getValues } = useFormContext<TFieldValues>();

  if (!["title", "body"].every((k) => k in getValues())) {
    throw new Error(`Invalid field names`);
  }
  const titleAIFields = useMemo(
    () =>
      // .filter((f) => f.name !== "title"),
      aiFields,
    [aiFields],
  );
  const bodyAIFields = useMemo(
    () =>
      // .filter((f) => f.name !== "description"),
      aiFields,
    [aiFields],
  );

  return (
    <div className="space-y-8 lg:space-y-12">
      {!hideTitle && (
        <FormField
          control={control}
          name={"title" as Path<TFieldValues>}
          render={({ field }) => (
            <FormItem>
              <FormLabel className="capitalize">{title}</FormLabel>
              <FormControl>
                <div className="flex items-center justify-center">
                  <Input
                    placeholder={titlePlaceholder}
                    {...field}
                    className="flex-1"
                  />
                  {showAIPopTitle && titleAIGeneratedPopTexts && (
                    <div className="ml-2 md:ml-4">
                      <AIGeneratePop
                        fields={titleAIFields}
                        callback={aiTitleCallBack}
                        targetedField={"title"}
                        item={aiItem}
                        checkBoxes={aiCheckBoxes}
                        {...titleAIGeneratedPopTexts}
                      />
                    </div>
                  )}
                </div>
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
      )}
      <FormField
        control={control}
        name={"body" as Path<TFieldValues>}
        render={({ field }) => (
          <FormItem>
            <div className="flex items-center justify-between">
              <FormLabel className="capitalize">{body}</FormLabel>
              {showAIPopDescription && bodyAIGeneratedPopTexts && (
                <AIGeneratePop
                  fields={bodyAIFields}
                  callback={aiDescriptionCallBack}
                  targetedField={"description"}
                  item={aiItem}
                  checkBoxes={aiCheckBoxes}
                  extraContext={extraBodyContext}
                  {...bodyAIGeneratedPopTexts}
                />
              )}
            </div>
            <FormControl>
              <Editor
                descritpion={field.value as string}
                onChange={field.onChange}
                placeholder={bodyPlaceholder}
                key={editorKey}
              />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />
    </div>
  );
};
