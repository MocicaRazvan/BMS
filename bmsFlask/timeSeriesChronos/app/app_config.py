import os

from config import ConfigClient

SPRING_CLOUD_CONFIG_URI_BY_PROFILE = {
        "default": "http://localhost:8888",
        "dev": "http://localhost:8888",
        "server": "http://config-server:8888",
        "k8s": "http://config-server:8888",
        "docker": "http://host.docker.internal:8888",
}
APP_NAME = "time-series-service"
PYTHON_PROFILES_ACTIVE = os.getenv("PYTHON_PROFILES_ACTIVE", "default")
SPRING_CLOUD_CONFIG_URI = os.getenv("SPRING_CLOUD_CONFIG_URI", "http://localhost:8888")
cc = ConfigClient(
        app_name=APP_NAME,
        profile=PYTHON_PROFILES_ACTIVE,
        label="main",
        address=SPRING_CLOUD_CONFIG_URI_BY_PROFILE.get(PYTHON_PROFILES_ACTIVE, SPRING_CLOUD_CONFIG_URI)
)
cc.get_config()

APP_VERSION = cc.get("app.version")
FLASK_DEBUG = cc.get("flask.debug", "False").lower() == "true"
LOKI_URL = cc.get("loki.url", "")
MODEL_ID = cc.get("model.id")
LOCAL_CACHE_DIR = "/home/appuser/model-cache"

# cuda settings
CUDA_ENABLED = os.getenv("CUDA_ENABLED",cc.get("cuda.enabled", "True")).lower() == "true"

# zipkin settings
ZIPKIN_SAMPLE_RATE = float(cc.get("zipkin.sample.rate", "0.1"))
ZIPKIN_URL = cc.get("zipkin.url", "http://localhost:9411/api/v2/spans")

cache_redis = cc.get("cache.redis")
# redis
CACHE_REDIS_HOST = cache_redis.get("host", "localhost")
CACHE_REDIS_PORT = int(cache_redis.get("port", 6379))
CACHE_REDIS_DB = int(cache_redis.get("db", 0))
CACHE_REDIS_PASSWORD = cache_redis.get("password", "")
CACHE_REDIS_EXPIRATION = int(cache_redis.get("expiration", 60 * 60))
