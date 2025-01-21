"use server";
import * as toxicity from "@tensorflow-models/toxicity";
import { detect } from "tinyld";
import { emitInfo } from "@/logger";
import lande from "lande";

const threshold = process.env.TOXICITY_THRESHOLD
  ? parseFloat(process.env.TOXICITY_THRESHOLD)
  : 0.5;
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
    emitInfo({
      message: "Toxicity Model Loaded",
      threshold,
      labels,
    });
  }
  return globalModel;
}
export async function getToxicity(text: string) {
  const tinyDet = detect(text) === "en";
  const landeDet = lande(text)[0][0] === "eng";

  const isOneEnglish = tinyDet || landeDet;

  if (!isOneEnglish && text.length > 10) {
    return {
      failure: true,
      reason: TOXIC_REASON.LANGUAGE,
      message: "ENGLISH ONLY",
    };
  }
  const model = await loadGlobalModel();
  const predictions = await model.classify(text);

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
