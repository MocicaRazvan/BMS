export interface SelectedRowsTexts {
  of: string;
  rowsSelected: string;
}

interface Props {
  selectedLength: number;
  totalLength: number;
  texts: SelectedRowsTexts;
}

export default function SelectedRows({
  selectedLength,
  totalLength,
  texts,
}: Props) {
  return (
    <div className="flex-1 text-sm text-muted-foreground">
      {selectedLength} {texts.of} {totalLength} {texts.rowsSelected}
    </div>
  );
}
