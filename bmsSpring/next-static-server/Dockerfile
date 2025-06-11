# Base image for Maven build
FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9 AS build
WORKDIR /app

COPY /bmsSpring/next-static-server/pom.xml .

RUN mvn dependency:go-offline -B

COPY /bmsSpring/next-static-server/src ./src

RUN mvn package -DskipTests -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview



# Build Nextjs
FROM node:20-alpine AS nextjsbase


FROM nextjsbase AS nextjsdeps
RUN apk add --no-cache libc6-compat
WORKDIR /app


COPY client-next/package*.json ./
RUN npm install



FROM nextjsbase AS nextjsbuilder
WORKDIR /app
COPY --from=nextjsdeps /app/node_modules ./node_modules
COPY client-next/ .

ENV NEXT_TELEMETRY_DISABLED=1


ARG NEXT_PUBLIC_SPRING
ARG NEXT_PUBLIC_SPRING_CLIENT
ARG NEXTAUTH_URL
ARG NEXTAUTH_URL_INTERNAL
ARG NEXT_PUBLIC_NEXTAUTH_URL
ARG OLLAMA_MODEL
ARG OLLAMA_BASE_URL
ARG OLLAMA_EMBEDDING
ARG POSTGRES_HOST
ARG POSTGRES_PORT
ARG ASSETS_PREFIX_URL
ARG CACHE_HANDLER_REDIS_URL
ARG NEXT_PUBLIC_SPRING_CLIENT_WEBSOCKET
ARG NEXT_ZIPKIN_URL

ENV NEXT_PUBLIC_SPRING=${NEXT_PUBLIC_SPRING}
ENV NEXT_PUBLIC_SPRING_CLIENT=${NEXT_PUBLIC_SPRING_CLIENT}
ENV NEXTAUTH_URL=${NEXTAUTH_URL}
ENV NEXTAUTH_URL_INTERNAL=${NEXTAUTH_URL_INTERNAL}
ENV OLLAMA_MODEL=${OLLAMA_MODEL}
ENV OLLAMA_BASE_URL=${OLLAMA_BASE_URL}
ENV OLLAMA_EMBEDDING=${OLLAMA_EMBEDDING}
ENV POSTGRES_HOST=${POSTGRES_HOST}
ENV POSTGRES_PORT=${POSTGRES_PORT}
ENV NEXT_PUBLIC_SPRING_CLIENT_WEBSOCKET=${NEXT_PUBLIC_SPRING_CLIENT_WEBSOCKET}
ENV NEXT_ZIPKIN_URL=${NEXT_ZIPKIN_URL}
ENV NEXT_PUBLIC_NEXTAUTH_URL=${NEXT_PUBLIC_NEXTAUTH_URL}
ENV ASSETS_PREFIX_URL=${ASSETS_PREFIX_URL}
ENV CACHE_HANDLER_REDIS_URL=${CACHE_HANDLER_REDIS_URL}

RUN npm run ci:build

# ---------------------- RUNTIME IMAGE ----------------------
FROM alpine/java:22.0.2-jre AS runtime
WORKDIR /app

RUN apk add --no-cache curl netcat-openbsd


COPY --from=build /app/target/next-static-server-0.0.1-SNAPSHOT.jar .
COPY --from=nextjsbuilder /app/.next/static  ./data/static


ENTRYPOINT ["java", "--enable-preview", "-jar", "next-static-server-0.0.1-SNAPSHOT.jar"]
