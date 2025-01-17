export function getOllamaArgs() {
  const modelName = process.env.OLLAMA_MODEL;
  const ollamaBaseUrl = process.env.OLLAMA_BASE_URL;
  if (!modelName || !ollamaBaseUrl) {
    throw new Error(
      "OLLAMA_MODEL and OLLAMA_BASE_URL must be set in the environment",
    );
  }
  return { modelName, ollamaBaseUrl };
}
