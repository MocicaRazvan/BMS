import glob
import os
from typing import List

import torch
import transformers
from chronos import ChronosPipeline

from app_config import LOCAL_CACHE_DIR, CUDA_ENABLED, MODEL_ID
from logger import logger

logger.info(f"MODEL_ID {MODEL_ID}")

dir_path = os.path.join(LOCAL_CACHE_DIR, f"models--{MODEL_ID.replace('/', '--')}", "snapshots", "*")


def get_latest_snapshot():
    snapshot_dirs = glob.glob(dir_path)
    return max(snapshot_dirs, key=os.path.getmtime) if snapshot_dirs else None


snapshot_dir = get_latest_snapshot()


def predict_series(context: List[List[float]], prediction_length: int) -> torch.Tensor:
    with torch.no_grad():
        transformers.set_seed(0)
        torch_context = torch.tensor(context)
        return get_pipeline().predict_quantiles(torch_context, prediction_length, quantile_levels=[0.25, 0.5, 0.75])[0]


def get_pipeline():
    global snapshot_dir
    device = "cuda" if torch.cuda.is_available() and CUDA_ENABLED else "cpu"
    logger.info(f"Device: {device}")
    try:
        torch_dtype = torch.bfloat16 if torch.cuda.get_device_capability(0)[0] >= 8 else torch.float16
    except:
        torch_dtype = torch.float16
    logger.info(f"Using torch dtype: {torch_dtype}")
    if snapshot_dir:
        logger.info("Using downloaded model")
        pipeline = ChronosPipeline.from_pretrained(
            snapshot_dir,
            device_map=device,
            torch_dtype=torch_dtype,
            force_download=False,
            trust_remote_code=True,
            local_files_only=True
        )
    else:
        logger.info("Downloading model")
        logger.info(f"LOCAL_CACHE_DIR: {LOCAL_CACHE_DIR}")
        pipeline = ChronosPipeline.from_pretrained(
            MODEL_ID,
            device_map=device,
            torch_dtype=torch_dtype,
            cache_dir=LOCAL_CACHE_DIR,
            force_download=False,
            trust_remote_code=True
        )
        snapshot_dir = get_latest_snapshot()
    return pipeline
