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

export interface TitleBodyTexts {
  title: string;
  body: string;
  titlePlaceholder?: string;
  bodyPlaceholder?: string;
}

interface CustomFieldProps<TFieldValues extends TitleBodyDto> {
  control: Control<TFieldValues>;
  //   field: ControllerRenderProps<TFieldValues, Path<TFieldValues>>;
  titleBodyTexts: TitleBodyTexts;
  hideTitle?: boolean;
  editorKey?: number;
}

export const TitleBodyForm = <TFieldValues extends TitleBodyDto>({
  control,
  titleBodyTexts: { body, bodyPlaceholder, titlePlaceholder, title },
  hideTitle = false,
  editorKey,
}: CustomFieldProps<TFieldValues>) => {
  const { getValues } = useFormContext<TFieldValues>();

  if (!["title", "body"].every((k) => k in getValues())) {
    throw new Error(`Invalid field names`);
  }

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
                <Input placeholder={titlePlaceholder} {...field} />
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
            <FormLabel className="capitalize">{body}</FormLabel>
            <FormControl>
              {/* <Textarea placeholder={bodyPlaceholder} {...field} />
               */}
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
