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
// todo inainte disable modsecurity

const cookies = [{
    "domain": "im51.go.ro",
    "expirationDate": 1763877515,
    "hostOnly": true,
    "httpOnly": false,
    "name": "_pk_id.1.45fb",
    "path": "/",
    "sameSite": "lax",
    "secure": false,
    "session": false,
    "storeId": "0",
    "value": "dc8ce2a307b78525.1729922315."
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
    "value": "430ed7e4e2f39c16fed5e6bb5a3de87734d7c99549ffc56695337d5a7d13a5b9%7C4c6dbdf70edb715fe0d8379e2bb54d3724a183ab2da6062b4be3eaee8fb8f04a"
}, {
    "domain": "im51.go.ro",
    "expirationDate": 1762189738.080164,
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
    "httpOnly": false,
    "name": "googleState",
    "path": "/",
    "sameSite": "unspecified",
    "secure": false,
    "session": true,
    "storeId": "0",
    "value": "3521de1d-4d30-48f4-88c5-8bdd43a59768"
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
    "expirationDate": 1733933927.840518,
    "hostOnly": true,
    "httpOnly": true,
    "name": "grafana_session",
    "path": "/",
    "sameSite": "lax",
    "secure": false,
    "session": false,
    "storeId": "0",
    "value": "b2f16bf16af88041ff0e0e22009ff358"
}, {
    "domain": "im51.go.ro",
    "expirationDate": 1734343097.591985,
    "hostOnly": true,
    "httpOnly": true,
    "name": "__Secure-next-auth.session-token",
    "path": "/",
    "sameSite": "lax",
    "secure": true,
    "session": false,
    "storeId": "0",
    "value": "eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2R0NNIn0..zjsb-FN7x1f1wq9b.dzfDpi_I3qvoZpGpQYPagbt0Vhn9qxdVTu_Ss3uiNam44ooiju7JmgIcb5y90t27tF9zeR0QAERfsOSwGcPhn3PTtgi11ohivyt9ol5E8FeGnDQzLUHa4hwz6xg8Ye8A29YA_yg66ghhdqtHEQGZjitNXfPpKiZ9z88XkJtLfWGvoKZaMLg8o4AVSXi7I4OkoJDv63rnZ6DSiOZU25jWyQRFzeVX6f55qx7z192JH49Ko-ac9ZtPVRhQZko38xPDZ4ll8FxHEtQhzJobtLXODfCFTqHEKiiRhCkUTAu2FMU99knToMaj9qLznzD-_-nhlvPQvx4MMXTLSogFqmEukR6Ui4T0qp1BGzgc_f3S_xYl_h50BsIckHGhnEXejsr4ernlvsERNCnC4nijs3nkwUgq6EhFnoGaPH5vpC0btmb94uaU5nzx-gqMmnGJ6KlLXOFQhb4l48WQDV9L9Wj3eg6Pr_RI8mRSo02a3mhPv7eJTKbbw_LM5v8I-9zRzH62MxG8LrZkl9Gd8QpOZNKZ0asgSV6cxfWx7MfiF85qvDKe2CwWji87JVPrF7-I34SrmcULLrTRBRITgvWNiTEAylpEZIBHTNPrD3gN1bb062gpKe_LQ-A0j_APcxMOXX8NqW_vxTkq3Zod4MsdQQfUfdeJLuK51HECbrsl_wAUp-0WJbossfyvJOjezfO5Ab8t-1WFFdOpxlfrSZ6ekSu9_GBD-veEXnb7ZcspHFbnIadnopMKNP-wbm118AI_mxtgHblsSqhXhlevfyOW35tGSWbeRKdY69aro3CkngtT2y4lu__h4CzlshtOhK0yhnMfn3SqbZdNfhmKYxvGivVcA6MGbV9gb4zk7dxFxIwp8k4Y-aoHpklt0pd1PUn1qvcumZL8ee6TaU8aHYW-Ejca4JADQHygshqivAOWmw9cJa9diskEplm-IcgXYTDvsscnZA.Chan3bNzZUlH4LCPdb_jWQ"
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