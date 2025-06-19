FROM python:3.10-slim AS system-builder

RUN apt-get update && apt-get install -y \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

RUN pip install --upgrade pip==24.3.1  --root-user-action=ignore  --no-cache-dir -q

FROM system-builder AS builder

ARG MODULE_NAME

WORKDIR /app

COPY ${MODULE_NAME}/requirements.txt .

RUN pip install  -r requirements.txt --root-user-action=ignore --no-cache-dir -q