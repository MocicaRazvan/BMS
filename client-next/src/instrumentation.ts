export async function register() {
  console.log("register function called");

  if (process.env.NEXT_RUNTIME === "nodejs") {
    console.log("NEXT_RUNTIME is nodejs, loading instrumentation.node.ts");
    await import("./instrumentation.node");
    const { registerInitialCache } = await import(
      "@neshca/cache-handler/instrumentation"
    );
    const CacheHandler = (await import("../cache-handler.mjs")).default;
    await registerInitialCache(CacheHandler);
  } else {
    console.log("NEXT_RUNTIME is not nodejs");
  }
}
// import { registerOTel } from "@vercel/otel";
//
// export function register() {
//   registerOTel({ serviceName: process.env.NEXT_SEVICE_NAME });
// }
