FROM python:3.10-slim AS builder

WORKDIR /app

RUN apt-get update && apt-get install -y \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

RUN pip install --upgrade pip==24.3.1

COPY requirements.txt .

RUN pip install  -r requirements.txt

FROM python:3.10-slim AS runtime

RUN apt-get update \
 && apt-get install -y curl netcat-openbsd \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system appgroup \
 && useradd  --system \
     --gid appgroup \
     --create-home \
     --home-dir /home/appuser \
     --shell /usr/sbin/nologin \
     appuser

ENV HOME=/home/appuser

WORKDIR /app

COPY --from=builder /usr/local/lib/python3.10/site-packages /usr/local/lib/python3.10/site-packages
COPY --from=builder /usr/local/bin/gunicorn /usr/local/bin/gunicorn
COPY --chown=appuser:appgroup ./app .

RUN mkdir -p /home/appuser/hf-cache \
 && chown -R appuser:appgroup /home/appuser/hf-cache

ENV HF_HOME=/home/appuser/hf-cache

ENV PYTHONUNBUFFERED=1
ENV PORT=5002
ENV WORKER_PROCESSES=1
ENV PROMETHEUS_MULTIPROC_DIR=/tmp

ENV OMP_NUM_THREADS=1
ENV MKL_NUM_THREADS=1
ENV TORCH_NUM_THREADS=1
ENV TORCH_INTEROP_THREADS=1


EXPOSE ${PORT}

VOLUME ["/home/appuser/hf-cache"]

CMD ["sh", "-c", "chown -R appuser:appgroup /home/appuser/hf-cache && exec su -s /bin/sh appuser -c 'exec gunicorn -c gunicorn_conf.py -w $WORKER_PROCESSES -b 0.0.0.0:$PORT --timeout 6000 app:app'"]