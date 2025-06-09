import path from "node:path";
import { promises as fs } from "fs";
import { NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";

const geoDataJSON = path.join(process.cwd(), "public", "data", "geoData.json");

export async function GET() {
  const session = await getServerSession(authOptions);
  if (session?.user?.role !== "ROLE_ADMIN") {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
  const data = await fs.readFile(geoDataJSON);
  return new NextResponse(data, {
    status: 200,
    headers: {
      "Content-Type": "application/json",
      "Cache-Control": "public, max-age=31536000, immutable",
    },
  });
}
