"use client";

import { motion } from "framer-motion";
import { WorldMap } from "@/components/aceternityui/world-map";
import { SVGMap } from "@/components/aceternityui/world-wrapper";

export interface HomeMapTexts {
  title: string;
  subtitle: string;
}

export interface HomeMapProps extends HomeMapTexts, Partial<SVGMap> {}

export default function HomeMap({ subtitle, title, svgMap }: HomeMapProps) {
  if (!svgMap) {
    return null;
  }
  return (
    <div className=" py-40 bg-background w-full">
      <div className="max-w-7xl mx-auto text-center">
        <p className="font-bold text-xl md:text-5xl ">
          <span className="font-semibold">
            {title.split("").map((word, idx) => (
              <motion.span
                key={idx}
                className="inline-block"
                initial={{ x: -10, opacity: 0 }}
                whileInView={{ x: 0, opacity: 1 }}
                transition={{ duration: 0.5, delay: idx * 0.04 }}
                viewport={{ once: true }}
              >
                {word === " " ? "\u00A0" : word}
              </motion.span>
            ))}
          </span>
        </p>
        <motion.p
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          transition={{ duration: 0.5, delay: 0.15, ease: "easeInOut" }}
          viewport={{ once: true }}
          className="text-sm md:text-lg text-neutral-500 max-w-2xl mx-auto py-4"
        >
          {subtitle}
        </motion.p>
      </div>
      <WorldMap
        svgMap={svgMap}
        dots={[
          {
            start: { lat: 44.4268, lng: 26.1025 }, // Bucharest
            end: { lat: 51.5074, lng: -1.99 }, // London
          },
          {
            start: { lat: 51.5074, lng: -1.99 }, // London
            end: { lat: 34.0522, lng: -118.2437 }, // Los Angeles
          },
          {
            start: { lat: 44.4268, lng: 26.1025 }, // Bucharest
            end: { lat: 39.9042, lng: 116.4074 }, // Beijing
          },
          {
            start: { lat: 44.4268, lng: 26.1025 }, // Bucharest
            end: { lat: 5.6037, lng: -0.187 }, // Accra, Ghana
          },
          {
            start: { lat: 44.4268, lng: 26.1025 }, // Bucharest
            end: { lat: -37.8688, lng: 133.2093 }, // Sydney
          },
          {
            start: { lat: 5.6037, lng: -0.187 }, // Accra, Ghana
            end: { lat: -28.9068, lng: -63.1729 }, // Rio de Janeiro, Brazil
          },
        ]}
      />
    </div>
  );
}
