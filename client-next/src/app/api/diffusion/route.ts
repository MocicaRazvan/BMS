import { NextRequest, NextResponse } from "next/server";
import { getUserWithMinRole } from "@/lib/user";

const diffusion_service =
  process.env.DIFFUSION_SERVICE_URL || "http://localhost:5000";
const url = diffusion_service + "/generate-images";

export async function POST(req: NextRequest) {
  try {
    // await getUserWithMinRole("ROLE_TRAINER");

    const body = await req.json();
    // const body = {
    //   negative_prompt: "blurry, low quality",
    //   prompt: "salad, chicken, tomatoes, realistic",
    //   num_images: 5,
    //   width: 512,
    //   height: 512,
    // };

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

    // const zipBuffer = await response.arrayBuffer();

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
