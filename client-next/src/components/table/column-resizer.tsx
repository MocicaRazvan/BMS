import { Header } from "@tanstack/react-table";

export const ColumnResizer = ({ header }: { header: Header<any, unknown> }) => {
  if (!header.column.getCanResize()) return <></>;

  return (
    <div
      {...{
        onMouseDown: header.getResizeHandler(),
        onTouchStart: header.getResizeHandler(),
        onDoubleClick: () => header.column.resetSize(),
        className: `absolute top-0 right-0 cursor-col-resize w-1 border-none h-full bg-muted hover:bg-primary/35 hover:w-2 
        group-hover:bg-primary/35 group-hover:w-2 transition-all duration-200 ease-in-out`,
        style: {
          userSelect: "none",
          touchAction: "none",
        },
      }}
    />
  );
};
