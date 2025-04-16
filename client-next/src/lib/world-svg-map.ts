import DottedMap from "dotted-map";

declare const globalThis: {
  svgMap?: string;
} & typeof global;

function getSvgMap() {
  if (!globalThis.svgMap) {
    console.log("SVG map not found, generating a new one...");
    const map = new DottedMap({ height: 100, grid: "diagonal" });
    globalThis.svgMap = map.getSVG({
      radius: 0.22,
      color: "#FFFFFF40",
      shape: "circle",
      backgroundColor: "#1A1A1A",
    });
  }
  return globalThis.svgMap;
}
getSvgMap();

export default getSvgMap;
