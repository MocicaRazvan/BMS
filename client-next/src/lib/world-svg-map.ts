import DottedMap from "dotted-map";

export interface SVGMap {
  dark: string;
  light: string;
}

declare const globalThis: {
  svgMap?: SVGMap;
} & typeof global;
const radius = 0.22;
const shape = "circle";
const backgroundColor = "transparent";

function getSvgMap() {
  if (!globalThis.svgMap) {
    // console.log("SVG map not found, generating a new one...");
    const map = new DottedMap({ height: 100, grid: "diagonal" });
    globalThis.svgMap = {
      dark: map.getSVG({
        radius,
        color: "hsl(211,13%,39%)",
        shape,
        backgroundColor,
      }),
      light: map.getSVG({
        radius,
        color: "hsl(215.4, 16.3%, 53.9%)",
        shape,
        backgroundColor,
      }),
    };
  }
  return globalThis.svgMap;
}
getSvgMap();

export default getSvgMap;
