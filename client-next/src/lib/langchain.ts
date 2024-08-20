import { MemoryVectorStore } from "langchain/vectorstores/memory";
import { DirectoryLoader } from "langchain/document_loaders/fs/directory";
import { TextLoader } from "langchain/document_loaders/fs/text";
import { DocumentInterface } from "@langchain/core/documents";
import { RecursiveCharacterTextSplitter } from "langchain/text_splitter";
import { EmbeddingsFilter } from "langchain/retrievers/document_compressors/embeddings_filter";
import { CustomOllamaEmbeddings } from "@/lib/custom-ollama-embeddings";
import { PoolConfig } from "pg";
import {
  PGVectorStore,
  DistanceStrategy,
} from "@langchain/community/vectorstores/pgvector";

const ollamaBaseUrl = process.env.OLLAMA_BASE_URL;
const embeddingModel = process.env.OLLAMA_EMBEDDING;
const siteUrl = process.env.NEXTAUTH_URL;
const keepAlive = "-1m";

const pgConfig = {
  postgresConnectionOptions: {
    type: "postgres",
    host: process.env.POSTGRES_HOST,
    port: parseInt(process.env.POSTGRES_PORT || "5432"),
    user: process.env.POSTGRES_USER,
    password: process.env.POSTGRES_PASSWORD,
    database: process.env.POSTGRES_DB,
  } as PoolConfig,
  tableName: "langchain_vector_store",
  columns: {
    idColumnName: "id",
    vectorColumnName: "vector",
    contentColumnName: "content",
    metadataColumnName: "metadata",
  },
  distanceStrategy: "cosine" as DistanceStrategy,
};

if (!ollamaBaseUrl || !embeddingModel || !siteUrl) {
  throw new Error(
    "OLLAMA_BASE_URL, OLLAMA_EMBEDDING and NEXTAUTH_URL must be set in the environment",
  );
}

export class VectorStoreSingleton {
  private vectorStore: MemoryVectorStore | undefined;
  private embeddings: CustomOllamaEmbeddings | undefined;
  private filter: EmbeddingsFilter | undefined;
  private isInitialized = false;
  private pgVectorStore: PGVectorStore | undefined;

  constructor() {
    //
  }

  public async initialize(reset = false, memory = true) {
    if (reset || !this.isInitialized) {
      console.log("Initializing or reinitializing vector store and embeddings");

      if (!this.embeddings) {
        this.embeddings = new CustomOllamaEmbeddings({
          model: embeddingModel,
          baseUrl: ollamaBaseUrl,
          keepAlive,
          requestOptions: {
            keepAlive,

            // numCtx: 2048,
          },
        });
      }

      if (memory && (!this.vectorStore || reset)) {
        const splitDocs = await VectorStoreSingleton.generateEmbeddings();
        this.vectorStore = await MemoryVectorStore.fromDocuments(
          splitDocs,
          this.embeddings,
        );
      }

      if (!memory && (!this.pgVectorStore || reset)) {
        this.pgVectorStore = await PGVectorStore.initialize(
          this.embeddings,
          pgConfig,
        );
      }

      if (!this.filter || reset) {
        this.filter = new EmbeddingsFilter({
          embeddings: this.embeddings,
          // todo put in env
          similarityThreshold: 0.9,
          // k: 25,
        });
      }

      this.isInitialized = true;
      console.log("Vector store and embeddings initialized");

      // vectorStoreInstance = this;
      //   if (process.env.NODE_ENV !== "production")
      globalThis.vectorStoreGlobal = this;
      vectorStoreInstance = globalThis.vectorStoreGlobal;
    }
  }

  public async addToPGVectorStore() {
    if (!this.isInitialized) {
      await this.initialize(false, false);
    }
    await this.pgVectorStore?.addDocuments(
      await VectorStoreSingleton.generateEmbeddings(),
    );
  }

  public async getPGVectorStore(): Promise<PGVectorStore | undefined> {
    if (!this.isInitialized) {
      await this.initialize();
    }
    return this.pgVectorStore;
  }

  public async getFilter(): Promise<EmbeddingsFilter | undefined> {
    if (!this.isInitialized) {
      await this.initialize();
    }
    return this.filter;
  }

  public async getVectorStore(): Promise<MemoryVectorStore | undefined> {
    if (!this.isInitialized) {
      await this.initialize();
    }
    return this.vectorStore;
  }

  public async getEmbeddings(): Promise<CustomOllamaEmbeddings | undefined> {
    if (!this.isInitialized) {
      await this.initialize();
    }
    return this.embeddings;
  }

  public static async generateEmbeddings() {
    const [
      tsxDocs,
      // , jsonDocs
    ] = await Promise.all([
      VectorStoreSingleton.generateTSXEmbeddings(),
      // VectorStoreSingleton.generateJSONEmbeddings(),
    ]);
    // console.log(tsxDocs.length, jsonDocs.length);
    // // console.log(tsxDocs[0], jsonDocs[0]);
    // const tokenizeJSON = await VectorStoreSingleton.tokenizeDocuments(jsonDocs);
    // console.log(tokenizeJSON.length);
    // console.log(
    //   JSON.stringify(jsonDocs).length,
    //   JSON.stringify(tokenizeJSON).length,
    //);
    // return [...tsxDocs, ...jsonDocs];
    return tsxDocs;
  }

