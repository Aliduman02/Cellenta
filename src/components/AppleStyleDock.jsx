import { Home, Package, FileText, User } from "lucide-react";
import { Dock, DockIcon, DockItem } from "./Dock";
import React from "react";

const data = [
  { title: "Ana Sayfa", icon: <Home />, href: "/dashboard" },
  { title: "Mağaza", icon: <Package />, href: "/store" },
  { title: "Faturalar", icon: <FileText />, href: "/bills" },
  { title: "Profil", icon: <User />, href: "/profile" },
];

function isActive(href) {
  return window.location.pathname === href;
}

export default function AppleStyleDock() {
  return (
    <div 
      style={{ 
        position: "fixed", 
        left: 48, 
        top: "50%", 
        transform: "translateY(-50%)", 
        zIndex: 100,
        transition: "all 0.3s ease"
      }}
    >
      <div 
        style={{ 
          minWidth: 200,
          maxWidth: 220,
          background: "rgba(255, 255, 255, 0.95)",
          backdropFilter: "blur(20px)",
          borderRadius: 20,
          padding: "16px 12px",
          boxShadow: "0 8px 32px rgba(0,0,0,0.12), 0 4px 16px rgba(0,0,0,0.08)",
          border: "1px solid rgba(255,255,255,0.3)",
          animation: "slideInFromLeft 0.8s ease 0.2s both",
          display: "flex",
          flexDirection: "column",
          gap: "8px",
          overflow: "hidden"
        }}
      >
        {data.map((item, idx) => {
          const active = isActive(item.href);
          return (
            <a
              key={idx}
              href={item.href}
              style={{
                textDecoration: "none",
                width: "100%",
                display: "flex",
                alignItems: "center",
                gap: 12,
                transition: "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)",
              }}
            >
              <div 
                className="dock-item"
                style={{
                  width: "100%",
                  minHeight: 52,
                  padding: "12px 16px",
                  borderRadius: 16,
                  background: active 
                    ? "rgba(255, 255, 255, 0.98)"
                    : "rgba(255,255,255,0.6)",
                  boxShadow: active 
                    ? "0 8px 24px rgba(0,0,0,0.12), inset 0 1px 0 rgba(255,255,255,0.8)" 
                    : "0 2px 8px rgba(0,0,0,0.04), inset 0 1px 0 rgba(255,255,255,0.4)",
                  border: active ? "2px solid rgba(255, 255, 255, 0.8)" : "2px solid rgba(255,255,255,0.2)",
                  fontWeight: active ? 700 : 500,
                  cursor: "pointer",
                  position: "relative",
                  overflow: "hidden",
                  transform: active ? "translateX(6px) scale(1.02)" : "translateX(0) scale(1)",
                  animation: `fadeInScale 0.6s ease ${idx * 0.1}s both`,
                  display: "flex",
                  alignItems: "center",
                  gap: 12,
                  transition: "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)"
                }}
                onMouseEnter={(e) => {
                  if (!active) {
                    e.currentTarget.style.background = "rgba(255,255,255,0.85)";
                    e.currentTarget.style.transform = "translateX(4px) scale(1.03)";
                    e.currentTarget.style.boxShadow = "0 4px 16px rgba(0,0,0,0.08), inset 0 1px 0 rgba(255,255,255,0.6)";
                  } else {
                    e.currentTarget.style.transform = "translateX(8px) scale(1.04)";
                    e.currentTarget.style.boxShadow = "0 10px 28px rgba(0,0,0,0.15), inset 0 1px 0 rgba(255,255,255,0.9)";
                  }
                }}
                onMouseLeave={(e) => {
                  if (!active) {
                    e.currentTarget.style.background = "rgba(255,255,255,0.6)";
                    e.currentTarget.style.transform = "translateX(0) scale(1)";
                    e.currentTarget.style.boxShadow = "0 2px 8px rgba(0,0,0,0.04), inset 0 1px 0 rgba(255,255,255,0.4)";
                  } else {
                    e.currentTarget.style.transform = "translateX(6px) scale(1.02)";
                    e.currentTarget.style.boxShadow = "0 8px 24px rgba(0,0,0,0.12), inset 0 1px 0 rgba(255,255,255,0.8)";
                  }
                }}
              >
                {/* Subtle glow effect for all items */}
                <div 
                  style={{
                    position: "absolute",
                    top: -1,
                    left: -1,
                    right: -1,
                    bottom: -1,
                    background: active 
                      ? "linear-gradient(135deg, rgba(255, 255, 255, 0.3), rgba(248, 250, 252, 0.2))"
                      : "linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(248, 250, 252, 0.05))",
                    borderRadius: 17,
                    animation: active 
                      ? "pulse 3s ease-in-out infinite" 
                      : "subtlePulse 4s ease-in-out infinite",
                    zIndex: -1,
                    transition: "all 0.3s ease"
                  }}
                />
                
                <div 
                  style={{
                    transition: "all 0.3s ease",
                    transform: active ? "scale(1.1)" : "scale(1)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center"
                  }}
                >
                  <div style={{ 
                    color: active ? "#1f2937" : "#4b5563",
                    transition: "all 0.3s ease",
                    fontSize: "20px"
                  }}>
                    {item.icon}
                  </div>
                </div>
                
                <span
                  style={{
                    fontSize: 16,
                    color: active ? "#1f2937" : "#374151",
                    fontWeight: active ? 700 : 500,
                    letterSpacing: 0.3,
                    transition: "all 0.3s ease",
                    flex: 1
                  }}
                >
                  {item.title}
                </span>
              </div>
            </a>
          );
        })}
      </div>
      
      <style>{`
        @keyframes pulse {
          0%, 100% {
            opacity: 0.5;
          }
          50% {
            opacity: 0.8;
          }
        }
        
        @keyframes subtlePulse {
          0%, 100% {
            opacity: 0.3;
            transform: scale(1);
          }
          50% {
            opacity: 0.5;
            transform: scale(1.02);
          }
        }
        
        @keyframes fadeInScale {
          0% {
            opacity: 0;
            transform: translateY(20px) scale(0.8);
          }
          100% {
            opacity: 1;
            transform: translateY(0) scale(1);
          }
        }
        
        @keyframes slideInFromLeft {
          0% {
            opacity: 0;
            transform: translateX(-50px) scale(0.9);
          }
          100% {
            opacity: 1;
            transform: translateX(0) scale(1);
          }
        }
      `}</style>
    </div>
  );
}
