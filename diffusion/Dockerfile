FROM python:3.10-slim AS builder

WORKDIR /app


COPY requirements.txt .

RUN apt-get update && apt-get install -y \
    build-essential \
    && rm -rf /var/lib/apt/lists/*


RUN pip install --upgrade pip==24.3.1
RUN pip install  -r requirements.txt

FROM python:3.10-slim AS runtime

WORKDIR /app

COPY --from=builder /usr/local/lib/python3.10/site-packages /usr/local/lib/python3.10/site-packages
COPY --from=builder /usr/local/bin/gunicorn /usr/local/bin/gunicorn
COPY ./app .


ENV PYTHONUNBUFFERED=1
ENV PORT=5000
ENV WORKER_PROCESSES=1
ENV PYTORCH_CUDA_ALLOC_CONF=expandable_segments:True
ENV PROMETHEUS_MULTIPROC_DIR=/tmp
ENV prometheus_multiproc_dir=/tmp

EXPOSE ${PORT}

CMD ["sh", "-c", "gunicorn -c gunicorn_conf.py -w $WORKER_PROCESSES -b 0.0.0.0:$PORT --timeout 6000 app:app"]
