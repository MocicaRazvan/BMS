import fs from "fs/promises";
import path from "path";
import {fileURLToPath} from 'url';
import puppeteer from 'puppeteer';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// todo mereu reconectare pt cookies
// le copiezi din cookies din browser
// scrape dupa faci get la /api/chat/pg-admin sa salvezi in bd
// getul il faci pe localhost !!!
// todo REFA CU NOILE RUTE SI SCOATELE PE CELE CARE DAU 404 manual
// todo pt generare embeddings foloseste pe serverel # OLLAMA_EMBEDDING="zylonai/bge-m3" altfel cel de la chatfire


const cookies = [{
    "domain": "im51.go.ro",
    "expirationDate": 1761480130,
    "hostOnly": true,
    "httpOnly": false,
    "name": "_pk_id.1.45fb",
    "path": "/",
    "sameSite": "lax",
    "secure": false,
    "session": false,
    "storeId": "0",
    "value": "9406497ee04d5d10.1727524930."
}, {
    "domain": "im51.go.ro",
    "expirationDate": 1759145611.372074,
    "hostOnly": true,
    "httpOnly": false,
    "name": "NEXT_LOCALE",
    "path": "/",
    "sameSite": "lax",
    "secure": false,
    "session": false,
    "storeId": "0",
    "value": "en"
}, {
    "domain": "im51.go.ro",
    "hostOnly": true,
    "httpOnly": true,
    "name": "__Host-next-auth.csrf-token",
    "path": "/",
    "sameSite": "lax",
    "secure": true,
    "session": true,
    "storeId": "0",
    "value": "6ce01b7ee9ab5b6f429188b71433631dfaf52bb949c1735d8b86c0ba54bd980a%7Cee53450bb196de2f174080a72f81ade21182646bb665a418b6993442fc5d9839"
}, {
    "domain": "im51.go.ro",
    "expirationDate": 1727710087.889046,
    "hostOnly": true,
    "httpOnly": true,
    "name": "portainer_api_key",
    "path": "/",
    "sameSite": "strict",
    "secure": false,
    "session": false,
    "storeId": "0",
    "value": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJhZG1pbiIsInJvbGUiOjEsInNjb3BlIjoiZGVmYXVsdCIsImZvcmNlQ2hhbmdlUGFzc3dvcmQiOmZhbHNlLCJleHAiOjE3Mjc3MTAwODgsImp0aSI6ImQ0Zjk2ZjhiLWNlOGMtNDViZS05MTY5LTc4NmY5NmNiMTdlNyIsImlhdCI6MTcyNzY4MTI4OH0.52qye5cXZHvQRpyco2gOXluT2I1D7Qa_0hEQe4m6o_s"
}, {
    "domain": "im51.go.ro",
    "expirationDate": 1727729500.241102,
    "hostOnly": true,
    "httpOnly": true,
    "name": "_gorilla_csrf",
    "path": "/",
    "sameSite": "lax",
    "secure": false,
    "session": false,
    "storeId": "0",
    "value": "MTcyNzY4NjMwMHxJamRHYzJSck9XMXpUV3MwU0dkRFNGWmFTMlY1YkU1SE1tTnpkRWR2VjAxd2QwSkpNMHAyYTBzeFNITTlJZ289fHRB_KY_VbVeJB6FQbWlzgdb_l0rzcsmdLLNJhAAdOR7"
}, {
    "domain": "im51.go.ro",
    "hostOnly": true,
    "httpOnly": true,
    "name": "__Secure-next-auth.callback-url",
    "path": "/",
    "sameSite": "lax",
    "secure": true,
    "session": true,
    "storeId": "0",
    "value": "https%3A%2F%2Fim51.go.ro%2Fen%2Fauth%2Fsignin"
}, {
    "domain": "im51.go.ro",
    "expirationDate": 1730278399.639447,
    "hostOnly": true,
    "httpOnly": true,
    "name": "__Secure-next-auth.session-token",
    "path": "/",
    "sameSite": "lax",
    "secure": true,
    "session": false,
    "storeId": "0",
    "value": "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIn0..7Q-ylCbZVQPVuDP3.bKP_XCsPn3Wh6QHZ0J82PWxsI1h-u1zMnZiZcfXgtiy49SugK8ze676t4Yhr4fdLTAEVg7ad-RUZk2XT0yGSjJcpRwqbewBZVveBnOl4hXtoipiP25rBZdKkV6P_ODTg5w3UAf5UsQwrNla_A0PTFM_Sjj0njhahXnHdV94W2fgUfxKPP82zR1SoQpbG6aczjLLq5ZBKgcTm53fOTHvEjXfMl_I0yzLojWMgaLI8XIdD4J-taMYNiWJFqVdwZYxjdbvwaXQuBFx5ZpD-ibET-0O5buVi4oMzXt8TNAKzmgBFRoCxKZLulhuq0G3IWZn9qGgw8V1jo2EEiKqcas5TFuS1V9k7DOX7sfhP0evOAt52y6xlAYErY_kZbQmvUexin2LWJQcDaGR6JMR6KcWNn8UzQoc-zZ9fxD-mSc1GI8ZsLFIpG6an61rIY45cKqPOK3EeHWV3iE61R2iBSgQZOF213ZMTX8n_08Rg-HMzVm2qb8Nc4ApyAPzyHMrSzJwVO1mXak0d1baICmtPBFVOsL1q-5qxUzCLj0cRwVIwE0-NxUgYUegiXuULvalELF1O9WDiUEpZybpu76VP5OFM7P4FuyjQi6HAyEyKlZX-_xy2WQjHci4HU6zXwtHz5IcDP-sl5NSLRNbWHtNUKsEaKMUhAmbMK4PVJ-FaaERpfl_0ssOn5G_bbwhe-faMaRZeY7jKn-3MB77otAVobLRZgCydmmkhJqUqzynuTIZwrWNieVC3fjaGZ4ytQdsIrw_7YFYRwXz_TeKxv86g2c-Fnt6GLK6R1uhmfDkvfe1Ze1pabOJNc9IcH7-EKcIECkdasww9IageQzjevlWzmisRVBMN947Q2T7oH-dyXnEoV20PlH-GXv_U7P-Ha9SUPLOYgmWY0JbrfX7vrqrjo9YFgJ5E-pBKCQh8c6vljFEHeZxaxupjnvhYL1AmSdZl.zUcMEEjKGdNVr-lbadEPKw"
}]


