import { NextResponse } from "next/server";
import client from "prom-client";

export const dynamic = "force-dynamic";

const register = new client.Registry();

const raw = process.env.NEXT_SERVICE_NAME || "next_js";

client.collectDefaultMetrics({
  register,
  gcDurationBuckets: [0.1, 0.3, 1.5, 5.0],
  prefix: raw.replace(/-/g, "_"),
  eventLoopMonitoringPrecision: 550,
});
//todo maybe remove this, you have instrumentation in the app
export async function GET() {
  const metrics = await register.metrics();
  return new NextResponse(metrics, {
    status: 200,
    headers: {
      "Content-Type": register.contentType,
    },
  });
}
