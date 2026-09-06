"use client";

import Image from "next/image";
import { motion } from "framer-motion";
import Link from "next/link";

interface LogoProps {
  size?: "sm" | "md" | "lg";
  asLink?: boolean;
}

export default function Logo({ size = "md", asLink = true }: LogoProps) {
  const dimensions = {
    sm: { img: 36, text: "text-xl", gap: "gap-2" },
    md: { img: 44, text: "text-2xl", gap: "gap-3" },
    lg: { img: 56, text: "text-3xl", gap: "gap-3" }
  };
  
  const { img, text, gap } = dimensions[size];

  const content = (
    <motion.div 
      className={`flex items-center ${gap} group`}
      style={{ perspective: "1000px" }}
      initial={{ opacity: 0, scale: 0.9 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.5, ease: "easeOut" }}
    >
      <motion.div
        whileHover={{ 
          scale: 1.05,
          rotateY: 10,
          rotateX: -10,
          filter: "drop-shadow(0px 8px 16px rgba(99, 102, 241, 0.5))"
        }}
        transition={{ 
          type: "spring", stiffness: 300, damping: 20 
        }}
        className="relative transform-gpu"
        style={{ transformStyle: "preserve-3d" }}
      >
        <Image 
          src="/logo-theme.svg" 
          alt="EventHive Logo" 
          width={img} 
          height={img} 
          className="drop-shadow-lg relative z-10"
        />
        {/* Animated glow behind the logo */}
        <div className="absolute inset-0 bg-primary/30 blur-xl rounded-full scale-150 opacity-0 group-hover:opacity-100 transition-opacity duration-500 -z-10"></div>
      </motion.div>
      
      <div className="relative overflow-hidden">
        <span className={`${text} font-black tracking-tighter text-transparent bg-clip-text bg-gradient-to-r from-primary to-accent relative z-10 hidden sm:block`}>
          EventHive
        </span>
        {/* Shine sweep effect on hover */}
        <motion.div 
          className="absolute inset-0 w-full h-full bg-gradient-to-r from-transparent via-white/50 to-transparent -skew-x-12 z-20"
          initial={{ left: "-150%" }}
          whileHover={{ left: "150%" }}
          transition={{ duration: 0.8, ease: "easeInOut" }}
        />
      </div>
    </motion.div>
  );

  if (asLink) {
    return (
      <Link href="/" className="inline-block">
        {content}
      </Link>
    );
  }
  return content;
}
