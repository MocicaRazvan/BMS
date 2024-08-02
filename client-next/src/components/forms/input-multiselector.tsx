"use client";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Control, FieldValues, Path, useFormContext } from "react-hook-form";
import MultipleSelector, { Option } from "@/components/ui/multiple-selector";

export interface InputMultipleSelectorTexts {
  label: string;
  emptyIndicator: string;
  placeholder: string;
}

interface Props<T extends FieldValues> {
  control: Control<T>;
  fieldName: string;
  inputMultipleSelectorTexts: InputMultipleSelectorTexts;
  options: Option[];
}

export default function InputMultipleSelector<T extends FieldValues>({
  control,
  fieldName,
  inputMultipleSelectorTexts: { label, emptyIndicator, placeholder },
  options,
}: Props<T>) {
  const { getValues } = useFormContext<T>();

  if (!(fieldName in getValues())) {
    throw new Error(`Invalid field name: ${fieldName}`);
  }
  return (
    <FormField
      control={control}
      name={fieldName as Path<T>}
      render={({ field }) => (
        <FormItem>
          <FormLabel className="capitalize">{label}</FormLabel>
          <FormControl>
            <MultipleSelector
              value={field.value}
              onChange={field.onChange}
              defaultOptions={options}
              placeholder={placeholder}
              emptyIndicator={
                <p className="text-center text-lg leading-10 text-gray-600 dark:text-gray-400">
                  {emptyIndicator}
                </p>
              }
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}
