# Base image for Maven build
FROM jelastic/maven:3.9.9-openjdk-22.0.2-almalinux-9 AS build
WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -q -B

COPY src ./src

RUN mvn package -DskipTests -Dmaven.compiler.source=22 -Dmaven.compiler.target=22 -Dmaven.compiler.compilerArgs=--enable-preview




# ---------------------- NEXTJS IMAGE ----------------------
FROM razvanmocica/next-js-bms:latest AS nextjs

# ---------------------- RUNTIME IMAGE ----------------------
FROM alpine/java:22.0.2-jre AS runtime
WORKDIR /app

RUN addgroup -S appgroup \
 && adduser -S -G appgroup \
      -h /home/appuser \
      -s /sbin/nologin \
      -D appuser

ENV HOME=/home/appuser

COPY --from=build /app/target/next-static-server-0.0.1-SNAPSHOT.jar .
COPY --from=nextjs /app/.next/static  ./data/static

RUN chown -R appuser:appgroup /app

USER appuser:appgroup

ENTRYPOINT ["java", "--enable-preview", "-jar", "next-static-server-0.0.1-SNAPSHOT.jar"]
