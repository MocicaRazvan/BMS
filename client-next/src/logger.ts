import { AnyValue, logs, SeverityNumber } from "@opentelemetry/api-logs";

const logger = logs.getLogger(
  process.env.NEXT_SERVICE_NAME || "next-js",
  process.env.NEXT_SERVICE_VERSION || "1.0.0",
  {
    includeTraceContext: true,
  },
);

function emit(severityNumber: SeverityNumber, body: AnyValue) {
  logger.emit({
    severityNumber,
    body,
    severityText: SeverityNumber[severityNumber],
    attributes: {
      "service.name": process.env.NEXT_SERVICE_NAME || "next-js",
    },
  });
}

export function emitInfo(body: AnyValue) {
  emit(SeverityNumber.INFO, body);
}

export function emitError(body: AnyValue) {
  emit(SeverityNumber.ERROR, body);
}
