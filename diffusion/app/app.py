import os
import torch
from diffusers import StableDiffusionPipeline, DDIMScheduler, DiffusionPipeline
from flask import Flask, request, send_file, jsonify
import io
import zipfile
import traceback
from prometheus_flask_exporter import PrometheusMetrics
from concurrent.futures import ThreadPoolExecutor

MODEL_ID = os.getenv("DIFFUSION_MODEL_ID", "CompVis/stable-diffusion-v1-4")
LOCAL_MODEL_PATH = (
    os.path.join(os.getenv("DIFFUSION_MODEL_PATH", ""), "./models")
    if "DIFFUSION_MODEL_PATH" in os.environ
    else "./models/stable_diffusion_v1_4"
)
DEVICE = "cuda"


def get_pipeline() -> StableDiffusionPipeline:
    """
    Load the Stable Diffusion model pipeline.
    If not available locally, download and save it.
    """
    if os.path.exists(LOCAL_MODEL_PATH):
        print(f"Loading model from local path: {LOCAL_MODEL_PATH}")
        pipe = StableDiffusionPipeline.from_pretrained(
            LOCAL_MODEL_PATH, torch_dtype=torch.float16, safety_checker=None,
        )
    else:
        print(f"Downloading model: {MODEL_ID}")
        pipe = StableDiffusionPipeline.from_pretrained(
            MODEL_ID, torch_dtype=torch.float16
        )
        print(f"Saving model locally to: {LOCAL_MODEL_PATH}")
        pipe.save_pretrained(LOCAL_MODEL_PATH)

    return pipe.to(DEVICE)

def process_image_to_bytes(image, idx):
    """Convert a single image to its byte representation."""
    img_byte_arr = io.BytesIO()
    image.save(img_byte_arr, format="PNG")
    img_byte_arr.seek(0)
    return f"image_{idx + 1}.png", img_byte_arr.getvalue()

# Initialize the Flask app

app = Flask(__name__)

metrics = PrometheusMetrics(app)

app_name = os.getenv("DIFFUSION_APP_NAME", "diffusion_service")
app_version = os.getenv("DIFFUSION_APP_VERSION", "1.0.0")
metrics.info(app_name, "Application info prometheus", version=app_version)


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

        pipeline = get_pipeline()

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

        del pipeline
        torch.cuda.empty_cache()
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
