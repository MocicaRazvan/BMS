"use server";
import * as toxicity from "@tensorflow-models/toxicity";
import LanguageDetect from "languagedetect";
import { franc } from "franc-min";
import { detect } from "tinyld";

const lngDetector = new LanguageDetect();

const threshold = 0.35;
let globalModel: toxicity.ToxicityClassifier | null = null;

enum TOXIC_REASON {
  LANGUAGE = "language",
  TOXICITY = "toxicity",
  NONE = "none",
}
export async function loadGlobalModel(): Promise<toxicity.ToxicityClassifier> {
  if (!globalModel) {
    const labels = [
      "identity_attack",
      "insult",
      "obscene",
      "severe_toxicity",
      "sexual_explicit",
      "threat",
      "toxicity",
    ];
    globalModel = await toxicity.load(threshold, labels);
  }
  return globalModel;
}
export async function getToxicity(text: string) {
  console.log("TEXT", text);
  const lang = franc(text, { minLength: 2 }) === "eng";
  const tangDet = lngDetector.detect(text, 5).some((l) => l[0] === "english");
  const tinyDet = detect(text) === "en";

  console.log("LANG", franc(text, { minLength: 2 }));
  console.log("DETECT", lngDetector.detect(text, 5));
  console.log("TINY", detect(text));
  console.log("text.length", text.length);
  if (!lang && !tangDet && !tinyDet && text.length > 10) {
    return {
      failure: true,
      reason: TOXIC_REASON.LANGUAGE,
      message: "ENGLISH ONLY",
    };
  }
  const model = await loadGlobalModel();
  const predictions = await model.classify(text);
  console.log("PREDICTIONS", JSON.stringify(predictions, null, 2));

  const isToxic = predictions.some((p) => p.results[0].match);
  if (isToxic) {
    return {
      failure: true,
      reason: TOXIC_REASON.TOXICITY,
      message: "TOXIC",
    };
  }
  return {
    failure: false,
    reason: TOXIC_REASON.NONE,
    message: "CLEAN",
  };
}
