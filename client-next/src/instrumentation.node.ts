import {NodeSDK} from "@opentelemetry/sdk-node";
import {ZipkinExporter} from "@opentelemetry/exporter-zipkin";
import {ATTR_SERVICE_NAME} from "@opentelemetry/semantic-conventions";
import {BatchSpanProcessor} from "@opentelemetry/sdk-trace-node";
import {getNodeAutoInstrumentations} from "@opentelemetry/auto-instrumentations-node";
import {TraceIdRatioBasedSampler} from "@opentelemetry/sdk-trace-base";
import {
    Resource,
    detectResourcesSync,
    envDetector,
    hostDetector,
    processDetector,
    browserDetector,
} from "@opentelemetry/resources";
import {PrometheusExporter} from "@opentelemetry/exporter-prometheus";
import {RuntimeNodeInstrumentation} from "@opentelemetry/instrumentation-runtime-node";
import {HttpInstrumentation} from "@opentelemetry/instrumentation-http";
import {MeterProvider} from "@opentelemetry/sdk-metrics";
import {HostMetrics} from "@opentelemetry/host-metrics";
import {registerInstrumentations} from "@opentelemetry/instrumentation";

console.log("NEXT_SERVICE_NAME:", process.env.NEXT_SERVICE_NAME);
console.log("NEXT_ZIPKIN_URL:", process.env.NEXT_ZIPKIN_URL);

if (!process.env.NEXT_SERVICE_NAME || !process.env.NEXT_ZIPKIN_URL || !process.env.NEXT_TRACING_PROBABILITY) {
    console.error(
        "Missing environment variables: Check NEXT_SERVICE_NAME and NEXT_ZIPKIN_URL",
    );
} else {
    console.log("Environment variables loaded correctly");
}
const customResource = new Resource({
    [ATTR_SERVICE_NAME]: process.env.NEXT_SERVICE_NAME,
});
const detectedResources = detectResourcesSync({
    detectors: [envDetector, hostDetector, processDetector, browserDetector],
});

const resources = customResource.merge(detectedResources);

const prometheusExporter = new PrometheusExporter({
    port: 9464,
});

const meterProvider = new MeterProvider({
    readers: [prometheusExporter],
    resource: resources,
});

const hostMetrics = new HostMetrics({
    name: process.env.NEXT_SERVICE_NAME,
    meterProvider,
});

registerInstrumentations({
    meterProvider,
    instrumentations: [
        getNodeAutoInstrumentations(),
        new RuntimeNodeInstrumentation({
            enabled: true,
            eventLoopUtilizationMeasurementInterval: 5000,
        }),
        new HttpInstrumentation(),
    ],
});

hostMetrics.start();

const sdk = new NodeSDK({
    resource: resources,
    spanProcessors: [
        new BatchSpanProcessor(
            new ZipkinExporter({
                url: process.env.NEXT_ZIPKIN_URL,
                serviceName: process.env.NEXT_SERVICE_NAME,
            }),
            {
                maxQueueSize: 10,
                maxExportBatchSize: 10,
                scheduledDelayMillis: 500,
                exportTimeoutMillis: 1000,
            },
        ),
    ],
    sampler: new TraceIdRatioBasedSampler(parseFloat(process.env.NEXT_TRACING_PROBABILITY || "0.1")),
    // metricReader: prometheusExporter,
});

sdk.start();

console.log("SDK initialized successfully");

process.on("SIGTERM", () => {
    sdk
        .shutdown()
        .then(() => console.log("Tracing terminated"))
        .catch((error) => console.log("Error terminating tracing", error))
        .finally(() => process.exit(0));
});
