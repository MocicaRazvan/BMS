import { z } from "zod";

export const exportTableBodySchema = z.object({
  tableColumnHeaders: z
    .array(
      z.object({
        id: z.coerce.string(),
        accessorKey: z.coerce.string(),
      }),
    )
    .nonempty(),
  tableRows: z.array(z.array(z.coerce.string())).nonempty(),
  fileName: z.string().nonempty(),
});

export type ExportTableBodyType = z.infer<typeof exportTableBodySchema>;
