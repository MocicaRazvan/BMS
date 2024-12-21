import os

# app settings
APP_NAME = os.getenv("DIFFUSION_APP_NAME", "diffusion_service")
APP_VERSION = os.getenv("DIFFUSION_APP_VERSION", "1.0.0")
FLASK_DEBUG = os.getenv("FLASK_DEBUG", "False").lower() == "true"
LOKI_URL = os.getenv("LOKI_URL", "")
MAX_IMAGE_THREADS = int(os.getenv("MAX_IMAGE_THREADS", 4))

# model settings
MODEL_ID = os.getenv("DIFFUSION_MODEL_ID", "CompVis/stable-diffusion-v1-4")
LOCAL_MODEL_PATH = (
    os.path.join(os.getenv("DIFFUSION_MODEL_PATH", ""), "./models")
    if "DIFFUSION_MODEL_PATH" in os.environ
    else "./models/stable_diffusion_v1_4"
)

# cuda settings
DEVICE = "cuda:0"
RESERVED_VRAM_GB = float(os.getenv("RESERVED_VRAM_GB", 1.5))
MEMORY_FRACTION = float(os.getenv("MEMORY_FRACTION", 0.5))
RESERVED_TENSOR = os.getenv("RESERVED_TENSOR", "True").lower() == "true"
PIPE_ENABLE_GRADIENT_CHECKPOINTING = os.getenv("PIPE_ENABLE_GRADIENT_CHECKPOINTING", "True").lower() == "true"
PIPE_DISABLE_SAFETY_CHECKER = os.getenv("PIPE_DISABLE_SAFETY_CHECKER", "True").lower() == "true"

# zipkin settings
ZIPKIN_SAMPLE_RATE = float(os.getenv("ZIPKIN_SAMPLE_RATE", 0.1))
ZIPKIN_URL = os.getenv("ZIPKIN_URL", "")
