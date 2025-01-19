"use client";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import { AiIdeasField, TargetedFields } from "@/types/ai-ideas-types";
import ShinyButton from "@/components/magicui/shiny-button";
import { getToxicity } from "@/actions/toxcity";
import DOMPurify from "dompurify";
import { Checkbox } from "@/components/ui/checkbox";
import { Pen, StopCircle } from "lucide-react";
import { useChat } from "ai/react";

export interface AIGeneratePopTexts {
  anchorText: string;
  descriptionText: string;
  buttonSubmitTexts: ButtonSubmitTexts;
  toxicError: string;
  englishError: string;
  forFunText: string;
  forFunBtnText: string;
  extraContext?: number;
  placeholder: string;
}
export type AIPopCallbackArg =
  | { error: string; answer?: string }
  | { answer: string; error?: string };
export type AIPopCallback = (resp: AIPopCallbackArg) => void;
interface AIPopProps extends AIGeneratePopTexts {
  fields: AiIdeasField[];
  finishCallback: AIPopCallback;
  updateCallback: (arg: string) => void;
  targetedField: TargetedFields;
  item: string;
  checkBoxes: Record<string, string>;
  loadingCallback?: (l: boolean) => void;
  updateDelayMs: number;
}

const MAX_RESPONSE_LENGTH = 250_000;

