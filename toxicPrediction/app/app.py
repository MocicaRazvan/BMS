import traceback
import re

from flask import Flask, request, jsonify
from flask_caching import Cache
from opentelemetry import trace
from opentelemetry.exporter.zipkin.json import ZipkinExporter
from opentelemetry.instrumentation.flask import FlaskInstrumentor
from opentelemetry.sdk.resources import Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.sdk.trace.sampling import ParentBased, TraceIdRatioBased
from prometheus_flask_exporter.multiprocess import GunicornInternalPrometheusMetrics

from app_config import APP_NAME, APP_VERSION, FLASK_DEBUG, ZIPKIN_SAMPLE_RATE, ZIPKIN_URL, MAX_BODY_SIZE
from app_config import CACHE_REDIS_DB, CACHE_REDIS_HOST, CACHE_REDIS_PASSWORD, CACHE_REDIS_PORT, CACHE_REDIS_EXPIRATION
from logger import logger
from models import predict_english, predict_toxicity
from utils import make_cache_key, error_response, ToxicReason

app = Flask(__name__)
metrics = GunicornInternalPrometheusMetrics(
    app,
    default_labels={"application": APP_NAME}
)
app.config['MAX_CONTENT_LENGTH'] = MAX_BODY_SIZE

metrics.info(APP_NAME.replace("-", "_"), "Application info prometheus", version=APP_VERSION)

provider = TracerProvider(resource=Resource(attributes={"service.name": APP_NAME}),
                          sampler=ParentBased(TraceIdRatioBased(ZIPKIN_SAMPLE_RATE)))
zipkin_exporter = ZipkinExporter(endpoint=ZIPKIN_URL, timeout=30)
provider.add_span_processor(
    BatchSpanProcessor(span_exporter=zipkin_exporter, max_queue_size=2048, schedule_delay_millis=10000,
                       max_export_batch_size=512))
trace.set_tracer_provider(provider)

FlaskInstrumentor().instrument_app(app, excluded_urls="metrics,healthz")

cache = Cache(app, config={
    "CACHE_TYPE": "redis",
    "CACHE_REDIS_HOST": CACHE_REDIS_HOST,
    "CACHE_REDIS_PORT": CACHE_REDIS_PORT,
    "CACHE_REDIS_DB": CACHE_REDIS_DB,
    "CACHE_REDIS_PASSWORD": CACHE_REDIS_PASSWORD,
    "CACHE_DEFAULT_TIMEOUT": CACHE_REDIS_EXPIRATION,
    "CACHE_KEY_PREFIX": "toxicity"
})


@app.route("/healthz")
def healthz():
    return "OK"


@app.route("/isToxic", methods=["PATCH"])
@cache.cached(make_cache_key=make_cache_key, key_prefix="toxicity:isToxic")
def toxicity():
    try:
        data = request.get_json(silent=True) or {}
        text = data.get("text")
        if not text or not isinstance(text, str):
            return error_response("text is required")

        stripped_text = re.sub(r'\s+', ' ', text).strip().lower()

        is_english = predict_english(stripped_text)

        if not is_english:
            return jsonify({
                'failure': True,
                'reason': ToxicReason.LANGUAGE.value,
                'message': "ENGLISH ONLY"
            })

        is_toxic = predict_toxicity(stripped_text)

        if is_toxic:
            return jsonify({
                'failure': True,
                'reason': ToxicReason.TOXICITY.value,
                'message': "TOXIC"
            })

        return jsonify({
            'failure': False,
            'reason': ToxicReason.NONE.value,
            'message': "CLEAN"
        })

    except Exception as e:
        logger.exception("Error Traceback:", traceback.format_exc())
        return error_response("Exception", 500, str(e))


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5002, debug=FLASK_DEBUG)
