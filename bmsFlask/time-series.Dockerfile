# syntax = devthefuture/dockerfile-x
ARG MODULE_NAME=timeSeriesChronos
INCLUDE ./base-docker/build.Dockerfile

FROM python:3.10-slim AS runtime

ARG MODULE_NAME

RUN apt-get update \
 && apt-get install -y curl netcat-openbsd \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system appgroup \
 && useradd  --system \
     --gid appgroup \
     --create-home \
     --home-dir /home/appuser \
     --shell /usr/sbin/nologin \
     appuser \
    && mkdir -p /home/appuser/hf-cache /home/appuser/model-cache \
        && chown -R appuser:appgroup /home/appuser/hf-cache \
           && chown -R appuser:appgroup /home/appuser/model-cache

ENV HOME=/home/appuser

WORKDIR /app

COPY --from=builder /usr/local/lib/python3.10/site-packages /usr/local/lib/python3.10/site-packages
COPY --from=builder /usr/local/bin/gunicorn /usr/local/bin/gunicorn
COPY --chown=appuser:appgroup ${MODULE_NAME}/app .

RUN mkdir -p /home/appuser/hf-cache /home/appuser/model-cache \
 && chown -R appuser:appgroup /home/appuser/hf-cache \
    && chown -R appuser:appgroup /home/appuser/model-cache


ENV HF_HOME=/home/appuser/hf-cache


ENV PYTHONUNBUFFERED=1
ENV PORT=5001
ENV WORKER_PROCESSES=1
ENV PYTORCH_CUDA_ALLOC_CONF=expandable_segments:True
ENV PROMETHEUS_MULTIPROC_DIR=/tmp


EXPOSE ${PORT}

VOLUME ["/home/appuser/hf-cache", "/home/appuser/model-cache"]

ENTRYPOINT ["sh", "-c", "chown -R appuser:appgroup /home/appuser/hf-cache /home/appuser/model-cache && exec su -s /bin/sh appuser -c 'exec gunicorn -c gunicorn_conf.py -w $WORKER_PROCESSES -b 0.0.0.0:$PORT --timeout 6000 app:app'"]
