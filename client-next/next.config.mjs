import createNextIntlPlugin from "next-intl/plugin";
import { EventEmitter } from "events";
EventEmitter.defaultMaxListeners = 20;

const withNextIntl = createNextIntlPlugin();

/** @type {import('next').NextConfig} */
const nextConfig = {
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
export default withNextIntl(nextConfig);
