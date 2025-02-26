import os
import threading

import torch
from diffusers import StableDiffusionPipeline, DPMSolverMultistepScheduler
from xformers.ops import MemoryEfficientAttentionFlashAttentionOp

from app_config import MODEL_ID, LOCAL_MODEL_PATH, DEVICE, RESERVED_VRAM_GB, MEMORY_FRACTION, RESERVED_TENSOR, \
    PIPE_DISABLE_SAFETY_CHECKER, PIPE_ENABLE_GRADIENT_CHECKPOINTING
from logger import logger

pipeline_lock = threading.Lock()
tensor_lock = threading.Lock()
pipeline = None
reserved_tensor = None

if torch.cuda.is_available():
    torch.cuda.set_per_process_memory_fraction(MEMORY_FRACTION, device=0)


def reserve_vram(size_gb=RESERVED_VRAM_GB):
    """Reserve VRAM by allocating a large tensor."""
    if not RESERVED_TENSOR:
        logger.info("No tensor is reserved")
        return
    global reserved_tensor
    if reserved_tensor is not None:
        logger.info("VRAM already reserved.")
        return
    with tensor_lock:
        num_elements = int((size_gb * 1024 ** 3) // 2)
        reserved_tensor = torch.empty((num_elements,), dtype=torch.float16, device=DEVICE)
        logger.info(f"Reserved ~{size_gb}GB of VRAM.")


def release_vram():
    """Release the reserved VRAM by deleting the tensor."""
    if not RESERVED_TENSOR:
        logger.info("No tensor was reserved")
        return
    global reserved_tensor
    with tensor_lock:
        if reserved_tensor is not None:
            del reserved_tensor
            reserved_tensor = None
            torch.cuda.empty_cache()
            logger.info("Released reserved VRAM.")


def clear_cache():
    if torch.cuda.is_available():
        logger.info("Clearing CUDA cache on GPU 0...")
        torch.cuda.set_device(0)
        torch.cuda.empty_cache()


def get_pipeline() -> StableDiffusionPipeline:
    """
    Load the Stable Diffusion model pipeline.
    If not available locally, download and save it.
    """
    global pipeline

    if pipeline is not None:
        return pipeline

    with pipeline_lock:
        if pipeline is not None:
            return pipeline

        safety_checker = None if PIPE_DISABLE_SAFETY_CHECKER else True
        torch_dtype = torch.bfloat16 if torch.cuda.get_device_capability(0)[0] >= 8 else torch.float16

        logger.info(f"Using torch dtype: {torch_dtype}")
        if os.path.exists(LOCAL_MODEL_PATH):
            logger.info(f"Loading model from local path: {LOCAL_MODEL_PATH}")
            pipe = load_from_local(safety_checker, torch_dtype)
        else:
            logger.info(f"Downloading model: {MODEL_ID}")
            pipe = StableDiffusionPipeline.from_pretrained(
                MODEL_ID, torch_dtype=torch_dtype
            )
            logger.info(f"Saving model locally to: {LOCAL_MODEL_PATH}")
            pipe.save_pretrained(LOCAL_MODEL_PATH)
            pipe = load_from_local(safety_checker)

        pipe = pipe.to(DEVICE)
        pipe.enable_attention_slicing("auto")
        pipe.enable_xformers_memory_efficient_attention(attention_op=MemoryEfficientAttentionFlashAttentionOp)
        pipe.vae.enable_xformers_memory_efficient_attention(attention_op=None)
        pipe.unet.to(dtype=torch_dtype)
        pipe.vae.to(dtype=torch_dtype)

        if PIPE_ENABLE_GRADIENT_CHECKPOINTING:
            pipe.unet.enable_gradient_checkpointing()
            logger.info("Gradient checkpointing enabled.")

        if safety_checker is None:
            pipe.safety_checker = None
            logger.info("Safety checker disabled.")

        pipeline = pipe
        return pipeline


def load_from_local(safety_checker, torch_dtype):
    if safety_checker is None:
        scheduler = DPMSolverMultistepScheduler.from_pretrained(
            LOCAL_MODEL_PATH, subfolder="scheduler", torch_dtype=torch_dtype, safety_checker=None)
        pipe = StableDiffusionPipeline.from_pretrained(
            LOCAL_MODEL_PATH, torch_dtype=torch_dtype, safety_checker=None, scheduler=scheduler
        )
        logger.info("Safety checker disabled for local model.")
    else:
        scheduler = DPMSolverMultistepScheduler.from_pretrained(
            LOCAL_MODEL_PATH, subfolder="scheduler", torch_dtype=torch_dtype)
        pipe = StableDiffusionPipeline.from_pretrained(
            LOCAL_MODEL_PATH, torch_dtype=torch_dtype, scheduler=scheduler
        )
        logger.info("Safety checker enabled for local model.")
    return pipe


def init_model():
    if pipeline is None:
        logger.info("Loading model pipeline...")
        get_pipeline()
    if reserved_tensor is None:
        reserve_vram()


init_model()
