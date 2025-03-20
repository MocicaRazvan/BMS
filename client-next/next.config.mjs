import createNextIntlPlugin from "next-intl/plugin";

import generated from "@next/bundle-analyzer";

const withNextIntl = createNextIntlPlugin();
const withBundleAnalyzer = generated({
  enabled: process.env.ANALYZE === "true",
});

/** @type {import('next').NextConfig} */
const nextConfig = {
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
export default withBundleAnalyzer(withNextIntl(nextConfig));
