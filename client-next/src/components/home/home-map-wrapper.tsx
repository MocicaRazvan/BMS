import DottedMap from "dotted-map";
import { cloneElement, ReactElement } from "react";

interface WithMapProps {
  children: ReactElement<{ svgMap: string }>;
}

export default function HomeMapWrapper({ children }: WithMapProps) {
  const map = new DottedMap({ height: 100, grid: "diagonal" });

  const svgMap = map.getSVG({
    radius: 0.22,
    color: "#FFFFFF40",
    shape: "circle",
    backgroundColor: "#1A1A1A",
  });

  return cloneElement(children, { svgMap });
}
