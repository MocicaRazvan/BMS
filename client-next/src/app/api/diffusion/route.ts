import { NextRequest, NextResponse } from "next/server";
import { getUserWithMinRole } from "@/lib/user";

const diffusion_service =
  process.env.DIFFUSION_SERVICE_URL || "http://localhost:5000";
const url = diffusion_service + "/generate-images";

export async function POST(req: NextRequest) {
  try {
    await getUserWithMinRole("ROLE_TRAINER");

    const body = await req.json();

    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      return new NextResponse("Failed to generate images", {
        status: response.status,
      });
    }

    return new NextResponse(response.body, {
      headers: {
        "Content-Type": "application/zip",
      },
      status: 200,
    });
  } catch (error) {
    console.error("Error fetching images:", error);
    return new NextResponse("Internal Server Error", { status: 500 });
  }
}
