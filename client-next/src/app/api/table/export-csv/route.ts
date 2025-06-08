import { NextRequest, NextResponse } from "next/server";
import { exportTableBodySchema } from "@/app/api/table/export-utils";
import { ZodError } from "zod";
import { asString, generateCsv, mkConfig } from "export-to-csv";

export async function POST(request: NextRequest) {
  try {
    const { tableColumnHeaders, tableRows, fileName } = await request
      .json()
      .then(exportTableBodySchema.parse);
    const formattedRows = tableRows.map((row) =>
      tableColumnHeaders.reduce(
        (acc, cur, index) => {
          acc[cur.id] = row[index];
          return acc;
        },
        {} as Record<string, string | number>,
      ),
    );
    if (formattedRows.length === 0) {
      return NextResponse.json(
        {
          error: "No data to export",
        },
        { status: 400 },
      );
    }
    const csvConfig = mkConfig({
      fieldSeparator: ",",
      filename: fileName,
      decimalSeparator: ".",
      showColumnHeaders: true,
      columnHeaders: tableColumnHeaders.map((h) => h.id),
      useKeysAsHeaders: true,
    });
    const csvData = generateCsv(csvConfig)(formattedRows);
    const csvBuffer = new Uint8Array(Buffer.from(asString(csvData)));
    return new NextResponse(csvBuffer, {
      headers: {
        "Content-Type": "text/csv; charset=utf-8",
        "Content-Disposition": `attachment; filename="${fileName}"`,
      },
    });
  } catch (error) {
    if (error instanceof ZodError) {
      console.error("Validation error:", JSON.stringify(error.errors, null, 2));
      return NextResponse.json(
        {
          error: "Validation failed",
          details: error.errors,
        },
        { status: 400 },
      );
    }

    return NextResponse.json(
      {
        error: "Unexpected error",
        message: (error as any)?.message ?? "Unknown error",
      },
      { status: 500 },
    );
  }
}
