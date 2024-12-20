import io
import traceback
import zipfile
from concurrent.futures import ThreadPoolExecutor

from flask import Flask, request, send_file, jsonify
from prometheus_flask_exporter.multiprocess import GunicornInternalPrometheusMetrics

from config import APP_NAME, APP_VERSION, FLASK_DEBUG, ZIPKIN_SAMPLE_RATE, ZIPKIN_URL
from logger import logger
from model import get_pipeline, reserve_vram, release_vram, clear_cache
from utils import process_image_to_bytes

from opentelemetry import trace
from opentelemetry.instrumentation.flask import FlaskInstrumentor
from opentelemetry.sdk.resources import Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.zipkin.json import ZipkinExporter
from opentelemetry.sdk.trace.sampling import ParentBased, TraceIdRatioBased

app = Flask(__name__)
metrics = GunicornInternalPrometheusMetrics(
    app,
    default_labels={"application": APP_NAME}
)

metrics.info(APP_NAME, "Application info prometheus", version=APP_VERSION)

provider = TracerProvider(resource=Resource(attributes={"service.name": APP_NAME}),
                          sampler=ParentBased(TraceIdRatioBased(ZIPKIN_SAMPLE_RATE)))
zipkin_exporter = ZipkinExporter(endpoint=ZIPKIN_URL, timeout=30)
provider.add_span_processor(
    BatchSpanProcessor(span_exporter=zipkin_exporter, max_queue_size=2048, schedule_delay_millis=10000,
                       max_export_batch_size=512))
trace.set_tracer_provider(provider)

FlaskInstrumentor().instrument_app(app, excluded_urls="/metrics,/healthz")


@app.route("/healthz")
def index():
    return "OK"


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

        data = request.get_json(silent=True) or {}

        prompt = data.get("prompt", "a beautiful landscape")
        negative_prompt = data.get("negative_prompt", None)
        num_inference_steps = data.get("num_inference_steps", 50)
        guidance_scale = data.get("guidance_scale", 7.5)
        num_images = data.get("num_images", 1)
        height = data.get("height", 512)
        width = data.get("width", 512)

        logger.info(f"Generating {num_images} image(s) for prompt: '{prompt}'")

        pipe = get_pipeline()
        images = pipe(
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

        clear_cache()
        reserve_vram()

        return send_file(
            zip_buffer,
            mimetype="application/zip",
            as_attachment=True,
            download_name="generated_images.zip",
        )

    except Exception as e:
        logger.exception("Error Traceback:", traceback.format_exc())
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=FLASK_DEBUG)
