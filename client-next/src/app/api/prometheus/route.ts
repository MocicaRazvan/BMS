import { NextResponse } from "next/server";
import client from "prom-client";

const register = new client.Registry();
client.collectDefaultMetrics({
  register,
  gcDurationBuckets: [0.1, 0.3, 1.5, 5.0],
  prefix: process.env.NEXT_SERVICE_NAME,
  eventLoopMonitoringPrecision: 150,
});

export async function GET() {
  const metrics = await register.metrics();
  return new NextResponse(metrics, {
    status: 200,
    headers: {
      "Content-Type": register.contentType,
    },
  });
}
