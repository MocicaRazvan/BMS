# syntax = docker/dockerfile:1.3
ARG NEXTJS_IMAGE_TAG=latest

# Base image for Maven build
FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9 AS build
WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -q -B

COPY src ./src

RUN mvn package -DskipTests -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview




# ---------------------- NEXTJS IMAGE ----------------------
FROM --pull razvanmocica/next-js-bms:${NEXTJS_IMAGE_TAG} AS nextjs

# ---------------------- RUNTIME IMAGE ----------------------
FROM alpine/java:22.0.2-jre AS runtime
WORKDIR /app

RUN apk add --no-cache curl netcat-openbsd


COPY --from=build /app/target/next-static-server-0.0.1-SNAPSHOT.jar .
COPY --from=nextjs /app/.next/static  ./data/static


ENTRYPOINT ["java", "--enable-preview", "-jar", "next-static-server-0.0.1-SNAPSHOT.jar"]
