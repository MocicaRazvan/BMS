import { NextRequest, NextResponse } from "next/server";
import { vectorStoreInstance } from "@/lib/langchain";
import { Document as LangDocument } from "langchain/document";
import { RecursiveCharacterTextSplitter } from "langchain/text_splitter";
import { MemoryVectorStore } from "langchain/vectorstores/memory";
import { ChatOllama } from "@langchain/ollama";
import { ChatPromptTemplate } from "@langchain/core/prompts";
import { createRetrievalChain } from "langchain/chains/retrieval";
import { createStuffDocumentsChain } from "langchain/chains/combine_documents";

const description = `"<h1>The Comprehensive Guide to the Mediterranean Diet: A Path to Long-Term Health and Wellness</h1>

<h2>Introduction: Understanding the Mediterranean Diet</h2>
<p>
    The Mediterranean diet is more than just a diet; it is a lifestyle rooted in the traditional eating patterns of countries bordering the Mediterranean Sea, such as Greece, Italy, and Spain. Renowned for its numerous health benefits, the Mediterranean diet has been recognized as one of the healthiest dietary patterns in the world. It emphasizes whole, minimally processed foods, healthy fats, and a balanced approach to nutrition that promotes longevity and well-being.
</p>
<p>
    In this extensive guide, we will explore the principles of the Mediterranean diet, the science behind its health benefits, how to implement it in your daily life, and tips for maximizing its potential to improve your overall health.
</p>

<h2>What is the Mediterranean Diet?</h2>
<p>
    The Mediterranean diet is not a strict regimen, but rather a flexible eating pattern that emphasizes a variety of nutrient-rich foods. It is characterized by high consumption of vegetables, fruits, whole grains, legumes, nuts, seeds, and olive oil. Moderate intake of fish, poultry, dairy, and wine is also encouraged, while red meat and sweets are limited.
</p>

<h3>Core Components of the Mediterranean Diet</h3>
<ul>
    <li><strong>Vegetables:</strong> The diet is rich in a wide variety of vegetables, which are often the cornerstone of meals.</li>
    <li><strong>Fruits:</strong> Fresh, seasonal fruits are consumed daily, providing essential vitamins, minerals, and fiber.</li>
    <li><strong>Whole Grains:</strong> Whole grains such as barley, oats, and brown rice are preferred over refined grains.</li>
    <li><strong>Legumes:</strong> Beans, lentils, and chickpeas are common sources of plant-based protein.</li>
    <li><strong>Nuts and Seeds:</strong> Almonds, walnuts, and flaxseeds are consumed regularly, offering healthy fats and protein.</li>
    <li><strong>Olive Oil:</strong> Extra virgin olive oil is the primary source of fat, known for its heart-healthy monounsaturated fats.</li>
    <li><strong>Fish and Seafood:</strong> Fatty fish like salmon, sardines, and mackerel are consumed several times a week, providing omega-3 fatty acids.</li>
    <li><strong>Poultry and Eggs:</strong> Consumed in moderation, these provide lean protein sources.</li>
    <li><strong>Dairy:</strong> Cheese and yogurt are enjoyed in moderation, often made from goat or sheep milk.</li>
    <li><strong>Wine:</strong> Red wine is consumed in moderation, typically with meals.</li>
    <li><strong>Herbs and Spices:</strong> Fresh herbs and spices are used to flavor foods instead of salt.</li>
</ul>

<h3>Foods to Limit in the Mediterranean Diet</h3>
<ul>
    <li><strong>Red Meat:</strong> Limited to a few times a month, with an emphasis on lean cuts.</li>
    <li><strong>Processed Foods:</strong> Minimally processed or avoided entirely.</li>
    <li><strong>Refined Sugars:</strong> Sweets and sugary drinks are consumed sparingly.</li>
    <li><strong>Refined Grains:</strong> White bread, pasta, and rice are limited.</li>
    <li><strong>Butter and Margarine:</strong> Replaced with healthy fats like olive oil.</li>
</ul>

<h2>The Science-Backed Health Benefits of the Mediterranean Diet</h2>
<p>
    The Mediterranean diet has been extensively studied and linked to numerous health benefits. Below are some of the most well-documented advantages of adopting this dietary pattern:
</p>

<h3>1. Cardiovascular Health</h3>
<p>
    One of the most significant benefits of the Mediterranean diet is its ability to protect against heart disease. The diet's emphasis on healthy fats, such as those found in olive oil, fish, and nuts, helps lower LDL (bad) cholesterol levels and increase HDL (good) cholesterol. The abundance of fruits, vegetables, and whole grains also provides antioxidants and fiber, which contribute to heart health. Studies have shown that individuals following the Mediterranean diet have a reduced risk of heart attacks, strokes, and cardiovascular mortality.
</p>

<h3>2. Weight Management</h3>
<p>
    The Mediterranean diet is not a calorie-restrictive plan, yet it can aid in weight management. The high fiber content from fruits, vegetables, legumes, and whole grains helps you feel full and satisfied, reducing the likelihood of overeating. Additionally, the healthy fats from olive oil and nuts provide satiety, making it easier to maintain a healthy weight over the long term.
</p>

<h3>3. Type 2 Diabetes Prevention and Management</h3>
<p>
    The Mediterranean diet's emphasis on whole grains, legumes, and healthy fats can help regulate blood sugar levels and improve insulin sensitivity. Research suggests that this dietary pattern is effective in preventing the onset of type 2 diabetes and managing blood glucose levels in individuals with the condition.
</p>

<h3>4. Cognitive Health and Longevity</h3>
<p>
    The Mediterranean diet is associated with better cognitive function and a lower risk of cognitive decline. The high intake of antioxidants from fruits and vegetables, along with omega-3 fatty acids from fish, supports brain health. Studies have shown that adherence to the Mediterranean diet may reduce the risk of Alzheimer's disease and other forms of dementia. Moreover, the diet's anti-inflammatory properties contribute to overall longevity, with many adherents living longer, healthier lives.
</p>

<h3>5. Cancer Prevention</h3>
<p>
    Several components of the Mediterranean diet, including high consumption of fruits, vegetables, and whole grains, are linked to a reduced risk of certain cancers. The diet's antioxidant-rich foods help combat oxidative stress, a key factor in cancer development. Additionally, the diet's emphasis on plant-based foods and healthy fats may protect against cancers of the breast, prostate, and colon.
</p>

<h3>6. Anti-Inflammatory Effects</h3>
<p>
    Chronic inflammation is a common factor in many diseases, including heart disease, diabetes, and cancer. The Mediterranean diet is rich in anti-inflammatory foods, such as olive oil, nuts, fish, and leafy greens. These foods help reduce markers of inflammation in the body, promoting overall health and reducing the risk of chronic diseases.
</p>

<h2>How to Implement the Mediterranean Diet in Your Daily Life</h2>
<p>
    Transitioning to the Mediterranean diet can be both enjoyable and sustainable. Here are some practical tips to help you incorporate this healthy eating pattern into your daily routine:
</p>

<h3>1. Start with Vegetables and Fruits</h3>
<p>
    Make vegetables and fruits the foundation of your meals. Aim to fill at least half of your plate with a variety of colorful vegetables and fruits at each meal. Experiment with different cooking methods, such as roasting, grilling, and saut├⌐ing, to bring out the natural flavors of your produce.
</p>

<h3>2. Choose Whole Grains</h3>
<p>
    Replace refined grains with whole grains such as brown rice, quinoa, barley, and whole-wheat bread. Whole grains are more nutrient-dense and provide sustained energy throughout the day. They are also rich in fiber, which aids digestion and supports heart health.
</p>

<h3>3. Embrace Healthy Fats</h3>
<p>
    Olive oil should be your primary cooking fat. Use it in dressings, marinades, and for saut├⌐ing vegetables. Incorporate other healthy fats, such as avocados, nuts, and seeds, into your diet. These fats not only add flavor but also support heart health and satiety.
</p>

<h3>4. Include Fish and Seafood</h3>
<p>
    Aim to eat fish and seafood at least twice a week. Fatty fish like salmon, sardines, and mackerel are particularly beneficial due to their high omega-3 fatty acid content. Try grilling, baking, or poaching fish for a delicious and healthy meal.
</p>

<h3>5. Enjoy Dairy in Moderation</h3>
<p>
    Choose low-fat or full-fat dairy products like yogurt, cheese, and milk, but consume them in moderation. Opt for plain yogurt and add fresh fruit or a drizzle of honey for natural sweetness. Cheese can be a flavorful addition to meals, but it's best enjoyed in small quantities.
</p>

<h3>6. Savor a Glass of Wine</h3>
<p>
    If you enjoy wine, particularly red wine, you can include it in your diet in moderation. The Mediterranean diet typically includes one glass of wine per day with meals. However, it's important to note that wine should be consumed in moderation and is not necessary for everyone.
</p>

<h3>7. Spice Up Your Meals</h3>
<p>
    Use herbs and spices to add flavor to your dishes without relying on salt. Fresh herbs like basil, oregano, rosemary, and parsley are staples in Mediterranean cooking. Spices such as cumin, coriander, and paprika can also enhance the taste and nutritional value of your meals.
</p>

<h2>Meal Planning and Recipes for the Mediterranean Diet</h2>
<p>
    Incorporating the Mediterranean diet into your life is easier with a bit of meal planning. Here are some meal ideas to get you started:
</p>

<h3>Breakfast</h3>
<ul>
    <li>Greek yogurt with fresh berries, honey, and a sprinkle of nuts.</li>
    <li>Whole-grain toast topped with avocado, tomato slices, and a poached egg.</li>
    <li>Oatmeal with chopped walnuts, cinnamon, and sliced apples.</li>
</ul>

<h3>Lunch</h3>
<ul>
    <li>Quinoa salad with chickpeas, cucumbers, tomatoes, feta cheese, and a lemon-olive oil dressing.</li>
    <li>Grilled chicken wrap with whole-wheat tortilla, mixed greens, hummus, and roasted vegetables.</li>
    <li>Vegetable and bean soup with a side of whole-grain bread.</li>
</ul>

<h3>Dinner</h3>
<ul>
    <li>Baked salmon with a side of roasted sweet potatoes and steamed broccoli.</li>
    <li>Whole-wheat pasta with tomato sauce, saut├⌐ed spinach, and grilled shrimp.</li>
    <li>Grilled lamb chops with a Greek salad and a side of brown rice.</li>
</ul>

<h3>Snacks</h3>
<ul>
    <li>Almonds or walnuts with a piece of fresh fruit.</li>
    <li>Hummus with carrot sticks or whole-grain crackers.</li>
    <li>Olives with a small piece of cheese.</li>
</ul>

<h2>Common Myths About the Mediterranean Diet</h2>

<h3>1. ""The Mediterranean Diet Is Expensive""</h3>
<p>
    While certain Mediterranean foods like olive oil, fish, and nuts can be more costly, there are many budget-friendly options. Seasonal vegetables, legumes, whole grains, and eggs are affordable and make up the bulk of the diet. Planning meals around sales and using frozen or canned vegetables can also help reduce costs.
</p>

<h3>2. ""You Have to Drink Wine for the Diet to Be Effective""</h3>
<p>
    Although moderate wine consumption is a part of the traditional Mediterranean diet, it is not essential for everyone. The dietΓÇÖs health benefits come from its overall pattern of whole foods, not just wine. If you don't drink alcohol, you can still fully benefit from the Mediterranean diet by focusing on its core components.
</p>

<h3>3. ""The Mediterranean Diet Is Only for Weight Loss""</h3>
<p>
    While the Mediterranean diet can support weight loss, it is primarily a heart-healthy eating pattern that promotes overall wellness. It is sustainable and suitable for long-term adherence, providing benefits that extend beyond weight management, such as improved heart health, cognitive function, and reduced inflammation.
</p>

<h3>4. ""You CanΓÇÖt Eat Meat on the Mediterranean Diet""</h3>
<p>
    The Mediterranean diet does not eliminate meat but encourages moderation. Red meat is limited to a few times a month, while lean meats like chicken and turkey are consumed more frequently. The focus is on portion control and quality, with an emphasis on plant-based foods.
</p>

<h2>Conclusion: Embracing the Mediterranean Diet for a Healthier Life</h2>
<p>
    The Mediterranean diet is more than just an eating plan; it is a way of life that promotes health, longevity, and a deep appreciation for good food. By focusing on whole, nutrient-dense foods, healthy fats, and a balanced approach to eating, the Mediterranean diet offers a sustainable and enjoyable path to better health.
</p>
<p>
    Whether your goal is to improve heart health, manage weight, or simply adopt a healthier lifestyle, the Mediterranean diet provides a flexible and delicious way to achieve those goals. Start by making small changes, such as incorporating more vegetables, switching to whole grains, and using olive oil as your primary fat. Over time, these habits can lead to significant improvements in your overall health and well-being.
</p>
<p>
    Remember, the Mediterranean diet is not about restriction but about enjoying a variety of foods in moderation. It encourages you to savor your meals, share them with others, and take pleasure in the process of cooking and eating. By embracing the Mediterranean way of eating, you can embark on a journey to a healthier, happier life.
</p>
<p>
    Begin your journey today by incorporating the principles of the Mediterranean diet into your daily routine. With its proven health benefits and delicious foods, the Mediterranean diet is a powerful tool for enhancing your quality of life and achieving lasting wellness.
</p>
"`;
const type = "Lose weight";

