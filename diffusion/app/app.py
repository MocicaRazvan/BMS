import io
import os
import traceback
import zipfile
from concurrent.futures import ThreadPoolExecutor

import torch
from diffusers import StableDiffusionPipeline, DDIMScheduler
from flask import Flask, request, send_file, jsonify
from prometheus_flask_exporter import PrometheusMetrics

pipeline = None
reserved_tensor = None

MODEL_ID = os.getenv("DIFFUSION_MODEL_ID", "CompVis/stable-diffusion-v-1-4")
LOCAL_MODEL_PATH = (
    os.path.join(os.getenv("DIFFUSION_MODEL_PATH", ""), "./models")
    if "DIFFUSION_MODEL_PATH" in os.environ
   else "./models/stable_diffusion_v1_4"
)

DEVICE = "cuda:0"
RESERVED_VRAM_GB = float(os.getenv("RESERVED_VRAM_GB", 1.5))
MEMORY_FRACTION = float(os.getenv("MEMORY_FRACTION", 0.5))

torch.cuda.set_per_process_memory_fraction(MEMORY_FRACTION, device=0)

def reserve_vram(size_gb=RESERVED_VRAM_GB):
    """
    Reserve VRAM by allocating a large tensor.
    """
    global reserved_tensor
    num_elements = int((size_gb * 1024**3) // 2)
    reserved_tensor = torch.empty((num_elements,), dtype=torch.float16, device=DEVICE)
    print(f"Reserved ~{size_gb}GB of VRAM.")

def release_vram():
    """
    Release the reserved VRAM by deleting the tensor.
    """
    global reserved_tensor
    if reserved_tensor is not None:
        del reserved_tensor
        reserved_tensor = None
        torch.cuda.empty_cache()
        print("Released reserved VRAM.")



def get_pipeline() -> StableDiffusionPipeline:
    """
    Load the Stable Diffusion model pipeline.
    If not available locally, download and save it.
    """
    if os.path.exists(LOCAL_MODEL_PATH):
        print(f"Loading model from local path: {LOCAL_MODEL_PATH}")
        scheduler = DDIMScheduler.from_pretrained(
                    LOCAL_MODEL_PATH, subfolder="scheduler", torch_dtype=torch.float16, safety_checker=None)
        pipe = StableDiffusionPipeline.from_pretrained(
            LOCAL_MODEL_PATH, torch_dtype=torch.float16, safety_checker=None,scheduler=scheduler
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
                    LOCAL_MODEL_PATH, torch_dtype=torch.float16, safety_checker=None,scheduler=scheduler
                )

    pipe=pipe.to(DEVICE)
    pipe.enable_attention_slicing("auto")
    pipe.unet.enable_gradient_checkpointing()
#     pipe.enable_model_cpu_offload()
    pipe.safety_checker = None
#     pipe.feature_extractor = None
    return pipe

def process_image_to_bytes(image, idx):
    """Convert a single image to its byte representation."""
    img_byte_arr = io.BytesIO()
    image.save(img_byte_arr, format="PNG")
    img_byte_arr.seek(0)
    return f"image_{idx + 1}.png", img_byte_arr.getvalue()

# Initialize the Flask app

def clear_cache():
    if torch.cuda.is_available():
        print("Clearing CUDA cache on GPU 0...")
        torch.cuda.set_device(0)
        torch.cuda.empty_cache()

app = Flask(__name__)

metrics = PrometheusMetrics(app)

app_name = os.getenv("DIFFUSION_APP_NAME", "diffusion_service")
app_version = os.getenv("DIFFUSION_APP_VERSION", "1.0.0")
metrics.info(app_name, "Application info prometheus", version=app_version)

if(pipeline is None):
    print("Initializing the pipeline...")
    pipeline = get_pipeline()
    reserve_vram()
#     torch.cuda.empty_cache()

@app.route("/")
def index():
    return "Hi baby!"


@app.route("/generate-images", methods=["POST"])
def generate_images():
    """
    Endpoint to generate images and return them in a ZIP file.
    Request JSON should contain:
        - prompt (str): Text describing the image.
        - negative_prompt (str, optional): Text to avoid in the image.
        - num_inference_steps (int, optional): Number of steps (default: 50).
        - guidance_scale (float, optional): Adherence to the prompt (default: 7.5).
        - num_images (int, optional): Number of images to generate (default: 1).
        - height (int, optional): Height of images (default: 512).
        - width (int, optional): Width of images (default: 512).
    """
    try:
        release_vram()
        clear_cache()
#         pipeline = get_pipeline()

        data = request.get_json(silent=True) or {}

        prompt = data.get("prompt", "a beautiful landscape")
        negative_prompt = data.get("negative_prompt", None)
        num_inference_steps = data.get("num_inference_steps", 50)
        guidance_scale = data.get("guidance_scale", 7.5)
        num_images = data.get("num_images", 1)
        height = data.get("height", 512)
        width = data.get("width", 512)

        print(f"Generating {num_images} image(s) for prompt: '{prompt}'")

        images = pipeline(
                prompt,
                negative_prompt=negative_prompt,
                num_inference_steps=num_inference_steps,
                guidance_scale=guidance_scale,
                height=height,
                width=width,
                num_images_per_prompt=num_images,
            ).images

        zip_buffer = io.BytesIO()
        with zipfile.ZipFile(zip_buffer, "w", compression=zipfile.ZIP_DEFLATED) as zip_file:
            with ThreadPoolExecutor() as executor:
                results = executor.map(process_image_to_bytes, images, range(len(images)))

                for file_name, img_data in results:
                    zip_file.writestr(file_name, img_data)


        zip_buffer.seek(0)

#         del pipeline
        clear_cache()
        reserve_vram()

        return send_file(
            zip_buffer,
            mimetype="application/zip",
            as_attachment=True,
            download_name="generated_images.zip",
        )

    except Exception as e:
        print("Error Traceback:", traceback.format_exc())
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=os.getenv("FLASK_DEBUG", False))
