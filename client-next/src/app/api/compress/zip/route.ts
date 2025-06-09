import archiver from "archiver";
import { NextRequest, NextResponse } from "next/server";
import { z } from "zod";
import { PassThrough, Readable } from "node:stream";

const FormDataSchema = z.object({
  fileName: z.string().min(1, "Field name is required"),
  files: z
    .array(
      z.custom<File>((val) => val instanceof File, {
        message: "Invalid file",
      }),
    )
    .min(1, "At least one file is required"),
});
export async function POST(req: NextRequest) {
  const formData = await req.formData();

  const fileName = formData.get("fileName");
  const rawFiles = formData.getAll("files");
  const validationResult = FormDataSchema.safeParse({
    fileName,
    files: rawFiles,
  });

  if (!validationResult.success) {
    return NextResponse.json(validationResult, {
      status: 400,
    });
  }

  const { fileName: validatedFileName, files } = validationResult.data;
  const passthrough = new PassThrough();
  const archive = archiver("zip", {
    zlib: { level: 0 },
  });
  archive.pipe(passthrough);
  const parsedFiles = await Promise.all(
    files.map((file) =>
      file.arrayBuffer().then((arrayBuffer) => ({
        name: file.name,
        content: Buffer.from(arrayBuffer),
      })),
    ),
  );

  parsedFiles.forEach((file) => {
    archive.append(file.content, { name: file.name });
  });

  await archive.finalize();

  return new NextResponse(passthrough as unknown as BodyInit, {
    headers: {
      "Content-Type": "application/zip",
      "Content-Disposition": `attachment; filename="${validatedFileName}.zip"`,
    },
  });
}