async function getDirectories(source) {
    try {
        const dirents = await fs.readdir(source, {withFileTypes: true});
        const directories = await Promise.all(dirents.map(async (dirent) => {
            const res = path.resolve(source, dirent.name);
            if (dirent.isDirectory()) {
                const nestedDirs = await getDirectories(res); // Recursively get subdirectories
                return [dirent.name, ...nestedDirs.map(nestedDir => path.join(dirent.name, nestedDir))];
            } else {
                return [];
            }
        }));
        return directories
            .flat()
            .map(dir => dir.replace(/\\/g, '/'))
            .map(dir => dir.replace(/\/?\(.*?\)\/?/g, ''))
            .map(dir => dir.trim() === "" ? '/' : dir);
    } catch (error) {
        console.error('Error reading directory:', error);
        return [];
    }
}

function getManualDirs(directories) {
    return directories.filter(dir => /\[.*?]/.test(dir));
}

const manualPaths = [
    {
        url: 'orders/single/119',
        numberMappings: ['[orderId]']
    },
    {
        url: 'plans/single/2',
        numberMappings: ['[planId]']
    },
    {
        url: 'posts/single/67',
        numberMappings: ['[postId]']
    },
    {
        url: 'subscriptions/single/2',
        numberMappings: ['[subscriptionId]']
    },
    {
        url: 'users/single/1',
        numberMappings: ['[userId]']
    },

    {
        url: 'trainer/ingredients/single/8',
        numberMappings: ['[ingredientId]']
    },
    {
        url: 'trainer/plans/single/2',
        numberMappings: ['[planId]']
    },
    {
        url: 'trainer/plans/update/2',
        numberMappings: ['[planId]']
    }, {
        url: 'trainer/plans/duplicate/2',
        numberMappings: ['[planId]']
    },
    {
        url: 'trainer/posts/single/67',
        numberMappings: ['[postId]']
    },
    {
        url: 'trainer/posts/update/67',
        numberMappings: ['[postId]']
    }, {
        url: 'trainer/posts/duplicate/67',
        numberMappings: ['[postId]']
    },
    {
        url: 'trainer/recipes/single/8',
        numberMappings: ['[recipeId]']
    },
    {
        url: 'trainer/recipes/update/8',
        numberMappings: ['[recipeId]']
    }, {
        url: 'trainer/recipes/duplicate/8',
        numberMappings: ['[recipeId]']
    },
    {
        url: 'trainer/user/1/plans',
        numberMappings: ['[userId]']
    },
    {
        url: 'trainer/user/1/plans/dailySales',
        numberMappings: ['[userId]']
    },
    {
        url: 'trainer/user/1/plans/monthlySales',
        numberMappings: ['[userId]']
    },
    {
        url: 'trainer/user/1/posts',
        numberMappings: ['[userId]']
    },
    {
        url: 'trainer/user/1/recipes',
        numberMappings: ['[userId]']
    },
    {
        url: 'trainer/user/1/days',
        numberMappings: ['[userId]']
    },

    {
        url: "/admin/ingredients/single/8",
        numberMappings: ['[ingredientId]']
    },
    {
        url: "/admin/ingredients/update/8",
        numberMappings: ['[ingredientId]']
    }, {
        url: "/admin/ingredients/duplicate/8",
        numberMappings: ['[ingredientId]']
    },
    {
        url: "/admin/orders/single/119",
        numberMappings: ['[orderId]']
    },
    {
        url: "/admin/plans/single/2",
        numberMappings: ['[planId]']
    },
    {
        url: "/admin/posts/single/67",
        numberMappings: ['[postId]']
    },
    {
        url: "/admin/recipes/single/8",
        numberMappings: ['[recipeId]']
    },
    {
        url: "/admin/users/1/dailySales",
        numberMappings: ['[userId]']
    },
    {
        url: "/admin/users/1/monthlySales",
        numberMappings: ['[userId]']
    },
    {
        url: "/admin/users/1/orders",
        numberMappings: ['[userId]']
    },
    {
        url: "/admin/users/1/plans",
        numberMappings: ['[userId]']
    },
    {
        url: "/admin/users/1/posts",
        numberMappings: ['[userId]']
    },
    {
        url: "/admin/users/1/recipes",
        numberMappings: ['[userId]']
    },

    // "/terms-of-service",

]

