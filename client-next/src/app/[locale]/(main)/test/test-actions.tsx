// RatioPieChartPreloader.tsx
import dynamic from "next/dynamic";

// 1) Wrap the same import, but immediately return () => null
//    so that when this component mounts, Next “preloads” the JS chunk
//    without ever displaying anything.
const RatioPieChartPreloader = dynamic(
  () =>
    import("@/components/charts/plans-ratio-pie-chart").then(() => () => null),
  {
    ssr: false, // still client-only, but Next will treat this as a dynamic component and preload
  },
);

export default RatioPieChartPreloader;
