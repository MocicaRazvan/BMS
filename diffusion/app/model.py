import os
import torch
from diffusers import StableDiffusionPipeline, DDIMScheduler
from config import MODEL_ID, LOCAL_MODEL_PATH, DEVICE, RESERVED_VRAM_GB, MEMORY_FRACTION
import threading

pipeline_lock = threading.Lock()
tensor_lock = threading.Lock()
pipeline = None
reserved_tensor = None

if torch.cuda.is_available():
    torch.cuda.set_per_process_memory_fraction(MEMORY_FRACTION, device=0)


def reserve_vram(size_gb=RESERVED_VRAM_GB):
    """Reserve VRAM by allocating a large tensor."""
    global reserved_tensor
    with tensor_lock:
        num_elements = int((size_gb * 1024 ** 3) // 2)
        reserved_tensor = torch.empty((num_elements,), dtype=torch.float16, device=DEVICE)
        print(f"Reserved ~{size_gb}GB of VRAM.")


def release_vram():
    """Release the reserved VRAM by deleting the tensor."""
    global reserved_tensor
    with tensor_lock:
        if reserved_tensor is not None:
            del reserved_tensor
            reserved_tensor = None
            torch.cuda.empty_cache()
            print("Released reserved VRAM.")


def clear_cache():
    if torch.cuda.is_available():
        print("Clearing CUDA cache on GPU 0...")
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
        if os.path.exists(LOCAL_MODEL_PATH):
            print(f"Loading model from local path: {LOCAL_MODEL_PATH}")
            scheduler = DDIMScheduler.from_pretrained(
                LOCAL_MODEL_PATH, subfolder="scheduler", torch_dtype=torch.float16, safety_checker=None)
            pipe = StableDiffusionPipeline.from_pretrained(
                LOCAL_MODEL_PATH, torch_dtype=torch.float16, safety_checker=None, scheduler=scheduler
            )
        else:
            print(f"Downloading model: {MODEL_ID}")
            pipe = StableDiffusionPipeline.from_pretrained(
                MODEL_ID, torch_dtype=torch.float16
            )
            print(f"Saving model locally to: {LOCAL_MODEL_PATH}")
            pipe.save_pretrained(LOCAL_MODEL_PATH)
            scheduler = DDIMScheduler.from_pretrained(
                LOCAL_MODEL_PATH, subfolder="scheduler", torch_dtype=torch.float16, safety_checker=None)
            pipe = StableDiffusionPipeline.from_pretrained(
                LOCAL_MODEL_PATH, torch_dtype=torch.float16, safety_checker=None, scheduler=scheduler
            )

        pipe = pipe.to(DEVICE)
        pipe.enable_attention_slicing("auto")
        pipe.unet.enable_gradient_checkpointing()
        #     pipe.enable_model_cpu_offload()
        pipe.safety_checker = None
        #     pipe.feature_extractor = None
        pipeline = pipe
        return pipeline


def init_model():
    if pipeline is None:
        print("Loading model pipeline...")
        get_pipeline()
    if reserved_tensor is None:
        reserve_vram()


init_model()
