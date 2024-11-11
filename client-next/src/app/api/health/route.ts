import { NextResponse } from "next/server";
import { emitInfo } from "@/logger";

export async function GET() {
  emitInfo({
    message: "Health Check",
  });

  return NextResponse.json(
    {
      status: "UP",
      sdkInitialized: true,
      timestamp: new Date().toISOString(),
    },
    {
      status: 200,
      headers: {
        "Content-Type": "application/json",
        "Cache-Control": "no-store",
      },
    },
  );
}
