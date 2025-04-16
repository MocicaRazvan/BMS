import { ReactElement, cloneElement } from "react";
import getSvgMap from "@/lib/world-svg-map";

export interface SVGMap {
  svgMap: string;
}
interface WithMapProps<P> {
  children: ReactElement<P>;
}

export default function WorldWrapper<P>({ children }: WithMapProps<P>) {
  return cloneElement(children, {
    ...children.props,
    svgMap: getSvgMap(),
  });
}
