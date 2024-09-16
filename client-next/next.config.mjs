import createNextIntlPlugin from "next-intl/plugin";

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
    ],
  },
  // compiler: {
  //   removeConsole:
  //     process.env.NODE_ENV === "production" ? { exclude: ["error"] } : false,
  // },

  // reactStrictMode: false
};
export default withNextIntl(nextConfig);
