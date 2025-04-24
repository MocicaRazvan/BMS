import { NextRequest, NextResponse } from "next/server";
import { getUserWithMinRole } from "@/lib/user";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import { getCsrfNextAuthHeader } from "@/actions/get-csr-next-auth";
import fetchFactory from "@/lib/fetchers/fetchWithRetry";

const springUrl = process.env.NEXT_PUBLIC_SPRING!;

export async function POST(req: NextRequest) {
  try {
    await getUserWithMinRole("ROLE_TRAINER");

    const [session, body, csrfHeader] = await Promise.all([
      getServerSession(authOptions),
      await req.json(),
      await getCsrfNextAuthHeader(),
    ]);

    if (!session || !session?.user?.token) {
      return new NextResponse("Unauthorized", { status: 401 });
    }

    const response = await fetchFactory(fetch)(
      springUrl + "/diffusion/generate-images",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${session.user.token}`,
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
