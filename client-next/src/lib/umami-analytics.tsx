import Script from "next/script";

export const UmamiAnalytics = () => {
  const websiteId = process.env.UMAMI_WEBSITE_ID;
  const src = process.env.UMAMI_URL;
  const dataTag = process.env.UMAMI_DATA_TAG ?? "homepage-layout";
  return (
    <Script async src={src} data-website-id={websiteId} data-tag={dataTag} />
  );
};
