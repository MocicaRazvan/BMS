import { NextRequest, NextResponse } from "next/server";

function handler(req: NextRequest) {
  return NextResponse.json({
    url: req.nextUrl,
    method: req.method,
    message: "The route does not exist",
    status: 404,
    timestamp: new Date().toISOString(),
  });
}

export {
  handler as GET,
  handler as POST,
  handler as DELETE,
  handler as PATCH,
  handler as PUT,
  handler as OPTIONS,
  handler as HEAD,
};
