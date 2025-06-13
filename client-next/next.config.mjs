import createNextIntlPlugin from "next-intl/plugin";
import CompressionPlugin from "compression-webpack-plugin";
import { constants } from "node:zlib";
import path from "path";
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
    minimumCacheTTL: 2678400,
    contentDispositionType: "inline",
    contentSecurityPolicy: "default-src 'self'; script-src 'none'; sandbox;",
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
  cacheHandler:
    process.env.NODE_ENV === "production" ? "./cache-handler.mjs" : undefined,
  cacheMaxMemorySize: process.env.NODE_ENV === "production" ? 0 : undefined,
  experimental: {
    instrumentationHook: true,
    serverComponentsExternalPackages: [
      // "@opentelemetry/instrumentation",
      // "langchain",
      // "@langchain/core",
    ],
  },
  webpack(config, { dev, isServer }) {
    if (!dev && !isServer) {
      console.log("Adding compression plugins to webpack config");
      config.plugins.push(
        new CompressionPlugin({
          filename: "[path][base].gz",
          algorithm: "gzip",
          test: /\.(js|css|html|svg|json)$/,
          threshold: 10240,
          minRatio: 0.8,
          deleteOriginalAssets: false,
          compressionOptions: {
            level: 9,
          },
        }),
        new CompressionPlugin({
          filename: "[path][base].br",
          algorithm: "brotliCompress",
          test: /\.(js|css|html|svg|json)$/,
          compressionOptions: {
            params: {
              [constants.BROTLI_PARAM_QUALITY]: 11,
            },
          },
          threshold: 10240,
          minRatio: 0.8,
          deleteOriginalAssets: false,
        }),
      );
    }
    config.module.rules.push({
      test: /geoData\.json$/i,
      resourceQuery: /url/,
      include: [path.resolve(process.cwd(), "src/assets/data/geoData.json")],
      type: "asset/resource",
      generator: {
        filename: "static/customdata/[name]-[contenthash][ext]",
      },
    });
    return config;
  },
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
  const enableCompression =
    isDev ||
    String(process.env.ENABLE_COMPRESSION).trim().toLowerCase() === "true";
  /**
   * @type {import('next').NextConfig}
   */
  const nextConfig = {
    ...baseConfig,
    assetPrefix,
    compress: enableCompression,
    ...(!isDev
      ? {
          headers: async () => {
            return [
              {
                source: "/:path*{/}?",
                headers: [
                  {
                    key: "X-Accel-Buffering",
                    value: "no",
                  },
                  {
                    key: "X-Content-Type-Options",
                    value: "nosniff",
                  },
                ],
              },
            ];
          },
        }
      : {}),
  };
  /**
   * @type {import('next').NextConfig}
   */
  return withBundleAnalyzer(withNextIntl(nextConfig));
};

export default finalConfig;
