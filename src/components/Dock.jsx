import React, { useRef, useMemo, useContext, createContext, useState, useEffect, Children, cloneElement } from "react";
import { motion, useMotionValue, useSpring, useTransform, AnimatePresence } from "framer-motion";

const DOCK_HEIGHT = 128;
const DEFAULT_MAGNIFICATION = 80;
const DEFAULT_DISTANCE = 150;
const DEFAULT_PANEL_HEIGHT = 64;

const DockContext = createContext();

function DockProvider({ children, value }) {
  return <DockContext.Provider value={value}>{children}</DockContext.Provider>;
}

function useDock() {
  const context = useContext(DockContext);
  if (!context) throw new Error("useDock, DockProvider içinde kullanılmalıdır");
  return context;
}

export function Dock({
  children,
  className = "",
  spring = { mass: 0.1, stiffness: 150, damping: 12 },
  magnification = DEFAULT_MAGNIFICATION,
  distance = DEFAULT_DISTANCE,
  panelHeight = DEFAULT_PANEL_HEIGHT,
  style = {},
}) {
  const mouseX = useMotionValue(Infinity);
  const isHovered = useMotionValue(0);

  const maxHeight = useMemo(() => Math.max(DOCK_HEIGHT, magnification + magnification / 2 + 4), [magnification]);
  const heightRow = useTransform(isHovered, [0, 1], [panelHeight, maxHeight]);
  const height = useSpring(heightRow, spring);

  return (
    <motion.div style={{ height, scrollbarWidth: "none", ...style }} className={`mx-2 flex max-w-full items-end overflow-x-auto ${className}`}>
      <motion.div
        onMouseMove={({ pageX }) => {
          isHovered.set(1);
          mouseX.set(pageX);
        }}
        onMouseLeave={() => {
          isHovered.set(0);
          mouseX.set(Infinity);
        }}
        className={`mx-auto flex w-fit gap-4 rounded-2xl bg-gray-50 px-4 dark:bg-neutral-900`}
        style={{ height: panelHeight }}
        role="toolbar"
        aria-label="Application dock"
      >
        <DockProvider value={{ mouseX, spring, distance, magnification }}>
          {children}
        </DockProvider>
      </motion.div>
    </motion.div>
  );
}

export function DockItem({ children, className = "", style = {}, active }) {
  const ref = useRef(null);
  const { distance, magnification, mouseX, spring } = useDock();
  const isHovered = useMotionValue(0);

  const mouseDistance = useTransform(mouseX, (val) => {
    const domRect = ref.current?.getBoundingClientRect() ?? { x: 0, width: 0 };
    return val - domRect.x - domRect.width / 2;
  });

  // Sadece kendi hover veya aktif olduğunda büyüsün
  const scale = useSpring(
    useTransform(isHovered, [0, 1], [1, 1.12]),
    spring
  );

  return (
    <motion.div
      ref={ref}
      style={{
        scale: active ? 1.12 : scale,
        display: "flex",
        flexDirection: "row",
        alignItems: "center",
        gap: 10,
        padding: "12px 20px",
        borderRadius: 12,
        background: active ? "#f5f3ff" : "#fff",
        boxShadow: active ? "0 6px 24px rgba(124,60,237,0.10)" : "0 2px 8px rgba(0,0,0,0.06)",
        border: active ? "2px solid #a78bfa" : "2px solid transparent",
        fontWeight: active ? 700 : 500,
        cursor: "pointer",
        minHeight: 56,
        width: 180,
        ...style,
      }}
      onHoverStart={() => isHovered.set(1)}
      onHoverEnd={() => isHovered.set(0)}
      onFocus={() => isHovered.set(1)}
      onBlur={() => isHovered.set(0)}
      className={className}
      tabIndex={0}
      role="button"
      aria-haspopup="true"
    >
      {children}
    </motion.div>
  );
}

export function DockLabel({ children, className = "", isHovered }) {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    if (!isHovered) return;
    const unsubscribe = isHovered.on("change", (latest) => setIsVisible(latest === 1));
    return () => unsubscribe && unsubscribe();
  }, [isHovered]);

  return (
    <AnimatePresence>
      {isVisible && (
        <motion.div
          initial={{ opacity: 0, y: 0 }}
          animate={{ opacity: 1, y: -10 }}
          exit={{ opacity: 0, y: 0 }}
          transition={{ duration: 0.2 }}
          className={`absolute -top-6 left-1/2 w-fit whitespace-pre rounded-md border border-gray-200 bg-gray-100 px-2 py-0.5 text-xs text-neutral-700 dark:border-neutral-900 dark:bg-neutral-800 dark:text-white ${className}`}
          role="tooltip"
          style={{ x: "-50%" }}
        >
          {children}
        </motion.div>
      )}
    </AnimatePresence>
  );
}

export function DockIcon({ children, className = "", width, active }) {
  return (
    <div className={className} style={{ display: "flex", alignItems: "center" }}>
      {React.cloneElement(children, {
        style: {
          fontSize: 26,
          color: active ? "#7c3aed" : "#6366f1",
          transition: "color 0.2s",
        },
      })}
    </div>
  );
}