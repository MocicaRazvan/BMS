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

USER appuser:appgroup
