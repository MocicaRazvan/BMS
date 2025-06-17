# syntax = devthefuture/dockerfile-x
ARG MODULE_NAME=archive-service

INCLUDE ./base-docker/independent-deps.Dockerfile

INCLUDE ./base-docker/independent-build.Dockerfile

# ---------------------- RUNTIME IMAGE ----------------------
FROM alpine/java:22.0.2-jre AS runtime
WORKDIR /app

RUN apk add --no-cache \
      curl \
      netcat-openbsd \
 && addgroup -g 1000 -S appgroup \
 && adduser  -u 1000 -S -G appgroup \
      -h /home/appuser -s /sbin/nologin -D appuser \
 && mkdir -p archive/data \
 && chmod 770 archive/data \
 && chmod g+s archive/data

ENV HOME=/home/appuser
VOLUME ["/app/archive/data"]

COPY --from=build \
     --chown=appuser:appgroup \
     /app/submodule/target/app.jar \
     app.jar

USER appuser:appgroup

ENTRYPOINT ["java","--enable-preview","-Dreactor.schedulers.defaultBoundedElasticOnVirtualThreads=true","-jar", "app.jar"]