import { HomeMapProps } from "@/components/home/home-map";
import getSvgMap from "@/lib/world-svg-map";
import dynamic from "next/dynamic";
import { Skeleton } from "@/components/ui/skeleton";

const DynamicHomeMap = dynamic(() => import("@/components/home/home-map"), {
  ssr: false,
  loading: () => <Skeleton className="w-full h-screen" />,
});

export default function HomeMapWrapper(props: HomeMapProps) {
  const svgMap = getSvgMap();
  return <DynamicHomeMap {...props} svgMap={svgMap} />;
}
