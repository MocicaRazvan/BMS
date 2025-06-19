"use client";
import {
  AnimatedTestimonials,
  Testimonial,
} from "@/components/aceternityui/animated-testimonials";
import { motion } from "framer-motion";

export type TypeTestimonials = `testimonial${1 | 2 | 3 | 4}`;
export interface HomeTestimonialsTexts {
  title: string;
  testimonials: Record<TypeTestimonials, Omit<Testimonial, "src">>;
}

interface Props {
  testimonials: Testimonial[];
  title: string;
}

const HomeTestimonials = ({ testimonials, title }: Props) => {
  return (
    <motion.section
      initial={{ opacity: 0, scale: 0.6 }}
      whileInView={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.6, ease: "easeOut" }}
      viewport={{ once: true, amount: 0.2 }}
    >
      <h1
        className={`text-3xl md:text-5xl font-bold text-center tracking-tighter mb-6`}
      >
        {title}
      </h1>
      <AnimatedTestimonials testimonials={testimonials} />
    </motion.section>
  );
};

export { HomeTestimonials };
