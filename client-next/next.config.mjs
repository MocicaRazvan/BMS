import createNextIntlPlugin from 'next-intl/plugin';

const withNextIntl = createNextIntlPlugin();

/** @type {import('next').NextConfig} */
const nextConfig = {
    output: 'standalone',
    images: {
        remotePatterns: [
            {
                hostname: "localhost",
            },
        ],
    },

    // reactStrictMode: false
};
export default withNextIntl(nextConfig);