export default function AIGeneratePop({
  fields,
  finishCallback,
  targetedField,
  item,
  checkBoxes,
  anchorText,
  forFunBtnText,
  buttonSubmitTexts,
  toxicError,
  englishError,
  descriptionText,
  forFunText,
  extraContext = 0,
  placeholder,
  updateCallback,
  updateDelayMs = 10,
  loadingCallback,
}: AIPopProps) {
  const [input, setInput] = useState<string | undefined>();
  // const [isLoading, setIsLoading] = useState<boolean>(false);
  const [popOpen, setPopOpen] = useState<boolean>(false);
  const [showFunBtn, setShowFunBtn] = useState<boolean>(false);
  const [inputErr, setInputErr] = useState<string | undefined>();
  const [sentFields, setSentFields] = useState<AiIdeasField[]>(fields);
  const lastUpdateTime = useRef<number>(-1);

  const {
    messages,
    setMessages,
    handleSubmit: handleChatSubmit,
    isLoading,
    stop,
  } = useChat({
    api: "/api/ai-idea",
    onFinish: (r) => {
      updateCallback(r.content);
      lastUpdateTime.current = -1;
    },
  });
  const hasContext = useMemo(
    () =>
      (sentFields.map((f) => f.content.trim()).join("") + (input || ""))
        .length > 20,
    [sentFields, input],
  );

  const message =
    messages[0]?.content && lastUpdateTime.current !== -1
      ? {
          content: messages[0].content,
          updatedAt: new Date().getTime(),
        }
      : {
          content: undefined,
          updatedAt: -1,
        };
  // console.log("messageAI", message, lastUpdateTime.current);

  useEffect(() => {
    if (!isLoading && lastUpdateTime.current !== -1) {
      lastUpdateTime.current = -1;
    }
  }, [isLoading, lastUpdateTime.current]);

  useEffect(() => {
    if (
      message &&
      message?.content &&
      message.content.length > MAX_RESPONSE_LENGTH
    ) {
      stop();
      updateCallback(message.content);
      lastUpdateTime.current = -1;
    }
  }, [message]);

  useEffect(() => {
    if (
      message.content &&
      lastUpdateTime.current !== -1 &&
      lastUpdateTime.current < message.updatedAt - updateDelayMs
    ) {
      lastUpdateTime.current = message.updatedAt;
      updateCallback(message.content);
    }
  }, [message]);

  useEffect(() => {
    if (loadingCallback) {
      loadingCallback(isLoading);
    }
  }, [isLoading]);

  const handleSubmit = useCallback(async () => {
    setShowFunBtn(false);
    // setIsLoading(true);
    setInputErr(undefined);
    setMessages([]);
    lastUpdateTime.current = new Date().getTime();
    const trimmedInput = input
      ? input.trim().replace(/\s+/g, " ") || undefined
      : undefined;
    if (trimmedInput) {
      const toxicResp = await getToxicity(
        DOMPurify.sanitize(trimmedInput, {
          ALLOWED_TAGS: [],
          ALLOWED_ATTR: [],
        }),
      );
      if (toxicResp.failure) {
        if (toxicResp.reason.toLowerCase() === "toxicity") {
          setInputErr(toxicError);
        } else {
          setInputErr(englishError);
        }
        // setIsLoading(false);
        setInput("");
        return;
      }
    }
    // const resp = await aiIdea({
    //   input: trimmedInput,
    //   targetedField,
    //   fields: sentFields,
    //   item,
    //   extraContext,
    // });

    handleChatSubmit(undefined, {
      allowEmptySubmit: true,
      body: {
        input: trimmedInput,
        targetedField,
        fields: sentFields,
        item,
        extraContext,
      },
    });
    // setIsLoading(false);
    // callback({});
    // setPopOpen(false);
  }, [input, hasContext, sentFields]);

  function handlePopChange(o: boolean) {
    setPopOpen(o);
    setShowFunBtn(false);
    setInputErr(undefined);
    setSentFields((prev) =>
      prev.map((f) => fields.find((fOrg) => fOrg.name === f.name) || f),
    );
  }
  function handleCheckedChange(c: string | boolean, k: string) {
    if (c) {
      setSentFields((p) => {
        const field = p.find((f) => f.name === k);
        if (field) {
          return p;
        }
        const fOrg = fields.find((f) => f.name === k);
        if (fOrg) {
          return [...p, fOrg];
        }
        return p;
      });
    } else {
      setSentFields((p) => p.filter((f) => f.name !== k));
    }
  }

  return (
    <Popover open={popOpen} onOpenChange={handlePopChange}>
      <PopoverTrigger asChild>
        <ShinyButton>
          <span className="flex items-center justify-center gap-2">
            <span className="font-semibold">{anchorText}</span>
            <Pen size={18} />
          </span>
        </ShinyButton>
      </PopoverTrigger>
      <PopoverContent className="shadow-md shadow-shadow_color space-y-4 md:min-w-[420px] md:p-5 z-[15] bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/75 ">
        <p className="text-[17px] tracking-tighter">{descriptionText}</p>
        <div className="space-y-2 my-1 p-1">
          {Object.entries(checkBoxes).map(([k, t]) => (
            <div className="flex items-center space-x-2" key={k}>
              <Checkbox
                id={k}
                disabled={!fields.some((f) => f.name === k)}
                checked={sentFields.some((f) => f.name === k)}
                onCheckedChange={(c) => handleCheckedChange(c, k)}
              />
              <label
                htmlFor={k}
                className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
              >
                {t}
              </label>
            </div>
          ))}
        </div>
        <div className="space-y-4">
          <Textarea
            className="md:min-h-[140px]"
            placeholder={placeholder}
            value={input}
            onChange={(e) => setInput(e.target.value)}
          />
          {!showFunBtn && (
            <div className="w-full h-full flex items-center justify-between">
              <ButtonSubmit
                onClick={async () => {
                  if (!hasContext) {
                    setShowFunBtn(true);
                    return;
                  }
                  await handleSubmit();
                }}
                isLoading={isLoading}
                disable={isLoading}
                size={"default"}
                buttonSubmitTexts={buttonSubmitTexts}
              />
              <Button
                type="button"
                size="icon"
                variant="ghost"
                className="fllex w-10 flex-none items-center justify-center"
                onClick={() => {
                  stop();
                }}
              >
                <StopCircle size={24} />
              </Button>
            </div>
          )}
          {showFunBtn && (
            <div className="space-y-4">
              <p className="text-destructive">{forFunText}</p>
              <Button variant="destructive" onClick={handleSubmit}>
                {forFunBtnText}
              </Button>
            </div>
          )}
        </div>
        {inputErr && (
          <p className="text-destructive font-semibold">{inputErr}</p>
        )}
      </PopoverContent>
    </Popover>
  );
}
