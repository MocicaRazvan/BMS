import traceback

import torch
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

from config import APP_NAME, APP_VERSION, FLASK_DEBUG, ZIPKIN_SAMPLE_RATE, ZIPKIN_URL
from config import CACHE_REDIS_DB, CACHE_REDIS_HOST, CACHE_REDIS_PASSWORD, CACHE_REDIS_PORT, CACHE_REDIS_EXPIRATION
from logger import logger
from model import predict_series
from utils import make_cache_key, error_response

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

FlaskInstrumentor().instrument_app(app, excluded_urls="metrics,healthz")

cache = Cache(app, config={
    "CACHE_TYPE": "redis",
    "CACHE_REDIS_HOST": CACHE_REDIS_HOST,
    "CACHE_REDIS_PORT": CACHE_REDIS_PORT,
    "CACHE_REDIS_DB": CACHE_REDIS_DB,
    "CACHE_REDIS_PASSWORD": CACHE_REDIS_PASSWORD,
    "CACHE_DEFAULT_TIMEOUT": CACHE_REDIS_EXPIRATION,
    "CACHE_KEY_PREFIX": "timeseries"
})


@app.route("/healthz")
def healthz():
    return "OK"


@app.route("/countAmount", methods=["PATCH"])
@cache.cached(make_cache_key=make_cache_key, key_prefix="timeseries:countAmount")
def countAmount():
    try:
        data = request.get_json(silent=True)
        count_list = data.get("count_list")
        if count_list is None:
            return error_response("count_list is required")

        total_amount_list = data.get("total_amount_list")
        if total_amount_list is None:
            return error_response("total_amount_list is required", )

        if len(count_list) != len(total_amount_list):
            return error_response("count_list and total_amount_list should have the same length", )

        prediction_length = data.get("prediction_length", 3)

        predictions = predict_series([count_list, total_amount_list], prediction_length)
        return jsonify({
            "count_quantiles": predictions[0].tolist(),
            "total_amount_quantiles": predictions[1].tolist()
        })

    except Exception as e:
        logger.exception("Error Traceback:", traceback.format_exc())
        return error_response("Exception", 500, str(e))
    finally:
        torch.cuda.empty_cache()


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001, debug=FLASK_DEBUG)
