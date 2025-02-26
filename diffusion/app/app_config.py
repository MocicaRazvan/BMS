import os
from config import ConfigClient

SPRING_CLOUD_CONFIG_URI_BY_PROFILE = {
    "default": "http://localhost:8888",
    "dev": "http://localhost:8888",
    "server": "http://config-server:8888",
    "k8s": "http://config-server:8888",
    "docker": "http://host.docker.internal:8888",
}
APP_NAME = "diffusion-service"
PYTHON_PROFILES_ACTIVE = os.getenv("PYTHON_PROFILES_ACTIVE", "default")
SPRING_CLOUD_CONFIG_URI = os.getenv("SPRING_CLOUD_CONFIG_URI", "http://localhost:8888")
cc = ConfigClient(
    app_name=APP_NAME,
    profile=PYTHON_PROFILES_ACTIVE,
    label="main",
    address=SPRING_CLOUD_CONFIG_URI_BY_PROFILE.get(PYTHON_PROFILES_ACTIVE, SPRING_CLOUD_CONFIG_URI)
)
cc.get_config()

# app settings
APP_VERSION = cc.get("app.version")
FLASK_DEBUG = cc.get("flask.debug", "False").lower() == "true"
LOKI_URL = cc.get("loki.url", "")
MAX_IMAGE_THREADS = int(cc.get("max.image.threads", 4))

# model settings
MODEL_ID = cc.get("model.id")
ENV_LOCAL_MODEL_PATH = (
    os.path.join(os.getenv("DIFFUSION_MODEL_PATH", ""), "./models")
    if "DIFFUSION_MODEL_PATH" in os.environ
    else "./models/stable_diffusion_v1_4"
)
LOCAL_MODEL_PATH = cc.get("local.model.path", ENV_LOCAL_MODEL_PATH)

# cuda settings
DEVICE = cc.get("device", "cuda:0")
RESERVED_VRAM_GB = float(cc.get("reserved.vram.gb", 1.5))
MEMORY_FRACTION = float(cc.get("reserved.memory.fraction", 0.5))
RESERVED_TENSOR = cc.get("reserved.tensor", "False").lower() == "true"
PIPE_ENABLE_GRADIENT_CHECKPOINTING = cc.get("pipe.enable.gradient.checkpointing", "False").lower() == "true"
PIPE_DISABLE_SAFETY_CHECKER = cc.get("pipe.disable.safety.checker", "True").lower() == "true"

# zipkin settings
ZIPKIN_SAMPLE_RATE = float(cc.get("zipkin.sample.rate", "0.1"))
ZIPKIN_URL = cc.get("zipkin.url", "http://localhost:9411/api/v2/spans")
