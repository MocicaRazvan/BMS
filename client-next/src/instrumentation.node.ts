import { NodeSDK } from "@opentelemetry/sdk-node";
import { ZipkinExporter } from "@opentelemetry/exporter-zipkin";
import { ATTR_SERVICE_NAME } from "@opentelemetry/semantic-conventions";
import { BatchSpanProcessor } from "@opentelemetry/sdk-trace-node";
import { getNodeAutoInstrumentations } from "@opentelemetry/auto-instrumentations-node";
import {
  TraceIdRatioBasedSampler,
  ParentBasedSampler,
} from "@opentelemetry/sdk-trace-base";
import {
  browserDetector,
  detectResourcesSync,
  envDetector,
  hostDetector,
  processDetector,
  Resource,
} from "@opentelemetry/resources";
import { PrometheusExporter } from "@opentelemetry/exporter-prometheus";
import { RuntimeNodeInstrumentation } from "@opentelemetry/instrumentation-runtime-node";
import { HttpInstrumentation } from "@opentelemetry/instrumentation-http";
import { MeterProvider } from "@opentelemetry/sdk-metrics";
import { HostMetrics } from "@opentelemetry/host-metrics";
import { registerInstrumentations } from "@opentelemetry/instrumentation";
import {
  BatchLogRecordProcessor,
  ConsoleLogRecordExporter,
  SimpleLogRecordProcessor,
} from "@opentelemetry/sdk-logs";
import { OTLPLogExporter } from "@opentelemetry/exporter-logs-otlp-http";
import { IORedisInstrumentation } from "@opentelemetry/instrumentation-ioredis";
import { PgInstrumentation } from "@opentelemetry/instrumentation-pg";

if (
  !process.env.NEXT_SERVICE_NAME ||
  !process.env.NEXT_ZIPKIN_URL ||
  !process.env.NEXT_TRACING_PROBABILITY
) {
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
      monitoringPrecision: 5000,
    }),
    new HttpInstrumentation(),
    new IORedisInstrumentation({
      requireParentSpan: false,
      enabled: true,
    }),
    new PgInstrumentation({
      enhancedDatabaseReporting: true,
      requireParentSpan: false,
    }),
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
        maxQueueSize: 2048,
        maxExportBatchSize: 512,
        scheduledDelayMillis: 10000,
        exportTimeoutMillis: 50000,
      },
    ),
  ],
  logRecordProcessors: [
    new SimpleLogRecordProcessor(new ConsoleLogRecordExporter()),
    ...(process.env.NODE_ENV === "production" && process.env.NEXT_LOKI_URL
      ? [
          new BatchLogRecordProcessor(
            new OTLPLogExporter({
              url: process.env.NEXT_LOKI_URL,
            }),
            {
              scheduledDelayMillis: 3000,
              maxExportBatchSize: 512,
              exportTimeoutMillis: 30000,
              maxQueueSize: 2048,
            },
          ),
        ]
      : []),
  ],
  sampler: new ParentBasedSampler({
    root: new TraceIdRatioBasedSampler(
      parseFloat(process.env.NEXT_TRACING_PROBABILITY || "0.1"),
    ),
  }),
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
