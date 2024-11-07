import {DirectoryLoader} from "langchain/document_loaders/fs/directory";
import {TextLoader} from "langchain/document_loaders/fs/text";
import {RecursiveCharacterTextSplitter} from "langchain/text_splitter";
import * as cheerio from "cheerio";
import path from "path";
import {fileURLToPath} from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

function parseHTMLWithCheerio(htmlContent, source) {
    const $ = cheerio.load(htmlContent);
    const slugs = source.match(/\[[^\]]*]/g);

    const title = $("title").text();
    const metaDescription = $('meta[name="description"]').attr("content") || "";
    let canonical = $('link[rel="canonical"]').attr("href") || "";
    const keywords = $('meta[name="keywords"]').attr("content") || "";
    const bodyContent = $("body").text().trim();

    let numberCount = 0;
    canonical = canonical.replace(/\d+/g, (match) => {
        numberCount += 1;
        if (numberCount > 2) {
            if (slugs?.[numberCount - 3]) {
                return slugs[numberCount - 3];
            }
            return "[id]";
        }
        return match;
    });

    return {title, metaDescription, canonical, keywords, bodyContent};
}

// const dir = "output/";
const dir = path.join(__dirname, "..", 'client-next', 'scrape');
const loader = new DirectoryLoader(
    dir,
    {
        ".html": (path) => new TextLoader(path),
    },
    true,
);

(async () => {
    const docs = await loader.load();
    const mapped = docs.map((d, i) => {
        const {
            title,
            metaDescription,
            canonical,
            keywords,
            bodyContent
        } = parseHTMLWithCheerio(d.pageContent, d.metadata.source);
        console.log({
            i,
            source: d.metadata.source,
            length: bodyContent.length,
            canonical
        })
        return {
            pageContent: bodyContent,
            metadata: {
                scope: "Part of parsed HTML page of the website",
                url: canonical,
                keywords,
                title,
                source: canonical,
                description: metaDescription,
            },
        };
    })
    const splitter = new RecursiveCharacterTextSplitter({
        chunkSize: 1000,
        chunkOverlap: 300,
    });
    const splitDocs = (await splitter.splitDocuments(mapped)).map(((d, i) => {
        console.log(i, d.metadata.source)
        return {
            ...d,
            id: i.toString() + "_" + d.metadata.scope + "_" + d.metadata.source,
        }
    }));

    const docsWithoutSource = splitDocs.filter(d => !d.metadata.source);
    console.log("Split docs size", splitDocs.length);
    console.log(docsWithoutSource.length)
})()
