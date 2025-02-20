import os

APP_NAME = os.getenv("TIMESERIES_APP_NAME", "timeseries_chronos")
APP_VERSION = os.getenv("TIMESERIES_APP_VERSION", "1.0.0")
FLASK_DEBUG = os.getenv("FLASK_DEBUG", "False").lower() == "true"
LOKI_URL = os.getenv("LOKI_URL", "")
MODEL_ID = os.getenv("TIMESERIES_MODEL_ID", "amazon/chronos-t5-base")
LOCAL_CACHE_DIR = os.path.join(os.curdir, "cache_dir")

# cuda settings
CUDA_ENABLED = os.getenv("CUDA_ENABLED", "True").lower() == "true"

# zipkin settings
ZIPKIN_SAMPLE_RATE = float(os.getenv("ZIPKIN_SAMPLE_RATE", 0.1))
ZIPKIN_URL = os.getenv("ZIPKIN_URL", "")

# redis
CACHE_REDIS_HOST = os.getenv("CACHE_REDIS_HOST", "localhost")
CACHE_REDIS_PORT = int(os.getenv("CACHE_REDIS_PORT", 6379))
CACHE_REDIS_DB = int(os.getenv("CACHE_REDIS_DB", 6))
CACHE_REDIS_PASSWORD = os.getenv("CACHE_REDIS_PASSWORD", "")
CACHE_REDIS_EXPIRATION = int(os.getenv("CACHE_REDIS_EXPIRATION", 60 * 60))