const modelName = process.env.OLLAMA_MODEL;
const ollamaBaseUrl = process.env.OLLAMA_BASE_URL;
if (!modelName || !ollamaBaseUrl) {
  throw new Error(
    "OLLAMA_MODEL and OLLAMA_BASE_URL must be set in the environment",
  );
}

async function getHtmlDocs(html: string) {
  return (
    await RecursiveCharacterTextSplitter.fromLanguage("html", {
      chunkSize: 500,
      chunkOverlap: 100,
    }).splitText(html)
  ).map(
    (chunk) =>
      new LangDocument({
        pageContent: chunk,
        metadata: {
          source: "Form field for description",
        },
      }),
  );
}

export async function POST(req: NextRequest) {
  const descDocs = await getHtmlDocs(description);
  const typeDoc = new LangDocument({
    pageContent: type,
    metadata: {
      source: "Form field for type",
    },
  });
  const embeddings = await vectorStoreInstance.getEmbeddings();
  if (!embeddings) {
    return new Response("Error getting embeddings", { status: 500 });
  }

  const vectorDb = await MemoryVectorStore.fromDocuments(
    descDocs.concat(typeDoc),
    embeddings,
  );
  const llm = new ChatOllama({
    model: modelName,
    baseUrl: ollamaBaseUrl,
    keepAlive: "-1m",
    temperature: process.env.OLLAMA_TEMPERATURE
      ? parseFloat(process.env.OLLAMA_TEMPERATURE)
      : 0.7,
    cache: false,
  });

  const prompt = ChatPromptTemplate.fromMessages([
    [
      "system",
      `You are an advanced AI language model tasked with assisting users in generating highly engaging and accurate titles based on the provided context. 
    Your goal is to produce a title that is clear, concise, and relevant to the content, capturing the essence of the given information. 
    Keep the audience in mind and ensure the title is attention-grabbing while remaining appropriate to the context. 
    **Always format your messages in markdown.** 
    **Only provide the titles without any additional explanation or commentary.**`,
    ],
    [
      "user",
      "Here is the context: {context}. Based on this, generate a suitable title.",
    ],
  ]);
  const combineDocsChain = await createStuffDocumentsChain({
    llm,
    prompt,
  });

  const retrievalChain = await createRetrievalChain({
    combineDocsChain,
    retriever: vectorDb.asRetriever({
      searchType: "mmr",
      k: 20,
    }),
  });

  const resp = await retrievalChain.invoke({
    input: "Chicken",
  });
  console.log(resp);
  return NextResponse.json(resp, { status: 200 });
}
