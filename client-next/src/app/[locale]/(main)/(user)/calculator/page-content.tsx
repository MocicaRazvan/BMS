"use client";

import { CalculatorPageTexts } from "@/app/[locale]/(main)/(user)/calculator/page";
import { useCallback, useMemo, useState } from "react";
import {
  activities,
  CalculatorSchemaType,
  getCalculatorSchema,
} from "@/types/forms";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { AnimatePresence, motion } from "framer-motion";

export interface Props extends CalculatorPageTexts {}
function createIntakeSubtitle(
  isImperial: boolean,
  valueKg: number,
  week: string,
) {
  const v = isImperial ? valueKg * 2.20462262 : valueKg;

  const formattedValue = Number.isInteger(v) ? v.toFixed(0) : v.toFixed(2);

  return `${formattedValue} ${isImperial ? "lbs" : "kg"}/${week}`;
}
export default function CalculatorPageContent({
  calculatorSchemaTexts,
  activitiesTexts,
  genderText,
  intakeTitles,
  week,
  metric,
  itemsTexts,
  imperial,
  header,
  title,
  button,
  message2,
  message1,
}: Props) {
  const [isImperial, setIsImperial] = useState(false);
  const [result, setResult] = useState<number | null>(null);
  const intakeValues = useMemo(
    () => [
      {
        title: intakeTitles["Maintain weight"],
        subtitle: "",
        percent: 1,
        value: "Maintain weight",
      },
      {
        title: intakeTitles["Mild weight loss"],
        subtitle: createIntakeSubtitle(isImperial, 0.25, week),
        percent: 0.92,
        value: "Mild weight loss",
      },
      {
        title: intakeTitles["Weight loss"],
        subtitle: createIntakeSubtitle(isImperial, 0.5, week),
        percent: 0.84,
        value: "Weight loss",
      },
      {
        title: intakeTitles["Extreme weight loss"],
        subtitle: createIntakeSubtitle(isImperial, 1, week),
        percent: 0.68,
        value: "Extreme weight loss",
      },
      {
        title: intakeTitles["Mild weight gain"],
        subtitle: createIntakeSubtitle(isImperial, 0.25, week),
        percent: 1.08,
        value: "Mild weight gain",
      },
      {
        title: intakeTitles["Weight gain"],
        subtitle: createIntakeSubtitle(isImperial, 0.5, week),
        percent: 1.16,
        value: "Weight gain",
      },
      {
        title: intakeTitles["Fast Weight gain"],
        subtitle: createIntakeSubtitle(isImperial, 1, week),
        percent: 1.32,
        value: "Fast Weight gain",
      },
    ],
    [intakeTitles, isImperial, week],
  );

  const schema = useMemo(
    () => getCalculatorSchema(calculatorSchemaTexts),
    [calculatorSchemaTexts],
  );

  const form = useForm<CalculatorSchemaType>({
    resolver: zodResolver(schema),
    defaultValues: {
      activity: undefined,
      age: undefined,
      gender: undefined,
      height: undefined,
      weight: undefined,
      intake: undefined,
    },
  });

  const onSubmit = useCallback(
    ({
      weight,
      age,
      intake,
      activity,
      gender,
      height,
    }: CalculatorSchemaType) => {
      setResult(
        (10 * weight * (1 - 0.546408 * Number(isImperial)) +
          6.25 * height * (1 - 0.606299 * Number(isImperial)) -
          5 * age +
          (gender === "male" ? 5 : -161)) *
          activities[activity] *
          (intakeValues.find((i) => i.value === intake)?.percent || 0),
      );
    },
    [intakeValues, isImperial],
  );

  return (
    <Card className="w-full p-4 ">
      <CardTitle className="font-bold text-2xl lg:text-4xl text-center capitalize">
        {title}
      </CardTitle>
      <CardHeader className="text-center lg:text-lg font-semibold my-5">
        {header}
      </CardHeader>
      <CardContent className="space-y-10 lg:space-y-14 ">
        <div className="w-full flex items-center justify-end gap-4">
          <p>{metric}</p>
          <Switch checked={isImperial} onCheckedChange={setIsImperial} />
          <p>{imperial}</p>
        </div>
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-8 lg:space-y-12 "
            noValidate
          >
            <div className="flex flex-col lg:flex-row items-start justify-center w-full gap-8 lg:gap-12">
              <FormField
                control={form.control}
                name="age"
                render={({ field }) => (
                  <FormItem className="flex-1 w-full">
                    <FormLabel className="capitalize">
                      {itemsTexts.age.label}
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder={itemsTexts.age.placeholder}
                        type={"number"}
                        step={1}
                        {...field}
                      />
                    </FormControl>
                    <FormDescription>
                      {itemsTexts.age?.description || ""}
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="activity"
                render={({ field }) => (
                  <FormItem className="flex-1 w-full">
                    <FormLabel className="capitalize">
                      {itemsTexts.activity.label}
                    </FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue
                            placeholder={itemsTexts.activity.placeholder}
                          />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {Object.entries(activitiesTexts).map(([key, value]) => (
                          <SelectItem
                            key={key}
                            value={key}
                            className="cursor-pointer"
                          >
                            {value}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>{" "}
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>{" "}
            <div className="flex flex-col lg:flex-row items-start justify-center w-full gap-8 lg:gap-12">
              <FormField
                control={form.control}
                name="height"
                render={({ field }) => (
                  <FormItem className="flex-1 w-full">
                    <FormLabel className="flex items-center justify-start gap-1.5">
                      <p className="capitalize"> {itemsTexts.height.label}</p>
                      <p className="lowercase font-semibold">
                        {isImperial ? "(inch)" : "(cm)"}
                      </p>
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder={itemsTexts.height.placeholder}
                        type={"number"}
                        step={1}
                        {...field}
                      />
                    </FormControl>{" "}
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="weight"
                render={({ field }) => (
                  <FormItem className="flex-1 w-full">
                    <FormLabel className="flex items-center justify-start gap-1.5">
                      <p className="capitalize"> {itemsTexts.weight.label}</p>
                      <p className="lowercase font-semibold">
                        {isImperial ? "(lbs)" : "(kg)"}
                      </p>
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder={itemsTexts.weight.placeholder}
                        type={"number"}
                        step={1}
                        min={0}
                        {...field}
                      />
                    </FormControl>{" "}
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            <div className="flex flex-col lg:flex-row items-start justify-center w-full gap-8 lg:gap-12">
              <FormField
                control={form.control}
                name="intake"
                render={({ field }) => (
                  <FormItem className="flex-1 w-full">
                    <FormLabel className="capitalize">
                      {itemsTexts.intake.label}
                    </FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue
                            placeholder={itemsTexts.intake.placeholder}
                          />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {intakeValues.map(({ title, subtitle, value }) => (
                          <SelectItem
                            key={title}
                            value={value}
                            className="cursor-pointer"
                          >
                            {title}
                            <p className="text-sm text-muted-foreground">
                              {" "}
                              {subtitle}
                            </p>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="gender"
                render={({ field }) => (
                  <FormItem className="flex-1 w-full">
                    <FormLabel>{itemsTexts.gender.label}</FormLabel>
                    <FormControl>
                      <RadioGroup
                        onValueChange={field.onChange}
                        defaultValue={field.value}
                        className="flex  items-center justify-start gap-4"
                      >
                        <FormItem className="flex items-center space-x-3 space-y-0">
                          <FormControl>
                            <RadioGroupItem value="male" />
                          </FormControl>
                          <FormLabel className="font-normal">
                            {genderText.male}
                          </FormLabel>
                        </FormItem>
                        <FormItem className="flex items-center space-x-3 space-y-0">
                          <FormControl>
                            <RadioGroupItem value="female" />
                          </FormControl>
                          <FormLabel className="font-normal">
                            {genderText.female}
                          </FormLabel>
                        </FormItem>
                      </RadioGroup>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            <Button type={"submit"} size={"lg"}>
              {button}
            </Button>
          </form>
        </Form>
        <AnimatePresence>
          {result && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="w-full flex items-center justify-center gap-4"
            >
              <CardDescription className="text-2xl tracking-tighter text-primary flex items-center justify-center gap-1.5">
                {message1}
                <p className={"font-bold"}>{` ${result.toFixed(0)} `}</p>
                {message2}
                <p className={"font-bold"}>
                  {` ${intakeTitles[form.getValues().intake]} `}
                </p>
              </CardDescription>
            </motion.div>
          )}
        </AnimatePresence>
      </CardContent>
    </Card>
  );
}
