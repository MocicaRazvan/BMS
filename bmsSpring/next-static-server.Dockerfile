# syntax = devthefuture/dockerfile-x
ARG MODULE_NAME=next-static-server

INCLUDE ./base-docker/independent-deps.Dockerfile

INCLUDE ./base-docker/independent-build.Dockerfile
# ---------------------- NEXTJS IMAGE ----------------------
FROM razvanmocica/next-js-bms:latest AS nextjs

# ---------------------- RUNTIME IMAGE ----------------------
FROM alpine/java:22.0.2-jre AS runtime
WORKDIR /app

RUN apk add --no-cache \
      curl \
      netcat-openbsd \
 && addgroup -S appgroup \
 && adduser  -S -G appgroup \
      -h /home/appuser \
      -s /sbin/nologin \
      -D appuser


ENV HOME=/home/appuser

COPY --from=build \
     --chown=appuser:appgroup \
    /app/submodule/target/app.jar \
     app.jar

COPY --from=nextjs \
     --chown=appuser:appgroup \
     /app/.next/static \
     data/static

USER appuser:appgroup

ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]
