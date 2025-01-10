interface ProgressTextProps {
  progress: number | undefined;
}

export default function ProgressText({ progress }: ProgressTextProps) {
  if (progress === undefined) {
    return null;
  }
  return (
    <div className="text-lg font-bold transition-opacity duration-300 ease-out">
      {progress.toFixed(0) + "%"}
    </div>
  );
}
