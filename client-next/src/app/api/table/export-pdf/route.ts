import { NextRequest, NextResponse } from "next/server";
import { exportTableBodySchema } from "@/app/api/table/export-utils";
import { ZodError } from "zod";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import "@/lib/calibri-normal-font-jspdf";

export async function POST(request: NextRequest) {
  try {
    const { tableColumnHeaders, tableRows, fileName } = await request
      .json()
      .then(exportTableBodySchema.parse);

    const doc = new jsPDF({
      orientation: "landscape",
      compress: true,
      putOnlyUsedFonts: true,
    });
    autoTable(doc, {
      head: [tableColumnHeaders.map((h) => h.id)],
      body: tableRows,
      useCss: true,
      theme: "striped",
      headStyles: { fillColor: [22, 160, 133] },
      bodyStyles: { fillColor: [244, 244, 244] },
      alternateRowStyles: { fillColor: [255, 255, 255] },
      startY: 20,
      margin: { top: 20, bottom: 20 },
      styles: {
        fontSize: 10,
        cellPadding: 4,
        overflow: "linebreak",
        minCellWidth: 20,
        font: "calibri",
        fontStyle: "normal",
      },
    });
    const pdfBlob = doc.output("arraybuffer");
    const pdfBuffer = Buffer.from(pdfBlob);
    return new NextResponse(pdfBuffer, {
      headers: {
        "Content-Type": "application/pdf",
        "Content-Disposition": `attachment; filename="${fileName}"`,
        "Content-Length": String(pdfBuffer.length),
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
