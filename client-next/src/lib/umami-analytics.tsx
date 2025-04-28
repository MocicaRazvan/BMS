import Script from "next/script";
import { emitInfo } from "@/logger";

export const UmamiAnalytics = () => {
  const websiteId = process.env.UMAMI_WEBSITE_ID;
  const src = process.env.UMAMI_URL;
  const dataTag = process.env.UMAMI_DATA_TAG ?? "homepage-layout";
  if (!websiteId || !src) {
    emitInfo("UmamiAnalytics is not configured");
    return <></>;
  }
  emitInfo(
    `UmamiAnalytics is starting with config: src: ${src}, websiteId: ${websiteId}, dataTag: ${dataTag}`,
  );
  return (
    <Script async src={src} data-website-id={websiteId} data-tag={dataTag} />
  );
};
