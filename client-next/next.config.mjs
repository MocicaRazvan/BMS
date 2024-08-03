import createNextIntlPlugin from 'next-intl/plugin';

const withNextIntl = createNextIntlPlugin();

/** @type {import('next').NextConfig} */
const nextConfig = {
    output: 'standalone',
    images: {
        remotePatterns: [
            {
                hostname: "localhost",
            },{
                hostname: "im51.go.ro"
            },{
                 hostname:"avatars.githubusercontent.com"
            },{
             hostname:"lh3.googleusercontent.com"
        },{
            hostname:"gateway-service"
            }
        ],
    },

    // reactStrictMode: false
};
export default withNextIntl(nextConfig);
