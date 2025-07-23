import React, { useEffect, useState } from "react";
import { motion, useSpring } from "framer-motion";

export default function AnimatedCircularProgressBar({
  max = 100,
  min = 0,
  value = 0,
  gaugePrimaryColor,
  gaugeSecondaryColor = "#d1d5db",
  unit = "",
  className = "",
}) {
  const circumference = 2 * Math.PI * 45;
  const percentPx = circumference / 100;
  const targetPercent = ((value - min) / (max - min)) * 100;

  // Framer Motion animasyonlu value ve percent
  const springValue = useSpring(0, { 
    duration: 6,
    bounce: 0,
    damping: 40,
    stiffness: 40
  });
  const springPercent = useSpring(0, { 
    duration: 6,
    bounce: 0,
    damping: 40,
    stiffness: 40
  });

  const [displayValue, setDisplayValue] = useState(0);
  const [displayPercent, setDisplayPercent] = useState(0);

  useEffect(() => {
    springValue.set(value);
    springPercent.set(targetPercent);

    const unsubValue = springValue.on("change", (v) => setDisplayValue(v));
    const unsubPercent = springPercent.on("change", (v) => setDisplayPercent(v));

    return () => {
      unsubValue();
      unsubPercent();
    };
  }, [value, targetPercent, springValue, springPercent]);

  return (
    <div
      className={className}
      style={{
        width: "200px",
        height: "200px",
        position: "relative",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
      }}
    >
      <svg fill="none" width="200" height="200" strokeWidth="2" viewBox="0 0 100 100">
        <circle
          cx="50"
          cy="50"
          r="45"
          strokeWidth="10"
          strokeDashoffset="0"
          strokeLinecap="round"
          strokeLinejoin="round"
          style={{
            stroke: gaugeSecondaryColor,
            strokeDasharray: `${circumference} ${circumference}`,
            transform: "rotate(-90deg)",
            transformOrigin: "50% 50%",
            opacity: 1,
          }}
        />
        <motion.circle
          cx="50"
          cy="50"
          r="45"
          strokeWidth="10"
          strokeDashoffset="0"
          strokeLinecap="round"
          strokeLinejoin="round"
          style={{
            stroke: gaugePrimaryColor,
            transform: "rotate(-90deg)",
            transformOrigin: "50% 50%",
          }}
          stroke={gaugePrimaryColor}
          fill="transparent"
          strokeDasharray={`${displayPercent * percentPx} ${circumference}`}
        />
      </svg>
      <div
        style={{
          position: "absolute",
          top: "50%",
          left: "50%",
          transform: "translate(-50%, -60%)",
          textAlign: "center",
        }}
      >
        <div style={{ fontSize: "2rem", fontWeight: 700, color: gaugePrimaryColor }}>
          {Math.round(displayPercent)}%
        </div>
        <div style={{ fontSize: "1.3rem", fontWeight: 700, color: "#22223b" }}>
          {typeof value === "number" && value % 1 !== 0
            ? displayValue.toFixed(1)
            : Math.round(displayValue)}{" "}
          {unit}
        </div>
      </div>
    </div>
  );
} 