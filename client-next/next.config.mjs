import createNextIntlPlugin from "next-intl/plugin";
import { PHASE_DEVELOPMENT_SERVER } from "next/constants.js";

import generated from "@next/bundle-analyzer";

const withNextIntl = createNextIntlPlugin();
const withBundleAnalyzer = generated({
  enabled: process.env.ANALYZE === "true",
});

/** @type {import('next').NextConfig} */
const baseConfig = {
  // reactStrictMode: false,
  output: "standalone",
  images: {
    remotePatterns: [
      {
        hostname: "localhost",
      },
      {
        hostname: "im51.go.ro",
      },
      {
        hostname: "avatars.githubusercontent.com",
      },
      {
        hostname: "lh3.googleusercontent.com",
      },
      {
        hostname: "gateway-service",
      },
      {
        hostname: "nginx-gateway",
      },
      {
        hostname: "images.unsplash.com",
      },
    ],
  },
  compiler: {
    removeConsole:
      process.env.NODE_ENV === "production" ? { exclude: ["error"] } : false,
  },
  experimental: {
    instrumentationHook: true,
    serverComponentsExternalPackages: [
      // "@opentelemetry/instrumentation",
      // "langchain",
      // "@langchain/core",
    ],
  },

  // reactStrictMode: false
};
const finalConfig = (phase) => {
  const isDev = phase === PHASE_DEVELOPMENT_SERVER;
  const envPrefix = process.env.ASSETS_PREFIX_URL;
  const assetPrefix = isDev
    ? undefined
    : !envPrefix
      ? "https://im51.go.ro"
      : envPrefix === "local"
        ? undefined
        : envPrefix;
  /**
   * @type {import('next').NextConfig}
   */
  const nextConfig = {
    ...baseConfig,
    assetPrefix,
  };
  /**
   * @type {import('next').NextConfig}
   */
  return withBundleAnalyzer(withNextIntl(nextConfig));
};

export default finalConfig;