function sanitizePath(urlPath) {
    const cleanPath = urlPath.split(/[?#]/)[0];

    return cleanPath.replace(/-/g, '/');
}

async function savePageContent(url, baseURL, page, outputDir, nextOutputDir, numberMappings) {
    let urlPath = url.replace(baseURL, '');

    if (numberMappings && numberMappings.length > 0) {
        let nrCount = 0;
        urlPath = urlPath.replace(/\d+/g, match => {
            if (numberMappings[nrCount]) {
                return numberMappings[nrCount++];
            }
            return match;
        })
    }

    const sanitizedPath = sanitizePath(urlPath);
    const folderStructure = sanitizedPath.split('/').filter(part => part);

    let currentDir = outputDir;
    let nextCurrentDir = nextOutputDir;
    for (const folder of folderStructure) {
        currentDir = path.join(currentDir, folder);
        nextCurrentDir = path.join(nextCurrentDir, folder);
    }

    const filePath = path.join(currentDir, 'page.html');
    const nextFilePath = path.join(nextCurrentDir, 'page.html');

    console.log('Saving page content:', url, 'to', filePath, 'and', nextFilePath);

    let html = await page.content();

    if (html.includes("<h2>This page could not be found.</h2>")) {
        console.log('Skipping page:', url, 'due to 404 error.');
        return;
    }

    html = html.replace(/<script[^>]*>[\s\S]*?<\/script>/gi, '');
    html = html.replace(/<next-route-announcer[^>]*>[\s\S]*?<\/next-route-announcer>/gi, '');
    html = html.replace(/\s(class|style|id|srcset|src)="[^"]*"/g, '');
    html = html.replace(/<svg[^>]*>[\s\S]*?<\/svg>/gi, '');
    html = html.replace(/<link[^>]*rel=["'](stylesheet|icon|preload|viewport)["'][^>]*>/gi, '');
    html = html.replace(/<style[^>]*>[\s\S]*?<\/style>/gi, '');
    html = html.replace(/<canvas[^>]*>[\s\S]*?<\/canvas>/gi, '');

    await fs.mkdir(path.dirname(filePath), {recursive: true});
    await fs.writeFile(filePath, html, 'utf8');

    await fs.mkdir(path.dirname(nextFilePath), {recursive: true});
    await fs.writeFile(nextFilePath, html, 'utf8');
}

async function getAutoPaths() {
    return await getDirectories(path.join(__dirname, "..", 'client-next', "src", 'app', '[locale]'))
        .then(directories => directories.filter(dir => !/\[.*?]/.test(dir)));
}

async function visitPage(url, visitedUrls, page, baseURL, outputDir, nextOutputDir, numberMappings) {
    if (visitedUrls.has(url)
        || url.startsWith("gar")
        || url.startsWith("test")
        // || url.includes('admin')

    ) {
        return;
    }
    visitedUrls.add(url);

    console.log('Visiting page:', url);

    const res = await page.goto(baseURL + "/" + url, {waitUntil: 'networkidle0'});


    if (res.ok()) {
        await savePageContent(url, baseURL, page, outputDir, nextOutputDir, numberMappings);
    } else {
        console.warn(`Non-OK status (${res.status()}) for ${url}, skipping save.`);
    }


    console.log('Finished visiting page:', url);
}

// (async () => {
//     const paths = await getAutoPaths().then(p => p.concat(manualPaths))
//     console.log(paths)
// })()


(async () => {
    const browser = await puppeteer.launch({headless: true, acceptInsecureCerts: true});
    const page = await browser.newPage();

    await page.setCookie(...cookies
        .map(({session, hostOnly, storeId, expirationDate, sameSite, ...rest}) =>
            ({...rest, sameSite: "Lax"})));

    const baseURL = 'https://im51.go.ro:443/en';
    // const baseURL = 'http://localhost:3000/en';

    const outputDir = path.join(__dirname, 'output');
    const nextOutputDir = path.join(__dirname, "..", 'client-next', 'scrape');

    await fs.rm(outputDir, {recursive: true, force: true});
    await fs.rm(nextOutputDir, {recursive: true, force: true});

    await fs.mkdir(outputDir, {recursive: true});
    await fs.mkdir(nextOutputDir, {recursive: true});

    const visitedUrls = new Set();


    // const urls = await getAutoPaths().then(paths => paths.concat(manualPaths)).then(urls => [...new Set(urls)]);
    const autoUrls = await getAutoPaths();

    for (const url of autoUrls) {
        await visitPage(url, visitedUrls, page, baseURL, outputDir, nextOutputDir, undefined);
    }

    for (const {url, numberMappings} of manualPaths) {
        await visitPage(url, visitedUrls, page, baseURL, outputDir, nextOutputDir, numberMappings);
    }

    await browser.close();
})();