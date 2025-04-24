import { NextRequest, NextResponse } from "next/server";
import { getUserWithMinRole } from "@/lib/user";
import { getCsrfNextAuthHeader } from "@/actions/get-csr-next-auth";
import fetchFactory from "@/lib/fetchers/fetchWithRetry";

const springUrl = process.env.NEXT_PUBLIC_SPRING!;

export async function POST(req: NextRequest) {
  try {
    const [user, body, csrfHeader] = await Promise.all([
      getUserWithMinRole("ROLE_TRAINER"),
      req.json(),
      getCsrfNextAuthHeader(),
    ]);

    if (!user || !user?.token) {
      return new NextResponse("Unauthorized", { status: 401 });
    }

    const response = await fetchFactory(fetch)(
      springUrl + "/diffusion/generate-images",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${user.token}`,
          ...csrfHeader,
        },
        body: JSON.stringify(body),
        credentials: "include",
      },
    );

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