  public static async gar(m = 512) {
    // return await VectorStoreSingleton.generateJSONEmbeddings().then((e) =>
    //   this.tokenizeDocuments(e, m),
    // );
    return [1];
  }

  private static async tokenizeDocuments(
    docs: DocumentInterface[],
    maxTokensPerChunk = 512,
  ): Promise<DocumentInterface[]> {
    const splitter = new RecursiveCharacterTextSplitter({
      chunkSize: maxTokensPerChunk,
    });

    return await docs.reduce(
      async (accPromise, doc) => {
        const acc = await accPromise;
        const chunks = await splitter.splitText(doc.pageContent);

        const combinedChunks: string[] = [];
        let currentChunk = "";

        chunks.forEach((chunk) => {
          if (currentChunk.length + chunk.length <= maxTokensPerChunk) {
            currentChunk += ` ${chunk}`.trim();
          } else {
            combinedChunks.push(currentChunk);
            currentChunk = chunk;
          }
        });

        if (currentChunk) {
          combinedChunks.push(currentChunk);
        }

        const chunkedDocs = combinedChunks.map((chunk, index) => ({
          ...doc,
          pageContent: chunk,
          id: `${doc.id}_${index}`,
        }));

        return acc.concat(chunkedDocs);
      },
      Promise.resolve([] as DocumentInterface[]),
    );
  }

  public static async generateJSONEmbeddings() {
    return null;
    //   const [enDocs, roDocs] = await Promise.all([
    //     new JSONLocaleLoader("messages/en.json").load(),
    //     new JSONLocaleLoader("messages/ro.json").load(),
    //   ]);
    //
    //   console.log(enDocs);
    //
    //   return [...enDocs, ...roDocs].map(
    //     (doc, i): DocumentInterface => ({
    //       pageContent: doc.pageContent,
    //       metadata: {
    //         scope: `Page texts with with key ${doc.metadata.key} and for language with locale ${doc.metadata.locale}. These are texts never reference them directly.`,
    //       },
    //       id: i.toString() + "_" + doc.metadata.key + "_" + doc.metadata.locale,
    //     }),
    //   );
  }

  private static async generateTSXEmbeddings() {
    const loader = new DirectoryLoader(
      // "src/app/[locale]/(main)",
      //`${process.cwd()}/src/app/`,
      //   "../app/",
      "scrape/",
      {
        // ".tsx": (path) => new TextLoader(path),
        // ".json": (path) => new JSONLoader(path),
        ".html": (path) => new TextLoader(path),
      },
      true,
    );

    // const docs = (await loader.load())
    //   .filter((doc) => doc.metadata.source.endsWith("page.tsx"))
    //   .map((doc, i): DocumentInterface => {
    //     const url =
    //       doc.metadata.source
    //         .replace(/\\/g, "/")
    //         .split("/src/app/[locale]")[1]
    //         .split("/page.")[0]
    //         .replace(/\([^)]*\)/g, "")
    //         .replace(/\/+/g, "/") || "/";
    //
    //     const pageContentTrimmed = doc.pageContent
    //       .replace(/^import.*$/gm, "") // Remove all import statements
    //       .replace(/ className=(["']).*?\1| className={.*?}/g, "") // Remove all className props
    //       .replace(/^\s*[\r]/gm, "") // remove empty lines
    //       .trim();
    //
    //     return {
    //       pageContent: pageContentTrimmed,
    //       metadata: {
    //         scope:
    //           "Page URL: " +
    //           url +
    //           " . Where you see URL parts in [] , for example: [id] ,  ask the user for more info about these parameters. Never put them in the sentence directly.",
    //       },
    //     };
    //   });

    const docs = (await loader.load()).map((doc, i) => {
      const url =
        doc.metadata.source
          .replace(/\\/g, "/")
          .split("/scrape")[1]
          .split("/page.")[0]
          .replace(/\([^)]*\)/g, "")
          .replace(/\/+/g, "/") || "/";

      const fullUrl = siteUrl + "/en" + url;

      return {
        pageContent: doc.pageContent,
        metadata: {
          scope: "HTML page of the website",
          source: doc.metadata.source,
        },
      };
    });

    const splitter = RecursiveCharacterTextSplitter.fromLanguage("html", {
      chunkSize: 1000,
      chunkOverlap: 200,
    });

    const splitDocs = (await splitter.splitDocuments(docs)).map((doc, i) => ({
      ...doc,
      id: i.toString() + "_" + doc.metadata.scope + "_" + doc.metadata.source,
    }));

    console.log(splitDocs.map((d) => d.id));

    console.log("Embeddings generated");

    return splitDocs;
  }
}
const vectorStoreSingleton = () => {
  return new VectorStoreSingleton();
};

declare const globalThis: {
  vectorStoreGlobal: VectorStoreSingleton | undefined;
} & typeof global;

let vectorStoreInstance =
  globalThis.vectorStoreGlobal ?? vectorStoreSingleton();

// if (process.env.NODE_ENV !== "production")
globalThis.vectorStoreGlobal = vectorStoreInstance;
export { vectorStoreInstance };
