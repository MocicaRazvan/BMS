import { NextRequest, NextResponse } from "next/server";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import { vectorStoreInstance } from "@/lib/langchain";
import { emitInfo } from "@/logger";

export async function GET(req: NextRequest) {
  emitInfo("GET /api/chat/pg-admin");
  const session = await getServerSession(authOptions);
  if (session?.user?.role !== "ROLE_ADMIN") {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }
  try {
    const pgVectorStore = await vectorStoreInstance.getPGVectorStore();

    if (!pgVectorStore) {
      return NextResponse.json(
        { error: "Error getting pgVectorStore" },
        { status: 500 },
      );
    }

    await pgVectorStore.delete({ filter: {} });
    await vectorStoreInstance.addToPGVectorStore();

    return NextResponse.json({ success: true }, { status: 200 });
  } catch (error) {
    console.error(error);
    return NextResponse.json({ error: "Error" }, { status: 500 });
  }
}
