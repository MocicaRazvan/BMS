from typing import List

import torch
import transformers
from chronos import ChronosPipeline

from config import LOCAL_CACHE_DIR, CUDA_ENABLED, MODEL_ID
from logger import logger


def predict_series(context: List[List[float]], prediction_length: int) -> torch.Tensor:
    with torch.no_grad():
        transformers.set_seed(0)
        torch_context = torch.tensor(context)
        return get_pipline().predict_quantiles(torch_context, prediction_length, quantile_levels=[0.25, 0.5, 0.75])[0]


def get_pipline():
    device = "cuda" if torch.cuda.is_available() and CUDA_ENABLED else "cpu"
    logger.info(f"Device: {device}")
    logger.info(f"LOCAL_CACHE_DIR: {LOCAL_CACHE_DIR}")
    torch_dtype = torch.bfloat16 if torch.cuda.get_device_capability(0)[0] >= 8 else torch.float16
    logger.info(f"Using torch dtype: {torch_dtype}")
    pipeline = ChronosPipeline.from_pretrained(
        MODEL_ID,
        device_map=device,
        torch_dtype=torch_dtype,
        cache_dir=LOCAL_CACHE_DIR,
        force_download=False,
        trust_remote_code=True
    )
    return pipeline
