import { DocumentLoader } from "@langchain/core/document_loaders/base";

import { promises as fs } from "fs";
import { BaseDocumentTransformer, Document } from "@langchain/core/documents";

export class JSONLocaleLoader implements DocumentLoader {
  private readonly filePath: string;

  constructor(filePath: string) {
    this.filePath = filePath;
  }

  async load(): Promise<Document[]> {
    const content = await fs.readFile(this.filePath, "utf-8");
    const parsedJSON = JSON.parse(content);
    const documents: Document[] = [];

    const createDocuments = (obj: any, path = "", depth = 0): void => {
      if (typeof obj === "object" && obj !== null) {
        const keys = Object.keys(obj);
        if (keys.length === 0) return;

        for (const key of keys) {
          const newPath = path ? `${path}.${key}` : key;
          const value = obj[key];

          if (newPath.toLowerCase().includes("sidebar")) {
            continue;
          }

          if (
            (newPath.startsWith("zod") || newPath.startsWith("auth")) &&
            depth === 1
          ) {
            this.addDocument(newPath, value, documents);
          } else if (
            (newPath.startsWith("components") || newPath.startsWith("pages")) &&
            depth === 2
          ) {
            this.addDocument(newPath, value, documents);
          } else {
            createDocuments(value, newPath, depth + 1);
          }
        }
      }
    };

    createDocuments(parsedJSON);

    return documents;
  }

  private addDocument(
    newPath: string,
    value: any,
    documents: Document[],
  ): void {
    const flattenedContent = this.flattenObject(value);
    const combinedContent = Object.entries(flattenedContent)
      .map(([subKey, subValue]) => `${subKey}: ${subValue}`)
      .join("; ");

    documents.push(
      new Document({
        pageContent: combinedContent,
        metadata: {
          source: this.filePath,
          key: newPath,
          locale: this.filePath.split(".")[0].split("/")[1] || "en",
        },
      }),
    );
  }

  private flattenObject(obj: any, parentKey = "", separator = "."): any {
    const flattened: any = {};

    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        const newKey = parentKey ? `${parentKey}${separator}${key}` : key;

        if (
          typeof obj[key] === "object" &&
          obj[key] !== null &&
          !Array.isArray(obj[key])
        ) {
          Object.assign(
            flattened,
            this.flattenObject(obj[key], newKey, separator),
          );
        } else {
          flattened[newKey] = obj[key];
        }
      }
    }

    return flattened;
  }

  async loadAndSplit(
    textSplitter?: BaseDocumentTransformer,
  ): Promise<Document[]> {
    const documents = await this.load();
    if (textSplitter) {
      return textSplitter.transformDocuments(documents);
    }
    return documents;
  }
}
