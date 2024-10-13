#FROM node:alpine AS base
FROM node:20-alpine AS base

# Install dependencies only when needed
FROM base AS deps
# Check https://github.com/nodejs/docker-node/tree/b4117f9333da4138b03a546ec926ef50a31506c3#nodealpine to understand why libc6-compat might be needed.
RUN apk add --no-cache libc6-compat
WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm install


# Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Next.js collects completely anonymous telemetry data about general usage.
# Learn more here: https://nextjs.org/telemetry
# Uncomment the following line in case you want to disable telemetry during the build.
# ENV NEXT_TELEMETRY_DISABLED 1

# Pass arguments to the build
ARG NEXT_PUBLIC_SPRING
ARG NEXT_PUBLIC_SPRING_CLIENT
ARG NEXTAUTH_URL
ARG NEXTAUTH_URL_INTERNAL
ARG NEXTAUTH_SECRET
ARG GITHUB_CLIENT_SECRET
ARG GITHUB_CLIENT_ID
ARG GOOGLE_CLIENT_ID
ARG GOOGLE_CLIENT_SECRET
ARG NODE_TLS_REJECT_UNAUTHORIZED
ARG OLLAMA_MODEL
ARG OLLAMA_BASE_URL
ARG OLLAMA_EMBEDDING
ARG POSTGRES_HOST
ARG POSTGRES_PORT
ARG POSTGRES_USER
ARG POSTGRES_PASSWORD
ARG POSTGRES_DB
ARG NEXT_PUBLIC_SPRING_CLIENT_WEBSOCKET
ARG NEXT_SEVICE_NAME
ARG NEXT_ZIPKIN_URL
ARG NEXT_OTEL_VERBOSE
ARG NEXT_TRACING_PROBABILITY
# Set environment variables
ENV NEXT_PUBLIC_SPRING=${NEXT_PUBLIC_SPRING}
ENV NEXT_PUBLIC_SPRING_CLIENT=${NEXT_PUBLIC_SPRING_CLIENT}
ENV NEXTAUTH_URL=${NEXTAUTH_URL}
ENV NEXTAUTH_URL_INTERNAL=${NEXTAUTH_URL_INTERNAL}
ENV NEXTAUTH_SECRET=${NEXTAUTH_SECRET}
ENV GITHUB_CLIENT_SECRET=${GITHUB_CLIENT_SECRET}
ENV GITHUB_CLIENT_ID=${GITHUB_CLIENT_ID}
ENV GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
ENV GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
ENV NODE_TLS_REJECT_UNAUTHORIZED=${NODE_TLS_REJECT_UNAUTHORIZED}
ENV OLLAMA_MODEL=${OLLAMA_MODEL}
ENV OLLAMA_BASE_URL=${OLLAMA_BASE_URL}
ENV OLLAMA_EMBEDDING=${OLLAMA_EMBEDDING}
ENV POSTGRES_HOST=${POSTGRES_HOST}
ENV POSTGRES_PORT=${POSTGRES_PORT}
ENV POSTGRES_USER=${POSTGRES_USER}
ENV POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
ENV POSTGRES_DB=${POSTGRES_DB}
ENV NEXT_PUBLIC_SPRING_CLIENT_WEBSOCKET=${NEXT_PUBLIC_SPRING_CLIENT_WEBSOCKET}
ENV OLLAMA_TEMPERATURE=${OLLAMA_TEMPERATURE}
ENV OLLAMA_CHAT_COUNT=${OLLAMA_CHAT_COUNT}
ENV NEXT_SEVICE_NAME=${NEXT_SEVICE_NAME}
ENV NEXT_ZIPKIN_URL=${NEXT_ZIPKIN_URL}
ENV NEXT_OTEL_VERBOSE=${NEXT_OTEL_VERBOSE}
ENV NEXT_TRACING_PROBABILITY=${NEXT_TRACING_PROBABILITY}


RUN npm run build

# Production image, copy all the files and run next
FROM base AS runner
WORKDIR /app

ENV NODE_ENV production
# Uncomment the following line in case you want to disable telemetry during runtime.
# ENV NEXT_TELEMETRY_DISABLED 1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public
# FOR LANGCHAIN DOC LOADER
COPY --from=builder /app/src ./src
COPY --from=builder /app/scrape ./scrape


# Set the correct permission for prerender cache
RUN mkdir .next
RUN chown nextjs:nodejs .next

# Automatically leverage output traces to reduce image size
# https://nextjs.org/docs/advanced-features/output-file-tracing
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

#EXPOSE 3000

ENV PORT 3000 9464

# server.js is created by next build from the standalone output
# https://nextjs.org/docs/pages/api-reference/next-config-js/output
#CMD node server.js
CMD ["node", "server.js"]
